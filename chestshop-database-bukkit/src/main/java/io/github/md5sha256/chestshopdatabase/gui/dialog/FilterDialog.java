package io.github.md5sha256.chestshopdatabase.gui.dialog;

import io.github.md5sha256.chestshopdatabase.gui.FindState;
import io.github.md5sha256.chestshopdatabase.model.ShopType;
import io.github.md5sha256.chestshopdatabase.util.DialogUtil;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class FilterDialog {

    @NotNull
    private static DialogBase createShopFiltersBase(@NotNull Set<ShopType> includedTypes) {
        var options = List.of(SingleOptionDialogInput.OptionEntry.create("enabled",
                        Component.text("On", NamedTextColor.GREEN),
                        true),
                SingleOptionDialogInput.OptionEntry.create("disabled",
                        Component.text("Off", NamedTextColor.RED),
                        false));
        List<? extends DialogInput> inputs = Arrays.stream(ShopType.values())
                .map(type -> DialogInput.singleOption(type.name(),
                                Component.text(type.displayName()),
                                options)
                        .build())
                .toList();

        return DialogBase.builder(Component.text("Select Shop Types"))
                .canCloseWithEscape(true)
                .inputs(inputs)
                .build();
    }

    @NotNull
    public static Dialog createFiltersDialog(@NotNull FindState state,
                                             @NotNull Supplier<Dialog> prevDialog) {
        Set<ShopType> includedTypes = state.shopTypes();
        ActionButton saveButton = ActionButton.builder(Component.text("Save"))
                .tooltip(Component.text("Save selection and return to previous menu"))
                .action(DialogAction.customClick(applyFilters(state, prevDialog),
                        DialogUtil.DEFAULT_CALLBACK_OPTIONS))
                .build();
        ActionButton backButton = ActionButton.builder(Component.text("Back"))
                .tooltip(Component.text("Return to previous menu"))
                .action(DialogUtil.openDialogAction(prevDialog))
                .build();
        return Dialog.create(factory ->
                factory.empty()
                        .base(createShopFiltersBase(includedTypes))
                        .type(DialogType.confirmation(saveButton, backButton))
        );
    }

    private static DialogActionCallback applyFilters(@NotNull FindState findState,
                                                     @NotNull Supplier<Dialog> prevDialog) {
        return (view, audience) -> {
            Set<ShopType> included = EnumSet.noneOf(ShopType.class);
            for (ShopType shopType : ShopType.values()) {
                String value = view.getText(shopType.name());
                if (value != null && value.equals("enabled")) {
                    included.add(shopType);
                }
            }
            findState.setShopTypes(included);
            audience.showDialog(prevDialog.get());
        };
    }

}
