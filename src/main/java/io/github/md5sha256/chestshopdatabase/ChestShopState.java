package io.github.md5sha256.chestshopdatabase;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.md5sha256.chestshopdatabase.database.DatabaseInterface;
import io.github.md5sha256.chestshopdatabase.model.HydratedShop;
import io.github.md5sha256.chestshopdatabase.util.BlockPosition;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class ChestShopState {

    private final Cache<BlockPosition, Boolean> shopCache;

    private final Map<BlockPosition, HydratedShop> createdShops = new HashMap<>();
    private final Map<BlockPosition, HydratedShop> updatedShops = new HashMap<>();
    private final Set<BlockPosition> deletedShops = new HashSet<>();

    public ChestShopState(@Nonnull Duration shopCacheDuration) {
        this.shopCache = CacheBuilder.newBuilder()
                .expireAfterAccess(shopCacheDuration)
                .build();
    }

    @Nonnull
    public Consumer<DatabaseInterface> flushTask() {
        List<HydratedShop> created = List.copyOf(this.createdShops.values());
        List<HydratedShop> updated = List.copyOf(this.updatedShops.values());
        List<HydratedShop> toInsert = new ArrayList<>(created.size() + updated.size());
        toInsert.addAll(created);
        toInsert.addAll(updated);
        List<BlockPosition> deleted = List.copyOf(this.deletedShops);
        this.createdShops.clear();
        this.updatedShops.clear();
        this.deletedShops.clear();
        return (database) -> {
            deleted.forEach(database::deleteShopByPos);
            database.insertShops(toInsert);
            database.flushSession();
        };
    }

    public boolean cachedShopRegistered(@Nonnull BlockPosition position) {
        return Objects.requireNonNullElse(this.shopCache.getIfPresent(position), Boolean.FALSE);
    }

    public void queueShopCreation(@Nonnull HydratedShop shop) {
        BlockPosition position = shop.blockPosition();
        this.updatedShops.remove(position);
        this.deletedShops.remove(position);
        this.createdShops.put(position, shop);
        this.shopCache.put(position, Boolean.TRUE);
    }

    public void queueShopUpdate(@Nonnull HydratedShop shop) {
        BlockPosition position = shop.blockPosition();
        this.createdShops.remove(position);
        this.deletedShops.remove(position);
        this.updatedShops.put(position, shop);
        this.shopCache.put(position, Boolean.TRUE);
    }

    public void queueShopDeletion(@Nonnull BlockPosition position) {
        this.createdShops.remove(position);
        this.updatedShops.remove(position);
        this.deletedShops.add(position);
        this.shopCache.invalidate(position);
    }


}
