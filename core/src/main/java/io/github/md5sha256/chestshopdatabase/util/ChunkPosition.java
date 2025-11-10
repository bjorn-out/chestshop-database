package io.github.md5sha256.chestshopdatabase.util;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ChunkPosition(@NotNull UUID worldId, int chunkX, int chunkZ) {
}
