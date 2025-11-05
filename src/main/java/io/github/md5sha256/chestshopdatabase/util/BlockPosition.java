package io.github.md5sha256.chestshopdatabase.util;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public record BlockPosition(@Nonnull UUID world, int x, int y, int z) {
    public BlockPosition {
        Objects.requireNonNull(world, "world cannot be null!");
    }

    public long distanceSquared(@Nonnull BlockPosition other) {
        if (!this.world.equals(other.world)) {
            return Long.MAX_VALUE;
        }
        long dx = this.x - other.x;
        long dy = this.y - other.y;
        long dz = this.z - other.z;

        return (dx * dx) + (dy * dy) + (dz * dz);
    }
}
