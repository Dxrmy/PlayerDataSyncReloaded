package de.craftingstudiopro.playerDataSyncReloaded.api;

import java.util.UUID;

public interface PDSPlayer {
    UUID getUniqueId();
    String getName();
    Object getHandle(); // The original player object (Bukkit Player, ServerPlayer, etc.)
}
