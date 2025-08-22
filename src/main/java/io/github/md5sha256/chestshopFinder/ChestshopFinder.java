package io.github.md5sha256.chestshopFinder;

import org.bukkit.plugin.java.JavaPlugin;

public final class ChestshopFinder extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new ChunkListener(getLogger()), this);
        getLogger().info("Plugin enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Plugin disabled");
    }
}
