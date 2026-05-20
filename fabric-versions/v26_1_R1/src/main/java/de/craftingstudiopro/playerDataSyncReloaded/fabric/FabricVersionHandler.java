package de.craftingstudiopro.playerDataSyncReloaded.fabric;

import de.craftingstudiopro.playerDataSyncReloaded.api.PDSPlayer;
import de.craftingstudiopro.playerDataSyncReloaded.api.PlayerData;
import de.craftingstudiopro.playerDataSyncReloaded.api.VersionHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

public class FabricVersionHandler implements VersionHandler {
    private List<String> itemExclusions;

    @Override
    public PlayerData capture(PDSPlayer pdsPlayer) {
        ServerPlayerEntity player = (ServerPlayerEntity) pdsPlayer.getHandle();
        PlayerData data = new PlayerData();
        data.uuid = player.getUuid();
        data.name = player.getName().getString();

        // Stats
        data.health = player.getHealth();
        data.foodLevel = player.getHungerManager().getFoodLevel();
        data.saturation = player.getHungerManager().getSaturationLevel();
        data.exp = player.experienceProgress;
        data.level = player.experienceLevel;
        data.totalExperience = player.totalExperience;

        // Inventory
        data.inventoryContents = serializeInventory(pdsPlayer);

        // GameMode
        data.gameMode = player.interactionManager.getGameMode().name();

        return data;
    }

    @Override
    public void apply(PDSPlayer pdsPlayer, PlayerData data) {
        ServerPlayerEntity player = (ServerPlayerEntity) pdsPlayer.getHandle();

        player.setHealth((float) data.health);
        player.getHungerManager().setFoodLevel(data.foodLevel);
        player.experienceProgress = data.exp;
        player.experienceLevel = data.level;
        player.totalExperience = data.totalExperience;

        if (data.inventoryContents != null) {
            deserializeInventory(pdsPlayer, data.inventoryContents);
        }
    }

    @Override
    public String serializeInventory(PDSPlayer pdsPlayer) {
        ServerPlayerEntity player = (ServerPlayerEntity) pdsPlayer.getHandle();
        try {
            NbtCompound nbt = new NbtCompound();
            player.getInventory().writeNbt(nbt);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(nbt, bos);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deserializeInventory(PDSPlayer pdsPlayer, String inventory) {
        ServerPlayerEntity player = (ServerPlayerEntity) pdsPlayer.getHandle();
        try {
            byte[] bytes = Base64.getDecoder().decode(inventory);
            NbtCompound nbt = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
            player.getInventory().readNbt(nbt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setItemExclusions(List<String> materials) {
        this.itemExclusions = materials;
    }
}
