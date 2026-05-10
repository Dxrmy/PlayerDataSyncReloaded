package de.craftingstudiopro.playerDataSyncReloaded.fabric;

import de.craftingstudiopro.playerDataSyncReloaded.common.SyncManager;
import de.craftingstudiopro.playerDataSyncReloaded.common.storage.SqlStorage;
import de.craftingstudiopro.playerDataSyncReloaded.common.storage.Storage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class PlayerDataSyncFabric implements ModInitializer {
    private SyncManager syncManager;
    private Storage storage;
    private FabricPlatform platform;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            setup(server);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            syncManager.handleJoin(new FabricPDSPlayer(handler.player));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            syncManager.handleQuit(new FabricPDSPlayer(handler.player));
        });

        Identifier syncId = new Identifier("pds", "sync");
        ServerPlayNetworking.registerGlobalReceiver(syncId, (server, player, handler, buf, responseSender) -> {
            String msg = buf.readString();
            server.execute(() -> {
                if (msg.startsWith("save:")) {
                    syncManager.handleQuit(new FabricPDSPlayer(player));
                } else if (msg.startsWith("load:")) {
                    syncManager.handleJoin(new FabricPDSPlayer(player));
                }
            });
        });
    }

    private void setup(MinecraftServer server) {
        this.platform = new FabricPlatform(server);
        
        // Use a simple SQLite storage for Fabric by default if no config
        this.storage = new SqlStorage(platform.getLogger(), "sqlite", "", 0, "playerdata.db", "", "");
        try {
            this.storage.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.syncManager = new SyncManager(platform, storage, new FabricVersionHandler());
        platform.getLogger().info("PlayerDataSync Fabric initialized!");
    }
}
