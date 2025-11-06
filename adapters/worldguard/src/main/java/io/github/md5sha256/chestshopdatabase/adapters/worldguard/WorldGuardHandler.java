package io.github.md5sha256.chestshopdatabase.adapters.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.github.md5sha256.chestshopdatabase.ReplacementRegistry;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;

public class WorldGuardHandler {
    private final Plugin plugin;
    private final ReplacementRegistry replacementRegistry;

    public WorldGuardHandler (@NotNull Plugin plugin, @NotNull ReplacementRegistry replacementRegistry) {
        this.plugin = plugin;
        this.replacementRegistry = replacementRegistry;
        initialize();
    }

    private void initialize () {
        replacementRegistry.stringReplacement("region-name", (shop) -> {
            // Get WorldGuard instance & platform
            WorldGuard worldGuard = WorldGuard.getInstance();
            if (worldGuard == null) return "N/A";
            WorldGuardPlatform worldGuardPlatform = worldGuard.getPlatform();
            if (worldGuardPlatform == null) return "N/A";
            // Get Region Container
            RegionContainer regionContainer = worldGuardPlatform.getRegionContainer();
            if (regionContainer == null) return "N/A";
            // Get World
            World world = plugin.getServer().getWorld(shop.worldId());
            if (world == null) return "N/A";
            // Get Region Manager
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world));
            if (regionManager == null) return "N/A";
            // Get regions set
            ApplicableRegionSet regions = regionManager.getApplicableRegions(BukkitAdapter.adapt(new Location(world, shop.posX(), shop.posY(), shop.posZ())).toVector().toBlockPoint());
            Optional<ProtectedRegion> firstRegionInPriority = regions.getRegions().stream().min(Comparator.comparingInt(ProtectedRegion::getPriority));
            return firstRegionInPriority.isPresent() ? firstRegionInPriority.get().getId() : "N/A";
        });
    }
}
