package de.craftingstudiopro.playerDataSyncReloaded.fabric;

import de.craftingstudiopro.playerDataSyncReloaded.api.PDSPlayer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class FabricPDSPlayer implements PDSPlayer {
    private final ServerPlayer player;

    public FabricPDSPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUUID();
    }

    @Override
    public String getName() {
        return player.getName().getString();
    }

    @Override
    public Object getHandle() {
        return player;
    }

    @Override
    public String getWorldName() {
        try {
            Object serverLevel;
            try {
                java.lang.reflect.Method m = player.getClass().getMethod("level");
                serverLevel = m.invoke(player);
            } catch (NoSuchMethodException e) {
                java.lang.reflect.Method m = player.getClass().getMethod("serverLevel");
                serverLevel = m.invoke(player);
            }
            java.lang.reflect.Method dimMethod = serverLevel.getClass().getMethod("dimension");
            Object dimKey = dimMethod.invoke(serverLevel);
            java.lang.reflect.Method locMethod = dimKey.getClass().getMethod("location");
            Object loc = locMethod.invoke(dimKey);
            return loc.toString();
        } catch (Exception e) {
            return "minecraft:overworld";
        }
    }
}
