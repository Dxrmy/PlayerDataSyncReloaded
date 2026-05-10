package de.craftingstudiopro.playerDataSyncReloaded.forge;

import de.craftingstudiopro.playerDataSyncReloaded.common.SyncManager;
import de.craftingstudiopro.playerDataSyncReloaded.common.storage.SqlStorage;
import de.craftingstudiopro.playerDataSyncReloaded.common.storage.Storage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.resources.ResourceLocation;

@Mod("playerdatasync")
public class PlayerDataSyncForge {
    private SyncManager syncManager;
    private Storage storage;
    private ForgePlatform platform;
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("pds", "sync"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public PlayerDataSyncForge() {
        MinecraftForge.EVENT_BUS.register(this);
        
        CHANNEL.registerMessage(0, String.class, (msg, buf) -> buf.writeUtf(msg), buf -> buf.readUtf(), (msg, ctx) -> {
            ctx.get().enqueueWork(() -> {
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
            e.printStackTrace();
        }
        this.syncManager = new SyncManager(platform, storage, new ForgeVersionHandler());
        platform.getLogger().info("PlayerDataSync Forge initialized!");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            syncManager.handleJoin(new ForgePDSPlayer(player));
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            syncManager.handleQuit(new ForgePDSPlayer(player));
        }
    }
}
