package io.github.md5sha256.chestshopdatabase.listener;

import io.github.md5sha256.chestshopdatabase.preview.PreviewHandler;
import io.github.md5sha256.chestshopdatabase.util.ChunkPosition;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.NotNull;

public record PreviewListener(
        @NotNull PreviewHandler handler
) implements Listener {

    @EventHandler
    public void onChunkLoad(@NotNull ChunkLoadEvent event) {
        this.handler.loadPreviewsInChunk(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        ChunkPosition chunkPos = new ChunkPosition(event.getWorld().getUID(),
                chunk.getX(),
                chunk.getZ());
        this.handler.destroyPreviews(chunkPos);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(PlayerJoinEvent event) {
        this.handler.loadVisibility(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogout(PlayerQuitEvent event) {
        this.handler.clearCache(event.getPlayer().getUniqueId());
    }

}
