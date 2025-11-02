package io.github.md5sha256.chestshopdatabase.settings;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ConfigSerializable
public record Settings(
        @Setting("database-settings") @Required @Nonnull DatabaseSettings databaseSettings,
        @Setting("result-gui-click-command") @Nullable String clickCommand
) {
}
