package io.github.md5sha256.chestshopdatabase.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CommandBean {

    @NotNull
    List<LiteralArgumentBuilder<CommandSourceStack>> commands();

    interface Single extends CommandBean {

        @NotNull
        LiteralArgumentBuilder<CommandSourceStack> command();

        @Override
        @NotNull
        default List<LiteralArgumentBuilder<CommandSourceStack>> commands() {
            return List.of(command());
        }

    }

}
