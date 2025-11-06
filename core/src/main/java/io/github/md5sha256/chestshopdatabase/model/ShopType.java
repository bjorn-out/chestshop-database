package io.github.md5sha256.chestshopdatabase.model;

import org.jetbrains.annotations.NotNull;

public enum ShopType {
    BUY("Buy"),
    SELL("Sell"),
    BOTH("Buy & Sell");

    private final String displayName;

    ShopType(@NotNull String displayName) {
        this.displayName = displayName;
    }

    @NotNull
    public String displayName() {
        return this.displayName;
    }
}
