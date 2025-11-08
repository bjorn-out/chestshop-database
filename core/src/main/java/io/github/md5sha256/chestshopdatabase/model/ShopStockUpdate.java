package io.github.md5sha256.chestshopdatabase.model;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ShopStockUpdate(@NotNull  UUID worldUUID, int x, int y, int z, int stock, int estimatedCapacity) {}
