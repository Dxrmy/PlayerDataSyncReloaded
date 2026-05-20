package de.craftingstudiopro.playerDataSyncReloaded.plugin;

import de.craftingstudiopro.playerDataSyncReloaded.api.PDSPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitPDSPlayer implements PDSPlayer {
    private final Player player;

    public BukkitPDSPlayer(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public Object getHandle() {
        return player;
    }

    @Override
    public String getWorldName() {
        return player.getWorld().getName();
    }
}
