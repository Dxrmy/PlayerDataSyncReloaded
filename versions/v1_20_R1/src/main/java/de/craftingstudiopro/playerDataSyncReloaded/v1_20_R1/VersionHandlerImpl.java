package de.craftingstudiopro.playerDataSyncReloaded.v1_20_R1;

import de.craftingstudiopro.playerDataSyncReloaded.api.PlayerData;
import de.craftingstudiopro.playerDataSyncReloaded.api.PDSPlayer;
import de.craftingstudiopro.playerDataSyncReloaded.common.BukkitBaseVersionHandler;
import org.bukkit.entity.Player;

public class VersionHandlerImpl extends BukkitBaseVersionHandler {
    @Override
    public PlayerData capture(PDSPlayer player) {
        return super.capture(player);
    }

    @Override
    public void apply(PDSPlayer player, PlayerData data) {
        super.apply(player, data);
    }

    @Override
    protected double getMaxHealth(Player player) {
        org.bukkit.attribute.AttributeInstance attr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        return attr != null ? attr.getBaseValue() : 20.0;
    }
}
