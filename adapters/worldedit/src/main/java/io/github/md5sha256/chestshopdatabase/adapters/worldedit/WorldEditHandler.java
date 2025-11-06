package io.github.md5sha256.chestshopdatabase.adapters.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import io.github.md5sha256.chestshopdatabase.ChestShopState;
import io.github.md5sha256.chestshopdatabase.util.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WorldEditHandler implements Listener {

    private final Queue<BlockPosition> regionQueue = new ConcurrentLinkedDeque<>();
    private final Plugin plugin;
    private final ChestShopState shopState;

    public WorldEditHandler(@NotNull Plugin plugin, @NotNull ChestShopState shopState) {
        this.plugin = plugin;
        this.shopState = shopState;
        initialize();
    }

    private void schedulePollTask() {
        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, () -> {
            int size = regionQueue.size();
            for (int i = 0; i < size; i++) {
                BlockPosition pos = regionQueue.poll();
                if (pos == null) {
                    break;
                }
                this.shopState.queueShopDeletion(pos);
            }
        }, 20, 20);
    }

    private void initialize() {
        WorldEdit.getInstance().getEventBus().register(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        schedulePollTask();
    }

    private @Nullable UUID getWorldId(@NotNull String worldName) {
        World bukkitWorld = Bukkit.getServer().getWorld(worldName);
        if (bukkitWorld == null) {
            return null;
        }
        return bukkitWorld.getUID();
    }

    @Subscribe
    public void onEditSession(EditSessionEvent event) {
        if (event.getWorld() == null) {
            return;
        }

        UUID worldId = getWorldId(event.getWorld().getName());
        if (worldId == null) {
            return;
        }
        Actor actor = event.getActor();
        if (actor != null && event.getStage() == (EditSession.Stage.BEFORE_HISTORY)) {
            event.setExtent(new ChestShopDatabaseLogger(this.regionQueue,
                    event.getExtent(),
                    worldId));
        }
    }

    @EventHandler
    public void onWorldEditDisable(PluginDisableEvent event) {
        if (!event.getPlugin().equals(this.plugin)) return;

        WorldEdit.getInstance().getEventBus().unregister(this);
    }

}
