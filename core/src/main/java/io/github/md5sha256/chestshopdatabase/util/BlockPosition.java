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
}
