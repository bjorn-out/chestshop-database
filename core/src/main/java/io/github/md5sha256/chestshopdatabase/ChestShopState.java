package io.github.md5sha256.chestshopdatabase;

import io.github.md5sha256.chestshopdatabase.model.HydratedShop;
import io.github.md5sha256.chestshopdatabase.util.BlockPosition;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface ChestShopState {

    @NotNull Set<String> itemCodes();

    boolean cachedShopRegistered(@NotNull BlockPosition position);

    void queueShopCreation(@NotNull HydratedShop shop);

    void queueShopUpdate(@NotNull HydratedShop shop);

    void queueShopDeletion(@NotNull BlockPosition position);
}
