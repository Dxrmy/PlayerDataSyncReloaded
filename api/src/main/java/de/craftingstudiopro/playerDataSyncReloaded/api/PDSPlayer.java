package de.craftingstudiopro.playerDataSyncReloaded.api;

import java.util.UUID;

public interface PDSPlayer {
    UUID getUniqueId();
    String getName();
    Object getHandle(); // The original player object (Bukkit Player, ServerPlayer, etc.)

    /**
     * Optional world/dimension identifier for exclusion checks.
     * Implementations may return an empty string if unavailable.
     */
    default String getWorldName() {
        return "";
    }
}
