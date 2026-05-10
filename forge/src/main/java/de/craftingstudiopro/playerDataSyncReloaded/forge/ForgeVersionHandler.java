package de.craftingstudiopro.playerDataSyncReloaded.forge;

import de.craftingstudiopro.playerDataSyncReloaded.api.PDSPlayer;
import de.craftingstudiopro.playerDataSyncReloaded.api.PlayerData;
import de.craftingstudiopro.playerDataSyncReloaded.api.VersionHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

public class ForgeVersionHandler implements VersionHandler {
    private List<String> itemExclusions;

    @Override
    public PlayerData capture(PDSPlayer pdsPlayer) {
        ServerPlayer player = (ServerPlayer) pdsPlayer.getHandle();
        PlayerData data = new PlayerData();
        data.uuid = player.getUUID();
        data.name = player.getGameProfile().getName();

        data.health = player.getHealth();
        data.foodLevel = player.getFoodData().getFoodLevel();
        data.level = player.experienceLevel;
        data.exp = player.experienceProgress;
        
        data.inventoryContents = serializeInventory(pdsPlayer);
        data.gameMode = player.gameMode.getGameModeForPlayer().name();

        return data;
    }

    @Override
    public void apply(PDSPlayer pdsPlayer, PlayerData data) {
        ServerPlayer player = (ServerPlayer) pdsPlayer.getHandle();
        player.setHealth((float) data.health);
        player.getFoodData().setFoodLevel(data.foodLevel);
        player.experienceLevel = data.level;
        player.experienceProgress = data.exp;

        if (data.inventoryContents != null) {
            deserializeInventory(pdsPlayer, data.inventoryContents);
        }
    }

    @Override
    public String serializeInventory(PDSPlayer pdsPlayer) {
        ServerPlayer player = (ServerPlayer) pdsPlayer.getHandle();
        try {
            CompoundTag nbt = new CompoundTag();
            player.getInventory().save(nbt.getList("Inventory", 10));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(nbt, bos);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void deserializeInventory(PDSPlayer pdsPlayer, String inventory) {
        ServerPlayer player = (ServerPlayer) pdsPlayer.getHandle();
        try {
            byte[] bytes = Base64.getDecoder().decode(inventory);
            CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
            player.getInventory().load(nbt.getList("Inventory", 10));
        } catch (Exception ignored) {}
    }

    @Override
    public void setItemExclusions(List<String> materials) {
        this.itemExclusions = materials;
    }
}
