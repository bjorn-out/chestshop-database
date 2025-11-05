package io.github.md5sha256.chestshopdatabase.model;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.github.md5sha256.chestshopdatabase.util.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public record Shop(
        @Nonnull UUID worldId,
        int posX,
        int posY,
        int posZ,
        @Nonnull String itemCode,
        @Nonnull String ownerName,
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

    @Nonnull
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

    public String regionName () {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        World world = Bukkit.getServer().getWorld(worldId());
        if (world == null) return "N/A";
        RegionManager manager = container.get(BukkitAdapter.adapt(world));
        if (manager == null) return "N/A";
        ApplicableRegionSet regions = manager.getApplicableRegions(BukkitAdapter.adapt(new Location(world, posX(), posY(), posZ())).toVector().toBlockPoint());
        Optional<ProtectedRegion> firstRegionInPriority = regions.getRegions().stream().min(Comparator.comparingInt(ProtectedRegion::getPriority));
        return firstRegionInPriority.isEmpty() ? "N/A" : firstRegionInPriority.get().getId();
    }
}
