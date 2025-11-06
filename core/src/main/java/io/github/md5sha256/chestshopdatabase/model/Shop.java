package io.github.md5sha256.chestshopdatabase.model;

import io.github.md5sha256.chestshopdatabase.util.BlockPosition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public record Shop(
        @NotNull UUID worldId,
        int posX,
        int posY,
        int posZ,
        @NotNull String itemCode,
        @NotNull String ownerName,
        @Nullable Double buyPrice,
        @Nullable Double sellPrice,
        int quantity,
        int stock,
        int estimatedCapacity
) {

    public Shop {
        if (buyPrice == null && sellPrice == null) {
            throw new IllegalArgumentException("Shop cannot have both buyPrice and sellPrice be null!");
        }
    }

    @NotNull
    public BlockPosition blockPosition() {
        return new BlockPosition(this.worldId, this.posX, this.posY, this.posZ);
    }

    public ShopType shopType() {
        if (this.buyPrice != null && this.sellPrice != null) {
            return ShopType.BOTH;
        } else if (this.sellPrice != null) {
            return ShopType.SELL;
        }
        return ShopType.BUY;
    }

    public Double unitBuyPrice() {
        if (buyPrice() == null) return null;
        return buyPrice() / quantity();
    }

    public Double unitSellPrice () {
        if (sellPrice() == null) return null;
        return sellPrice() / quantity();
    }
}
