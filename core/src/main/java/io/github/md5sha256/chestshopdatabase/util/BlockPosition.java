package io.github.md5sha256.chestshopdatabase.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public record BlockPosition(@NotNull UUID world, int x, int y, int z) {
    public BlockPosition {
        Objects.requireNonNull(world, "world cannot be null!");
    }

    public long distanceSquared(@NotNull BlockPosition other) {
        if (!this.world.equals(other.world)) {
            return Long.MAX_VALUE;
        }
        long dx = this.x - other.x;
        long dy = this.y - other.y;
        long dz = this.z - other.z;

        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    public int xChunk() {
        return Math.floorMod(this.x, 16);
    }

    public int zChunk() {
        return Math.floorMod(this.z, 16);
    }

    @NotNull
    public ChunkPosition chunkPosition() {
        return new ChunkPosition(this.world, this.x << 4, this.z << 4);
    }
}
