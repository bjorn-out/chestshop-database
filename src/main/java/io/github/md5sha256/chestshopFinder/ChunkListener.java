package io.github.md5sha256.chestshopFinder;

import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ChunkListener implements Listener {

    private final Logger logger;

    public ChunkListener(@Nonnull Logger logger) {
        this.logger = logger;
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        long start = System.currentTimeMillis();
        UUID worldId = event.getWorld().getUID();
        List<PartialShop> shops = event.getChunk()
                .getTileEntities(block -> Tag.SIGNS.isTagged(block.getType()), false)
                .parallelStream()
                .map(Sign.class::cast)
                .map(sign -> toPartialShop(worldId, sign))
                .toList();
        long numShops = shops.size();
        if (numShops == 0) {
            return;
        }
        long end = System.currentTimeMillis();
        long elapsedMillis = end - start;
        Supplier<String> func = () -> String.format("Took %dms to partially parse %d shops", elapsedMillis, numShops);
        this.logger.info(func);
    }

    @Nullable
    private PartialShop toPartialShop(@Nonnull UUID worldId, @Nonnull Sign sign) {
        String[] lines = sign.getLines();
        if (!ChestShopSign.isValid(lines)) {
            return null;
        }
        String itemCode = ChestShopSign.getItem(lines);
        int x = sign.getX();
        int y = sign.getY();
        int z = sign.getZ();
        String ownerName = ChestShopSign.getOwner(lines);
        String priceLine = ChestShopSign.getPrice(lines);
        BigDecimal buyPriceDecimal = PriceUtil.getExactBuyPrice(priceLine);
        BigDecimal sellPriceDecimal = PriceUtil.getExactSellPrice(priceLine);
        Double buyPrice = buyPriceDecimal.equals(PriceUtil.NO_PRICE) ? null : toDouble(buyPriceDecimal);
        Double sellPrice = sellPriceDecimal.equals(PriceUtil.NO_PRICE) ? null : toDouble(sellPriceDecimal);
        int quantity = ChestShopSign.getQuantity(lines);
        return new PartialShop(
                worldId,
                x,
                y,
                z,
                itemCode,
                ownerName,
                buyPrice,
                sellPrice,
                quantity
        );
    }

    private double toDouble(BigDecimal decimal) {
        return decimal.setScale(4, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .doubleValue();
    }

}
