package io.github.md5sha256.chestshopFinder;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DatabaseInterface {

    @Nonnull
    CompletableFuture<Void> registerShop(@Nonnull Shop shop);

    @Nonnull
    CompletableFuture<Void> deleteShop(@Nonnull UUID world, int posX, int posY, int posZ);

    @Nonnull
    CompletableFuture<List<Shop>> getShopsWithItemInWorld(
            @Nonnull UUID world,
            @Nonnull ShopType shopType,
            @Nonnull ItemStack item
    );

}
