package io.github.md5sha256.chestshopdatabase.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public interface CommandBean {

    @Nonnull
    List<LiteralArgumentBuilder<CommandSourceStack>> commands();

    interface Single extends CommandBean {

        @Nonnull
        LiteralArgumentBuilder<CommandSourceStack> command();

        @Override
        @NotNull
        default List<LiteralArgumentBuilder<CommandSourceStack>> commands() {
            return List.of(command());
        }

    }

}
