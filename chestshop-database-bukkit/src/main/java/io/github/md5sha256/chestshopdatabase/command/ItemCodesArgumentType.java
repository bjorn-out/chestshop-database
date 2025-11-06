package io.github.md5sha256.chestshopdatabase.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.md5sha256.chestshopdatabase.ChestShopState;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ItemCodesArgumentType implements CustomArgumentType<String, String> {

    private final ArgumentType<String> nativeType = StringArgumentType.greedyString();
    private final ChestShopState shopState;

    public ItemCodesArgumentType(@NotNull ChestShopState shopState) {
        this.shopState = shopState;
    }


    @Override
    @NotNull
    public ArgumentType<String> getNativeType() {
        return this.nativeType;
    }


    @Override
    public String parse(@NotNull StringReader reader) throws CommandSyntaxException {
        return this.nativeType.parse(reader);
    }

    @Override
    @NotNull
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context,
                                                              @NotNull SuggestionsBuilder builder) {
        String input = builder.getRemainingLowerCase();
        this.shopState.itemCodes().stream()
                .filter(s -> s.toLowerCase(Locale.ENGLISH).contains(input))
                .sorted(Comparator.reverseOrder())
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
