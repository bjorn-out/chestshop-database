package io.github.md5sha256.chestshopdatabase.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public record Settings(
        @Setting("database-settings") @Required @NotNull DatabaseSettings databaseSettings,
        @Setting("result-gui-click-command") @Nullable String clickCommand
) {
}
