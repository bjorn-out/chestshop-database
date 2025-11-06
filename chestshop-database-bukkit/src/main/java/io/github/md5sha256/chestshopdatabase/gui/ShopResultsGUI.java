package io.github.md5sha256.chestshopdatabase.gui;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.component.PagingButtons;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import io.github.md5sha256.chestshopdatabase.ReplacementRegistry;
import io.github.md5sha256.chestshopdatabase.model.Shop;
import io.github.md5sha256.chestshopdatabase.settings.Settings;
import io.github.md5sha256.chestshopdatabase.util.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record ShopResultsGUI(@NotNull Plugin plugin, @NotNull Settings settings, @NotNull
                             ReplacementRegistry replacements) {


    private static Component shopDisplayName(@NotNull Shop shop) {
        return Component.text()
                .content(shop.ownerName())
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
                .build();
    }

    private static String distanceString(Shop shop, @Nullable BlockPosition queryPosition) {
        if (queryPosition == null) return "∞";
        long squaredDistance = shop.blockPosition().distanceSquared(queryPosition);
        if (squaredDistance == Long.MAX_VALUE) return "∞";
        return String.format("%d", (long) Math.floor(Math.sqrt(squaredDistance)));
    }

    private static Component formatLore(Component lore) {
        return lore.decoration(TextDecoration.ITALIC, false);
    }

    private List<Component> shopLore(@NotNull Shop shop,
                                     @Nullable BlockPosition queryPosition) {
        ReplacementRegistry forked = this.replacements.fork()
                .stringReplacement("%distance%", s -> distanceString(s, queryPosition));
        return Stream.of(
                Component.text("Buy Price: %buy-price%, Sell Price: %sell-price%", NamedTextColor.AQUA),
                Component.text("Unit Buy Price: %buy-price-unit%, Unit Sell Price: %sell-price-unit%", NamedTextColor.AQUA),
                Component.text("Quantity: %quantity%", NamedTextColor.LIGHT_PURPLE),
                Component.text("Stock: %stock%", NamedTextColor.YELLOW),
                Component.text("Remaining Capacity: %capacity%", NamedTextColor.YELLOW),
                Component.text("Distance: %distance%", NamedTextColor.RED),
                Component.text("Location: %x%, %y%, %z% (%world%)", NamedTextColor.RED)
        )
                .map(component -> forked.applyReplacements(shop, component))
                .map(ShopResultsGUI::formatLore).toList();
    }

    private ItemStack shopToIcon(@NotNull Shop shop,
                                 @Nullable BlockPosition queryPosition) {
        Material material = switch (shop.shopType()) {
            case BOTH -> Material.ENDER_CHEST;
            case BUY -> Material.HOPPER_MINECART;
            case SELL -> Material.CHEST_MINECART;
        };
        ItemStack itemStack = ItemStack.of(material);
        itemStack.editMeta(meta -> {
            meta.displayName(shopDisplayName(shop));
            meta.lore(shopLore(shop, queryPosition));
        });
        return itemStack;
    }

    private GuiItem shopItemPreview(@NotNull ItemStack item) {
        return new GuiItem(item, event -> event.setCancelled(true), this.plugin);
    }

    public ChestGui createGui(@NotNull Component title,
                              @NotNull List<Shop> shops,
                              @NotNull ItemStack shopItem,
                              @Nullable BlockPosition queryPosition) {
        return createGui(title, shops, shopItem, queryPosition, null);
    }

    @NotNull
    private String injectPlaceholders(@NotNull String s, @NotNull Shop shop) {
        BlockPosition pos = shop.blockPosition();
        return s.replace("<x>", String.valueOf(pos.x()))
                .replace("<y>", String.valueOf(pos.y()))
                .replace("<z>", String.valueOf(pos.z()));
    }

    private GuiItem shopToGuiItem(@NotNull Shop shop,
                                  @Nullable BlockPosition queryPosition) {
        String clickCommand = settings().clickCommand();
        if (clickCommand == null || clickCommand.isEmpty()) {
            return new GuiItem(shopToIcon(shop, queryPosition), this.plugin);
        }

        String injected = injectPlaceholders(clickCommand, shop);

        return new GuiItem(shopToIcon(shop, queryPosition), (event) -> {
            event.setCancelled(true);
            event.getView().close();
            HumanEntity clicked = event.getWhoClicked();
            if (clicked instanceof Player player) {
                player.performCommand(injected);
            }
        }, this.plugin);
    }

    public ChestGui createGui(@NotNull Component title,
                              @NotNull List<Shop> shops,
                              @NotNull ItemStack shopItem,
                              @Nullable BlockPosition queryPosition,
                              @Nullable Gui parent) {
        ChestGui gui = new ChestGui(6, ComponentHolder.of(title), this.plugin);
        List<GuiItem> items = new ArrayList<>();
        for (Shop shop : shops) {
            GuiItem item = shopToGuiItem(shop, queryPosition);
            items.add(item);
        }
        PaginatedPane mainPane = new PaginatedPane(9, 5);
        mainPane.populateWithGuiItems(items);

        StaticPane footerPane = getFooterPane(parent);
        footerPane.addItem(shopItemPreview(shopItem), 4, 0);

        ItemStack fillItem = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        fillItem.editMeta(meta -> meta.displayName(Component.empty()));
        footerPane.fillWith(fillItem, null, this.plugin);

        PagingButtons pagingButtons = getPagingButtons(5, mainPane);

        gui.addPane(mainPane);
        gui.addPane(footerPane);
        gui.addPane(pagingButtons);
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        if (parent != null) {
            gui.setOnClose(event -> {
                // Don't force-open the parent gui if the reason is OPEN_NEW
                if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) {
                    return;
                }
                // Delay opening the ui 1 tick later otherwise all IF listeners will break
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                    parent.show(event.getPlayer());
                }, 1);
            });
        }
        return gui;
    }

    private @NotNull StaticPane getFooterPane(@Nullable Gui parent) {
        StaticPane footerPane = new StaticPane(0, 5, 9, 1, Pane.Priority.LOWEST);
        ItemStack backItem = ItemStack.of(Material.ARROW);
        backItem.editMeta(meta -> {
            Component displayName = Component.text("Back", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false);
            meta.displayName(displayName);
        });
        GuiItem backButton = new GuiItem(backItem, event -> {
            event.getView().close();
            if (parent != null) {
                parent.show(event.getWhoClicked());
            }
        }, this.plugin);
        footerPane.addItem(backButton, 0, 0);
        return footerPane;
    }

    private @NotNull PagingButtons getPagingButtons(int y, PaginatedPane mainPane) {
        PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(3, y),
                3,
                Pane.Priority.HIGH,
                mainPane,
                this.plugin);
        Component nextPageComp = Component.text("Next Page")
                .decoration(TextDecoration.ITALIC, false);
        Component prevPageComp = Component.text("Prev Page")
                .decoration(TextDecoration.ITALIC, false);
        ItemStack nextButton = ItemStack.of(Material.PAPER);
        nextButton.editMeta(meta -> meta.displayName(nextPageComp));
        ItemStack prevButton = ItemStack.of(Material.PAPER);
        prevButton.editMeta(meta -> meta.displayName(prevPageComp));
        pagingButtons.setForwardButton(new GuiItem(nextButton, this.plugin));
        pagingButtons.setBackwardButton(new GuiItem(prevButton, this.plugin));
        return pagingButtons;
    }

}
