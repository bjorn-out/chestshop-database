package io.github.md5sha256.chestshopdatabase;

import io.github.md5sha256.chestshopdatabase.model.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ReplacementRegistry {

    private final Map<String, Function<Shop, Component>> replacements = new HashMap<>();

    public ReplacementRegistry() {
    }

    public ReplacementRegistry(@NotNull Map<String, Function<Shop, Component>> replacements) {
        this.replacements.putAll(replacements);
    }

    @NotNull
    public ReplacementRegistry replacement(@Nonnull String key,
                                           @Nonnull Function<Shop, Component> function) {
        this.replacements.put(key, function);
        return this;
    }

    @NotNull
    public ReplacementRegistry stringReplacement(@Nonnull String key,
                                                 @Nonnull Function<Shop, String> function) {
        this.replacements.put(key, function.andThen(Component::text));
        return this;
    }

    @NotNull
    public Component applyReplacements(@NotNull Shop shop, @NotNull Component component) {
        Component result = component;
        for (Map.Entry<String, Function<Shop, Component>> entry : replacements.entrySet()) {
            result = result.replaceText(TextReplacementConfig.builder()
                    .match(entry.getKey())
                    .replacement(() -> entry.getValue().apply(shop))
                    .build());
        }
        return result;
    }

    @NotNull
    public ReplacementRegistry fork() {
        return new ReplacementRegistry(this.replacements);
    }
}
