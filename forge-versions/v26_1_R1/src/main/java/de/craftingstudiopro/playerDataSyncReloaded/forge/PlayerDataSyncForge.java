package de.craftingstudiopro.playerDataSyncReloaded.forge;

import de.craftingstudiopro.playerDataSyncReloaded.common.SyncManager;
import de.craftingstudiopro.playerDataSyncReloaded.common.storage.SqlStorage;
import de.craftingstudiopro.playerDataSyncReloaded.common.storage.Storage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("playerdatasync")
public class PlayerDataSyncForge {
    private static final Logger LOGGER = LogManager.getLogger();

    private SyncManager syncManager;
    private Storage storage;
    private ForgePlatform platform;
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath("pds", "sync"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public PlayerDataSyncForge() {
        MinecraftForge.EVENT_BUS.register(this);

        CHANNEL.registerMessage(0, String.class, (msg, buf) -> buf.writeUtf(msg), buf -> buf.readUtf(), (msg, ctx) -> {
            ctx.get().enqueueWork(() -> {
                if (syncManager == null) {
                    return;
                }
                var player = ctx.get().getSender();
                if (player != null) {
                    if (msg.startsWith("save:")) {
                        syncManager.handleQuit(new ForgePDSPlayer(player));
                    } else if (msg.startsWith("load:")) {
                        syncManager.handleJoin(new ForgePDSPlayer(player));
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        this.platform = new ForgePlatform();
        this.storage = new SqlStorage(platform.getLogger(), "sqlite", "", 0, "playerdata.db", "", "");
        try {
            this.storage.init();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize storage", e);
        }
        this.syncManager = new SyncManager(platform, storage, new ForgeVersionHandler());
        platform.getLogger().info("PlayerDataSync Forge initialized!");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (syncManager != null && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            syncManager.handleJoin(new ForgePDSPlayer(player));
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (syncManager != null && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            syncManager.handleQuit(new ForgePDSPlayer(player));
        }
    }
}
