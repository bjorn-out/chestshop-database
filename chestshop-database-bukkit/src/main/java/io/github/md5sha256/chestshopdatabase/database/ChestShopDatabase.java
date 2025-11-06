package io.github.md5sha256.chestshopdatabase.database;

import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import io.github.md5sha256.chestshopdatabase.ChestShopState;
import io.github.md5sha256.chestshopdatabase.model.ChestshopItem;
import io.github.md5sha256.chestshopdatabase.model.HydratedShop;
import io.github.md5sha256.chestshopdatabase.util.BlockPosition;
import io.github.md5sha256.chestshopdatabase.util.InventoryUtil;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ChestShopDatabase(@NotNull ChestShopState shopState) {


    private static double toDouble(BigDecimal decimal) {
        return decimal.setScale(4, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .doubleValue();
    }

    public void registerShop(
            @NotNull BlockPosition position,
            @NotNull ItemStack itemStack,
            @NotNull String itemCode,
            @NotNull String[] lines,
            @NotNull Container container
    ) {
        ChestshopItem item = new ChestshopItem(itemStack, itemCode);
        String ownerName = ChestShopSign.getOwner(lines);
        String priceLine = ChestShopSign.getPrice(lines);
        BigDecimal buyPriceDecimal = PriceUtil.getExactBuyPrice(priceLine);
        BigDecimal sellPriceDecimal = PriceUtil.getExactSellPrice(priceLine);
        Double buyPrice = buyPriceDecimal.equals(PriceUtil.NO_PRICE) ? null : toDouble(
                buyPriceDecimal);
        Double sellPrice = sellPriceDecimal.equals(PriceUtil.NO_PRICE) ? null : toDouble(
                sellPriceDecimal);
        int quantity = ChestShopSign.getQuantity(lines);
        int stock = InventoryUtil.countItems(itemStack, container.getInventory());
        int capacity = InventoryUtil.remainingCapacity(itemStack, container.getInventory());
        HydratedShop shop = new HydratedShop(
                position.world(),
                position.x(),
                position.y(),
                position.z(),
                item,
                ownerName,
                buyPrice,
                sellPrice,
                quantity,
                stock,
                capacity);
        this.shopState.queueShopCreation(shop);
    }


}
