package io.github.md5sha256.chestshopdatabase.command;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.md5sha256.chestshopdatabase.ChestShopState;
import io.github.md5sha256.chestshopdatabase.ExecutorState;
import io.github.md5sha256.chestshopdatabase.ItemDiscoverer;
import io.github.md5sha256.chestshopdatabase.database.DatabaseSession;
import io.github.md5sha256.chestshopdatabase.gui.ShopGUI;
import io.github.md5sha256.chestshopdatabase.model.Shop;
import io.github.md5sha256.chestshopdatabase.model.ShopType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public record FindCommand(@Nonnull ChestShopState shopState,
                          @Nonnull ItemDiscoverer discoverer,
                          @Nonnull Supplier<DatabaseSession> databaseSupplier,
                          @Nonnull ExecutorState executorState,
                          @Nonnull ShopGUI gui) implements CommandBean.Single {

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("find")
                .requires(sourceStack -> sourceStack.getSender() instanceof Player)
                .then(Commands.argument("itemCode", new ItemCodesArgumentType(shopState))
                        .executes(ctx -> {
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                return Command.SINGLE_SUCCESS;
                            }
                            String itemCode = ctx.getArgument("itemCode", String.class);
                            processCommand(player, itemCode);
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }

    private void processCommand(@Nonnull Player player, @Nonnull String itemCode) {
        UUID world = player.getWorld().getUID();
        this.discoverer.discoverItemCode(itemCode, item -> {
            if (item == null || item.isEmpty()) {
                player.sendMessage(Component.text("Unknown item: " + itemCode, NamedTextColor.RED));
                return;
            }
            CompletableFuture.supplyAsync(() -> {
                        try (DatabaseSession session = databaseSupplier.get()) {
                            return session.mapper().selectShopsByWorldAndItem(
                                    ShopType.BOTH,
                                    world,
                                    itemCode);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            // FIXME log warning
                            return Collections.<Shop>emptyList();
                        }
                    }, this.executorState.dbExec())
                    .thenAcceptAsync((shops) -> {
                        Component title = Component.text("Shop results for " + itemCode);
                        ChestGui chestGui = this.gui.createGui(title, shops, item);
                        chestGui.show(player);
                    }, this.executorState.mainThreadExec());
        });
    }

    private String priceToString(Double price) {
        return price == null ? "N/A" : price.toString();
    }

    private String capacityToString(int cap) {
        return cap == -1 ? "infinity" : String.valueOf(cap);
    }

    private Component formatShop(@Nonnull Shop shop) {
        return Component.text()
                .append(Component.text("Owner: " + shop.ownerName() + ",", NamedTextColor.GREEN))
                .appendNewline()
                .append(Component.text(String.format("Buy Price: %s, Sell Price: %s",
                        priceToString(shop.buyPrice()),
                        priceToString(shop.sellPrice())), NamedTextColor.AQUA))
                .appendNewline()
                .append(Component.text(String.format("Quantity: %d", shop.quantity()),
                        NamedTextColor.LIGHT_PURPLE))
                .appendNewline()
                .append(Component.text(String.format("Stock: %d", shop.stock()),
                        NamedTextColor.YELLOW))
                .appendNewline()
                .append(Component.text(String.format("Remaining Capacity: %s",
                        capacityToString(shop.remainingCapacity())), NamedTextColor.YELLOW))
                .appendNewline()
                .append(Component.text(String.format("Location: %d, %d, %d",
                        shop.posX(),
                        shop.posY(),
                        shop.posZ()), NamedTextColor.RED))
                .appendNewline()
                .build();
    }
}
