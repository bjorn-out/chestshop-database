package io.github.md5sha256.chestshopdatabase.adapters.fawe;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import io.github.md5sha256.chestshopdatabase.util.BlockPosition;

import javax.annotation.Nonnull;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class ChestShopDatabaseLogger extends AbstractDelegateExtent {

    private final Queue<BlockPosition> regionQueue;
    private final UUID world;

    protected ChestShopDatabaseLogger(Queue<BlockPosition> regionQueue, Extent extent, UUID world) {
        super(extent);
        this.regionQueue = regionQueue;
        this.world = world;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position,
                                                            T block) throws WorldEditException {
        addPositionsToQueue(position);
        return super.setBlock(position, block);
    }

    @Override
    public <B extends BlockStateHolder<B>> int setBlocks(Region region,
                                                         B block) throws MaxChangedBlocksException {
        return super.setBlocks(region, block);
    }

    @Override
    public int setBlocks(Set<BlockVector3> vset, Pattern pattern) {
        return super.setBlocks(vset, pattern);
    }

    @Override
    public int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
        return super.setBlocks(region, pattern);
    }

    private void addPositionsToQueue(@Nonnull BlockVector3 position) {
        this.regionQueue.add(new BlockPosition(world, position.x(), position.y(), position.z()));
    }
}
