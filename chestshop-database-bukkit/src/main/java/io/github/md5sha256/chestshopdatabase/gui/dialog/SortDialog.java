package io.github.md5sha256.chestshopdatabase.gui.dialog;

import io.github.md5sha256.chestshopdatabase.gui.FindState;
import io.github.md5sha256.chestshopdatabase.model.ShopAttribute;
import io.github.md5sha256.chestshopdatabase.util.DialogUtil;
import io.github.md5sha256.chestshopdatabase.util.SortDirection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class SortDialog {


    public static Dialog createSortDialog(@NotNull FindState findState,
                                          @NotNull Supplier<Dialog> prevDialog) {
        var buttons = List.of(
                ActionButton.builder(Component.text("Sorting Direction"))
                        .action(DialogUtil.openDialogAction(() -> createSortDirectionDialog(
                                findState, () -> createSortDialog(findState, prevDialog))))
                        .build(),
                ActionButton.builder(Component.text("Sorting Priority"))
                        .action(DialogUtil.openDialogAction(() -> createSortPriorityDialog(
                                findState, () -> createSortDialog(findState, prevDialog))))
                        .build()
        );

        var backButton = ActionButton.builder(Component.text("Back"))
                .tooltip(Component.text("Return to the previous menu"))
                .action(DialogUtil.openDialogAction(prevDialog)).build();

        return Dialog.create(factory -> factory.empty().base(sortingBase())
                .type(DialogType.multiAction(buttons)
                        .columns(1)
                        .exitAction(backButton)
                        .build()));
    }

    private static Dialog createSortDirectionDialog(@NotNull FindState findState,
                                                    @NotNull Supplier<Dialog> prevDialog) {

        var saveButton = ActionButton.builder(Component.text("Save"))
                .action(DialogAction.customClick(applyShopAttributeSortDirections(findState,
                                prevDialog),
                        DialogUtil.DEFAULT_CALLBACK_OPTIONS))
                .tooltip(Component.text("Save selection and return to the previous menu"))
                .build();

        var backButton = ActionButton.builder(Component.text("Back"))
                .tooltip(Component.text("Return to the previous menu"))
                .action(DialogUtil.openDialogAction(prevDialog)).build();
        var attributes = findState.selectedAttributes().stream().sorted().toList();

        return Dialog.create(factory ->
                factory.empty().base(setSortingDirectionBase(attributes))
                        .type(DialogType.confirmation(saveButton, backButton))
        );
    }

    private static Dialog createSortPriorityDialog(@NotNull FindState findState,
                                                   @NotNull Supplier<Dialog> prevDialog) {

        var saveButton = ActionButton.builder(Component.text("Save"))
                .action(DialogAction.customClick(applyShopAttributeSortPriority(findState,
                                prevDialog),
                        DialogUtil.DEFAULT_CALLBACK_OPTIONS))
                .tooltip(Component.text("Save selection and return to the previous menu"))
                .build();

        var backButton = ActionButton.builder(Component.text("Back"))
                .tooltip(Component.text("Return to the previous menu"))
                .action(DialogUtil.openDialogAction(prevDialog)).build();

        var attributes = findState.selectedAttributes().stream().sorted().toList();

        return Dialog.create(factory ->
                factory.empty().base(setPrioritiesBase(attributes))
                        .type(DialogType.confirmation(saveButton, backButton))
        );
    }

    @NotNull
    private static DialogBase sortingBase() {
        Component description = Component.text(
                "Configure how shops should be sorted");
        return DialogBase.builder(Component.text("Shop Sorting"))
                .body(List.of(DialogBody.plainMessage(description)))
                .build();
    }

    @NotNull
    private static DialogBase setSortingDirectionBase(@NotNull List<ShopAttribute> attributes) {
        var options = List.of(SingleOptionDialogInput.OptionEntry.create("ascending",
                        Component.text("Ascending", NamedTextColor.AQUA),
                        true),
                SingleOptionDialogInput.OptionEntry.create("descending",
                        Component.text("Descending", NamedTextColor.BLUE),
                        false),
                SingleOptionDialogInput.OptionEntry.create("disabled",
                        Component.text("Off", NamedTextColor.RED),
                        false));

        var directions = attributes.stream().map(attribute ->
                DialogInput.singleOption(attribute.name(),
                        Component.text(attribute.displayName()),
                        options
                ).build()
        ).toList();
        return DialogBase.builder(Component.text("Set Sorting Directions"))
                .inputs(directions)
                .build();
    }

    private static DialogBase setPrioritiesBase(@NotNull List<ShopAttribute> attributes) {

        var directions = attributes.stream().map(attribute ->
                DialogInput.numberRange(attribute.name(),
                        Component.text(attribute.displayName()),
                        0, 100
                ).initial(0f).step(1f).build()
        ).toList();
        Component message = Component.text(
                "Set the priority of each characteristic as a tiebreaker. Higher priority = used to tiebreak first.");
        return DialogBase.builder(Component.text("Set Priorities"))
                .body(List.of(DialogBody.plainMessage(message)))
                .inputs(directions)
                .build();
    }

    @NotNull
    private static DialogActionCallback applyShopAttributeSortDirections(@NotNull FindState findState,
                                                                         @NotNull Supplier<Dialog> prevDialog) {
        return (view, audience) -> {
            for (ShopAttribute attribute : ShopAttribute.values()) {
                String option = view.getText(attribute.name());
                if (option == null || option.equals("disabled")) {
                    findState.clearShopAttributeMeta(attribute);
                } else if (option.equals("ascending")) {
                    findState.getOrCreate(attribute).sortDirection(SortDirection.ASCENDING);
                } else if (option.equals("descending")) {
                    findState.getOrCreate(attribute).sortDirection(SortDirection.DESCENDING);
                } else {
                    findState.clearShopAttributeMeta(attribute);
                }
            }
            audience.showDialog(prevDialog.get());
        };
    }

    @NotNull
    private static DialogActionCallback applyShopAttributeSortPriority(@NotNull FindState findState,
                                                                       @NotNull Supplier<Dialog> prevDialog) {
        return (view, audience) -> {

            for (ShopAttribute attribute : ShopAttribute.values()) {
                Float value = view.getFloat(attribute.name());
                if (value == null) {
                    continue;
                }
                findState.setSortPriority(attribute, Math.round(value));
            }
            audience.showDialog(prevDialog.get());
        };
    }

}
