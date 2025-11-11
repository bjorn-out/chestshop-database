package io.github.md5sha256.chestshopdatabase.preview;

import io.github.md5sha256.chestshopdatabase.model.HydratedShop;
import io.github.md5sha256.chestshopdatabase.util.BlockPosition;
import io.github.md5sha256.chestshopdatabase.util.ChunkPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PreviewHandler {

    private final Map<ChunkPosition, Map<BlockPosition, ItemDisplay>> displayEntities = new HashMap<>();

    private Optional<ItemDisplay> getExistingDisplay(@NotNull BlockPosition position) {
        ChunkPosition chunkPos = position.chunkPosition();
        Map<BlockPosition, ItemDisplay> map = this.displayEntities.get(chunkPos);
        if (map == null) {
            return Optional.empty();
        }
        ItemDisplay display = map.get(position);
        return Optional.ofNullable(display);
    }

    public void renderPreview(@NotNull World world, @NotNull HydratedShop shop) {

        ItemStack item = shop.item().itemStack();
        BlockPosition pos = shop.blockPosition();
        Optional<ItemDisplay> existing = getExistingDisplay(pos);
        if (existing.isPresent()) {
            existing.get().setItemStack(item);
            return;
        }
        Location location = new Location(world, shop.posX() + 0.5, shop.posY() + 1, shop.posZ() + 0.5);
        ItemDisplay spawned = world.spawn(location, ItemDisplay.class, display -> {
            display.setVisibleByDefault(true);
            display.setPersistent(false);
            display.setItemStack(item);
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
            display.setTransformationMatrix(new Matrix4f()
                    .scale(0.5f));
            display.setBillboard(Display.Billboard.CENTER);
        });
        displayEntities.computeIfAbsent(pos.chunkPosition(), x -> new HashMap<>())
                .put(pos, spawned);
    }


    public void destroyPreview(@NotNull BlockPosition position) {
        ChunkPosition chunkPos = position.chunkPosition();
        Map<BlockPosition, ItemDisplay> map = this.displayEntities.get(chunkPos);
        if (map == null) {
            return;
        }
        ItemDisplay display = map.remove(position);
        if (display == null) {
            return;
        }
        display.remove();
        if (map.isEmpty()) {
            this.displayEntities.remove(chunkPos);
        }
    }

    public void destroyPreviews(@NotNull ChunkPosition chunkPosition) {
        Map<BlockPosition, ItemDisplay> map = this.displayEntities.remove(chunkPosition);
        if (map == null) {
            return;
        }
        map.values().forEach(ItemDisplay::remove);
    }

}
