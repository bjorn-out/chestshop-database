package io.github.md5sha256.chestshopdatabase.model;

import io.github.md5sha256.chestshopdatabase.util.BlockPosition;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record PartialHydratedShop(
        @NotNull UUID worldId,
        int posX,
        int posY,
        int posZ,
        @NotNull String itemCode,
        byte @NotNull [] itemBytes,
        @NotNull String ownerName,
        @Nullable Double buyPrice,
        @Nullable Double sellPrice,
        int quantity,
        int stock,
        int estimatedCapacity
) {

    public PartialHydratedShop {
        if (buyPrice == null && sellPrice == null) {
            throw new IllegalArgumentException(
                    "Shop cannot have both buyPrice and sellPrice be null!");
        }
    }

    @NotNull
    public BlockPosition blockPosition() {
        return new BlockPosition(this.worldId, this.posX, this.posY, this.posZ);
    }

    @NotNull
    public HydratedShop fullyHydrate() {
        return new HydratedShop(
                worldId,
                posX,
                posY,
                posZ,
                new ChestshopItem(ItemStack.deserializeBytes(itemBytes), itemCode),
                ownerName,
                buyPrice,
                sellPrice,
                quantity,
                stock,
                estimatedCapacity
        );
    }
}
