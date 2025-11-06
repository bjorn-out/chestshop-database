package io.github.md5sha256.chestshopdatabase.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class InventoryUtil {

    public static int countItems(@NotNull ItemStack itemStack, @NotNull Inventory inventory) {
        Iterator<ItemStack> iterator = inventory.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (item != null && !item.isEmpty() && item.isSimilar(itemStack)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public static int remainingCapacity(@NotNull ItemStack itemStack, @NotNull Inventory inventory) {
        int stackSize = Math.min(itemStack.getMaxStackSize(), inventory.getMaxStackSize());
        Iterator<ItemStack> iterator = inventory.iterator();
        int capacity = 0;
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (item == null || item.isEmpty()) {
                capacity += stackSize;
            } else if (item.isSimilar(itemStack)) {
                capacity += stackSize - item.getAmount();
            }
        }
        return capacity;
    }

}
