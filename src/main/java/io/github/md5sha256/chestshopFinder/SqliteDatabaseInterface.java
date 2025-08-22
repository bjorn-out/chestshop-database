package io.github.md5sha256.chestshopFinder;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SqliteDatabaseInterface implements DatabaseInterface {


    private static final String CREATE_ITEMS = """
            CREATE TABLE Items (
                item_id INTEGER PRIMARY KEY,
                item_bytes NOT NULL BLOB
            );
            """;

    private static final String CREATE_SHOP = """
            CREATE TABLE Shop (
                shop_id INTEGER PRIMARY KEY,
                world_uuid BLOB NOT NULL CHECK (length(item_uuid) = 16),
                pos_x INTEGER NOT NULL,
                pos_y INTEGER NOT NULL,
                pos_z INTEGER NOT NULL,
                owner_name TEXT,
                buy_price REAL,
                sell_price REAL,
                quantity INTEGER NOT NULL
                CHECK (buy_price IS NOT NULL OR sell_price IS NOT NULL),
                CHECK (buy_price IS NULL = buy_quantity IS NULL),
                CHECK (sell_price IS NULL = sell_quantity IS NULL),
                CHECK (quantity > 0),
            );
            """;

    private static final String CREATE_SHOP_CONSTRAINTS = """
            """;

    @Override
    public @NotNull CompletableFuture<Void> registerShop(@NotNull Shop shop) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Void> deleteShop(@NotNull UUID world,
                                                       int posX,
                                                       int posY,
                                                       int posZ) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<List<Shop>> getShopsWithItemInWorld(@NotNull UUID world,
                                                                          @NotNull ShopType shopType,
                                                                          @NotNull ItemStack item) {
        return null;
    }
}
