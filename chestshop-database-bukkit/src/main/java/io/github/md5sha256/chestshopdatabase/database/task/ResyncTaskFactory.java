package io.github.md5sha256.chestshopdatabase.database.task;

import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import io.github.md5sha256.chestshopdatabase.ChestShopState;
import io.github.md5sha256.chestshopdatabase.ExecutorState;
import io.github.md5sha256.chestshopdatabase.ItemDiscoverer;
import io.github.md5sha256.chestshopdatabase.database.DatabaseMapper;
import io.github.md5sha256.chestshopdatabase.database.DatabaseSession;
import io.github.md5sha256.chestshopdatabase.model.ShopStockUpdate;
import io.github.md5sha256.chestshopdatabase.task.TaskProgress;
import io.github.md5sha256.chestshopdatabase.util.BlockPosition;
import io.github.md5sha256.chestshopdatabase.util.ChunkPosition;
import io.github.md5sha256.chestshopdatabase.util.InventoryUtil;
import io.github.md5sha256.chestshopdatabase.util.TickUtil;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class ResyncTaskFactory {

    private final ChestShopState chestShopState;
    private final ItemDiscoverer discoverer;
    private final Supplier<DatabaseSession> sessionSupplier;
    private final ExecutorState executorState;
    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    public ResyncTaskFactory(
            @NotNull ChestShopState chestShopState,
            @NotNull ItemDiscoverer discoverer,
            @NotNull Supplier<DatabaseSession> sessionSupplier,
            @NotNull ExecutorState executorState,
            @NotNull Plugin plugin
    ) {
        this.chestShopState = chestShopState;
        this.discoverer = discoverer;
        this.sessionSupplier = sessionSupplier;
        this.executorState = executorState;
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    private static List<Bucket> toBuckets(@NotNull List<BlockPosition> positions) {
        Map<ChunkPosition, List<BlockPosition>> map = new HashMap<>();
        for (BlockPosition position : positions) {
            map.computeIfAbsent(position.chunkPosition(), unused -> new ArrayList<>())
                    .add(position);
        }
        List<Bucket> buckets = new ArrayList<>(map.size());
        for (Map.Entry<ChunkPosition, List<BlockPosition>> entry : map.entrySet()) {
            buckets.add(new Bucket(entry.getKey(), entry.getValue()));
        }
        return buckets;
    }

    private <T> CompletableFuture<T> toUpdateShopStock(@NotNull Sign sign,
                                                       @NotNull String[] lines,
                                                       @NotNull Container container,
                                                       Function<ShopStockUpdate, T> callback) {
        UUID world = sign.getWorld().getUID();
        int posX = sign.getX();
        int posY = sign.getY();
        int posZ = sign.getZ();
        String itemCode = ChestShopSign.getItem(lines);
        CompletableFuture<T> future = new CompletableFuture<>();
        this.discoverer.discoverItemStackFromCode(itemCode, itemStack -> {
            if (itemStack == null || itemStack.isEmpty()) {
                // FIXME log warning
                future.complete(callback.apply(null));
                return;
            }
            ShopStockUpdate shopStockUpdate = new ShopStockUpdate(
                    world,
                    posX,
                    posY,
                    posZ,
                    InventoryUtil.countItems(itemStack, container.getInventory()),
                    InventoryUtil.remainingCapacity(itemStack, container.getInventory())
            );
            future.complete(callback.apply(shopStockUpdate));
        });
        return future;
    }

    private CompletableFuture<Void> processBlockPosition(@NotNull Chunk chunk,
                                                         @NotNull BlockPosition pos) {
        try {
            Block block = chunk.getBlock(pos.xChunk(), pos.y(), pos.zChunk());
            if (!Tag.SIGNS.isTagged(block.getType())) {
                return CompletableFuture.completedFuture(null);
            }
            BlockState blockState = block.getState(false);
            if (blockState instanceof Sign sign) {
                Container container = uBlock.findConnectedContainer(sign);
                if (container != null) {
                    return toUpdateShopStock(sign,
                            sign.getLines(),
                            container,
                            update -> {
                                if (update != null) {
                                    return this.chestShopState.queueShopUpdate(update);
                                }
                                return CompletableFuture.<Void>completedFuture(null);
                            })
                            .thenCompose(Function.identity());
                } else {
                    // Don't do anything...?
                    return CompletableFuture.completedFuture(null);
                }
            }
            return chestShopState.queueShopDeletion(pos);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }


    private void processChunk(
            @NotNull Chunk chunk,
            @NotNull List<BlockPosition> blocks,
            @NotNull TaskProgress progress) {
        for (BlockPosition pos : blocks) {
            processBlockPosition(chunk, pos).thenRun(progress::markCompleted);
        }
    }

    private void processBuckets(
            @NotNull List<Bucket> buckets,
            @NotNull TaskProgress progress
    ) {
        Server server = this.plugin.getServer();
        for (Bucket bucket : buckets) {
            ChunkPosition chunkPosition = bucket.chunkPos();
            World world = server.getWorld(chunkPosition.worldId());
            List<BlockPosition> blocks = bucket.blocks();
            if (world == null) {
                blocks.forEach(chestShopState::queueShopDeletion);
                continue;
            }
            world.getChunkAtAsync(chunkPosition.chunkX(), chunkPosition.chunkZ(), false)
                    .thenAccept(chunk -> {
                        if (chunk != null) {
                            processChunk(chunk, blocks, progress);
                        } else {
                            progress.markCompleted(blocks.size());
                        }
                    });
        }
    }

    @NotNull
    public CompletableFuture<TaskProgress> triggerResync(
            int chunksPerInterval,
            int intervalTicks
    ) {
        CompletableFuture<TaskProgress> future = new CompletableFuture<>();
        this.executorState.dbExec().submit(() -> {
            List<Bucket> buckets;
            int numBlocks;
            try (DatabaseSession session = sessionSupplier.get()) {
                DatabaseMapper mapper = session.mapper();
                List<BlockPosition> blocks = mapper.selectShopsPositionsByWorld(null);
                numBlocks = blocks.size();
                buckets = toBuckets(blocks);
            } catch (Exception ex) {
                future.completeExceptionally(ex);
                return;
            }

            TaskProgress progress = new TaskProgress(numBlocks);
            future.complete(progress);
            TickUtil<Bucket> tickUtil = new TickUtil<>(list -> processBuckets(buckets, progress));
            tickUtil.queueElements(buckets);
            tickUtil.schedulePollTask(this.plugin,
                    this.scheduler,
                    chunksPerInterval,
                    intervalTicks);
            progress.chainOnComplete(tickUtil::cancelPollTask);
        });
        return future;
    }

    private record Bucket(@NotNull ChunkPosition chunkPos, @NotNull List<BlockPosition> blocks) {
    }

}
