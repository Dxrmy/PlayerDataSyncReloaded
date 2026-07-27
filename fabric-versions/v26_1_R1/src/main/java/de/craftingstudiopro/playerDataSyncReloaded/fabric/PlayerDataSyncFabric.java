package de.craftingstudiopro.playerDataSyncReloaded.fabric;

import de.craftingstudiopro.playerDataSyncReloaded.common.SyncManager;
import de.craftingstudiopro.playerDataSyncReloaded.common.storage.SqlStorage;
import de.craftingstudiopro.playerDataSyncReloaded.common.storage.Storage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerDataSyncFabric implements ModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger("PlayerDataSync");

    private SyncManager syncManager;
    private Storage storage;
    private FabricPlatform platform;

    @Override
    public void onInitialize() {
        try {
            try {
                PayloadTypeRegistry.playC2S().register(PdsSyncC2SPayload.PACKET_ID, PdsSyncC2SPayload.CODEC);
            } catch (NoSuchMethodError e) {
                java.lang.reflect.Method m = PayloadTypeRegistry.class.getMethod("serverboundPlay");
                Object registry = m.invoke(null);
                java.lang.reflect.Method reg = null;
                for (java.lang.reflect.Method method : registry.getClass().getMethods()) {
                    if (method.getName().equals("register") && method.getParameterCount() == 2) {
                        reg = method;
                        break;
                    }
                }
                if (reg != null) {
                    reg.invoke(registry, PdsSyncC2SPayload.PACKET_ID, PdsSyncC2SPayload.CODEC);
                }
            }
        } catch (Exception ex) {
            LOG.error("Failed to register custom payload via reflection", ex);
        }
        ServerPlayNetworking.registerGlobalReceiver(PdsSyncC2SPayload.PACKET_ID, (payload, context) -> {
            String msg = payload.message();
            MinecraftServer server = context.player().getServer();
            if (server == null) {
                return;
            }
            server.execute(() -> {
                if (syncManager == null) {
                    return;
                }
                if (msg.startsWith("save:")) {
                    syncManager.handleQuit(new FabricPDSPlayer(context.player()));
                } else if (msg.startsWith("load:")) {
                    syncManager.handleJoin(new FabricPDSPlayer(context.player()));
                }
            });
        });

        ServerLifecycleEvents.SERVER_STARTING.register(this::setup);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (syncManager != null) {
                syncManager.handleJoin(new FabricPDSPlayer(handler.player));
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (syncManager != null) {
                syncManager.handleQuit(new FabricPDSPlayer(handler.player));
            }
        });
    }

    private void setup(MinecraftServer server) {
        this.platform = new FabricPlatform(server);

        this.storage = new SqlStorage(platform.getLogger(), "mariadb", "localhost", 3306, "minecraft", "root", "");
        try {
            this.storage.init();
        } catch (Exception e) {
            LOG.error("Failed to initialize storage", e);
        }

        this.syncManager = new SyncManager(platform, storage, new FabricVersionHandler());
        platform.getLogger().info("PlayerDataSync Fabric initialized!");
    }
}
