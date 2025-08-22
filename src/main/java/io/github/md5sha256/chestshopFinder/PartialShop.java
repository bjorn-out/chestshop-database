package io.github.md5sha256.chestshopFinder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public record PartialShop(
        @Nonnull UUID worldId,
        int posX,
        int posY,
        int posZ,
        @Nonnull String itemCode,
        @Nonnull String ownerName,
        @Nullable Double buyPrice,
        @Nullable Double sellPrice,
        int quantity
) {

}
