package de.craftingstudiopro.playerDataSyncReloaded.common;

import de.craftingstudiopro.playerDataSyncReloaded.api.PDSPlayer;
import de.craftingstudiopro.playerDataSyncReloaded.api.PlayerData;
import de.craftingstudiopro.playerDataSyncReloaded.api.VersionHandler;
import de.craftingstudiopro.playerDataSyncReloaded.common.redis.RedisManager;
import de.craftingstudiopro.playerDataSyncReloaded.common.storage.Storage;
import de.craftingstudiopro.playerDataSyncReloaded.common.util.DiscordWebhookManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SyncManager {
    private final Platform platform;
    private final Storage storage;
    private final VersionHandler versionHandler;
    private final Logger logger;
    private final ConcurrentHashMap<UUID, Boolean> syncInProgress = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> inventoryHashes = new ConcurrentHashMap<>();
    private RedisManager redisManager;
    private DiscordWebhookManager discordManager;
    private double economyPlaceholder = 0; // Simple placeholder if platform doesn't handle economy

    public SyncManager(Platform platform, Storage storage, VersionHandler versionHandler) {
        this.platform = platform;
        this.storage = storage;
        this.versionHandler = versionHandler;
        this.logger = platform.getLogger();
        refreshExclusions();
    }

    public void refreshExclusions() {
        this.versionHandler.setItemExclusions(platform.getConfigStringList("exclusions.items"));
    }

    public void setRedisManager(RedisManager redisManager) {
        this.redisManager = redisManager;
        this.redisManager.subscribe(message -> {
            if (message.startsWith("saved:")) {
                String uuidStr = message.substring(6);
                UUID uuid = UUID.fromString(uuidStr);
                // Implementation depends on platform to find player and trigger handleJoin
                // For now, we'll assume the platform handles the event or we need a way to find PDSPlayer
            }
        });
    }

    public void setDiscordManager(DiscordWebhookManager discordManager) {
        this.discordManager = discordManager;
    }

    public void handleJoin(PDSPlayer player) {
        syncInProgress.put(player.getUniqueId(), true);
        
        String syncStarted = platform.getConfigString("messages.sync_started", "&7Syncing your data...");
        if (!syncStarted.isEmpty()) {
            platform.sendMessage(player.getUniqueId(), syncStarted);
        }

        long startTime = System.currentTimeMillis();
        storage.load(player.getUniqueId()).thenAccept(optionalData -> {
            if (optionalData.isPresent()) {
                PlayerData data = optionalData.get();
                platform.runTask(() -> applyData(player, data, startTime));
            } else {
                syncInProgress.remove(player.getUniqueId());
            }
        }).exceptionally(ex -> {
            syncInProgress.remove(player.getUniqueId());
            String syncFailed = platform.getConfigString("messages.sync_failed", "&cFailed to sync your data. Please contact an admin.");
            if (!syncFailed.isEmpty()) {
                platform.sendMessage(player.getUniqueId(), syncFailed);
            }
            logger.severe("Failed to load data for " + player.getName() + ": " + ex.getMessage());
            
            if (discordManager != null && platform.getConfigBoolean("discord.events.sync_failed", true)) {
                discordManager.sendSyncFailed(player.getName(), ex.getMessage());
            }
            return null;
        });
    }

    private void applyData(PDSPlayer player, PlayerData data, long startTime) {
        if (platform.isOnline(player.getUniqueId())) {
            versionHandler.apply(player, data);
            
            if (data.inventoryContents != null) {
                inventoryHashes.put(player.getUniqueId(), data.inventoryContents.hashCode());
            }

            long duration = System.currentTimeMillis() - startTime;
            
            String syncComplete = platform.getConfigString("messages.sync_complete", "&aData synced successfully!");
            if (!syncComplete.isEmpty()) {
                platform.sendMessage(player.getUniqueId(), syncComplete);
            }
            logger.info("Successfully synced data for player: " + player.getName() + " (" + duration + "ms)");
            
            if (discordManager != null && platform.getConfigBoolean("discord.events.sync_success", true)) {
                discordManager.sendSyncSuccess(player.getName(), duration);
            }
        }
        syncInProgress.remove(player.getUniqueId());
    }

    public void handleQuit(PDSPlayer player) {
        handleQuit(player, false);
    }

    public void handleQuit(PDSPlayer player, boolean isAutosave) {
        if (isSyncInProgress(player.getUniqueId())) {
             logger.warning("Player " + player.getName() + " quit while sync was still in progress!");
        }

        PlayerData data = versionHandler.capture(player);
        filterData(data); // Apply config filters
        
        if (data.inventoryContents != null) {
            int currentHash = data.inventoryContents.hashCode();
            Integer lastHash = inventoryHashes.remove(player.getUniqueId());
            if (lastHash != null && lastHash == currentHash && !platform.getConfigBoolean("sync.force_save_on_quit", false)) {
                return;
            }
        } else {
            inventoryHashes.remove(player.getUniqueId());
        }

        storage.save(data).thenRun(() -> {
            if (!isAutosave) {
                logger.info("Saved data for player: " + player.getName());
                if (redisManager != null) {
                    redisManager.publish("saved:" + player.getUniqueId().toString());
                }
            }
        });
    }

    private void filterData(PlayerData data) {
        if (!platform.getConfigBoolean("sync.inventory", true)) data.inventoryContents = null;
        if (!platform.getConfigBoolean("sync.ender_chest", true)) data.enderChestContents = null;
        if (!platform.getConfigBoolean("sync.health", true)) data.health = 20.0;
        if (!platform.getConfigBoolean("sync.experience", true)) {
            data.exp = 0;
            data.level = 0;
            data.totalExperience = 0;
        }
        if (!platform.getConfigBoolean("sync.potion_effects", true)) data.potionEffects = null;
        if (!platform.getConfigBoolean("sync.food", true)) {
            data.foodLevel = 20;
            data.saturation = 5.0f;
        }
        if (!platform.getConfigBoolean("sync.game_mode", true)) data.gameMode = "SURVIVAL";
        if (!platform.getConfigBoolean("sync.advancements", true)) data.advancements = null;
        if (!platform.getConfigBoolean("sync.statistics", true)) data.statistics = null;
        if (!platform.getConfigBoolean("sync.air_level", true)) data.airLevel = 300;
        if (!platform.getConfigBoolean("sync.fire_ticks", true)) data.fireTicks = 0;
        if (!platform.getConfigBoolean("sync.player_time", true)) data.playerTime = -1;
        if (!platform.getConfigBoolean("sync.player_weather", true)) data.playerWeather = null;
        if (!platform.getConfigBoolean("sync.freeze_ticks", true)) data.freezeTicks = 0;
        if (!platform.getConfigBoolean("sync.arrows_in_body", true)) data.arrowsInBody = 0;
        if (!platform.getConfigBoolean("sync.absorption", true)) data.absorptionAmount = 0.0;
        if (!platform.getConfigBoolean("sync.speeds", true)) {
            data.walkSpeed = 0.2f;
            data.flySpeed = 0.1f;
        }
        if (!platform.getConfigBoolean("sync.fall_distance", true)) data.fallDistance = 0.0f;
    }

    public boolean isSyncInProgress(UUID uuid) {
        return syncInProgress.getOrDefault(uuid, false);
    }
}
