package de.craftingstudiopro.playerDataSyncReloaded.fabric;

import de.craftingstudiopro.playerDataSyncReloaded.api.PDSPlayer;
import de.craftingstudiopro.playerDataSyncReloaded.api.PlayerData;
import de.craftingstudiopro.playerDataSyncReloaded.api.VersionHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.server.level.ServerPlayer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

public class FabricVersionHandler implements VersionHandler {
    private static final int NBT_COMPOUND_ID = 10;
    private List<String> itemExclusions;

    @Override
    public PlayerData capture(PDSPlayer pdsPlayer) {
        ServerPlayer player = (ServerPlayer) pdsPlayer.getHandle();
        PlayerData data = new PlayerData();
        data.uuid = player.getUUID();
        data.name = player.getName().getString();

        // Stats
        data.health = player.getHealth();
        data.foodLevel = player.getFoodData().getFoodLevel();
        data.saturation = player.getFoodData().getSaturationLevel();
        data.exp = player.experienceProgress;
        data.level = player.experienceLevel;
        data.totalExperience = player.totalExperience;

        // Inventory
        data.inventoryContents = serializeInventory(pdsPlayer);

        // GameMode
        data.gameMode = player.gameMode.getGameModeForPlayer().name();

        return data;
    }

    @Override
    public void apply(PDSPlayer pdsPlayer, PlayerData data) {
        ServerPlayer player = (ServerPlayer) pdsPlayer.getHandle();

        player.setHealth((float) data.health);
        player.getFoodData().setFoodLevel(data.foodLevel);
        player.experienceProgress = data.exp;
        player.experienceLevel = data.level;
        player.totalExperience = data.totalExperience;

        if (data.inventoryContents != null) {
            deserializeInventory(pdsPlayer, data.inventoryContents);
        }
    }

    @Override
    public String serializeInventory(PDSPlayer pdsPlayer) {
        ServerPlayer player = (ServerPlayer) pdsPlayer.getHandle();
        try {
            ListTag list = new ListTag();
            player.getInventory().save(list);
            CompoundTag root = new CompoundTag();
            root.put("Inventory", list);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(root, bos);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deserializeInventory(PDSPlayer pdsPlayer, String inventory) {
        ServerPlayer player = (ServerPlayer) pdsPlayer.getHandle();
        try {
            byte[] bytes = Base64.getDecoder().decode(inventory);
            CompoundTag root = NbtIo.readCompressed(new ByteArrayInputStream(bytes), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            ListTag list = root.getList("Inventory", NBT_COMPOUND_ID);
            player.getInventory().load(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setItemExclusions(List<String> materials) {
        this.itemExclusions = materials;
    }
}
