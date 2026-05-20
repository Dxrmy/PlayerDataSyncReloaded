package de.craftingstudiopro.playerDataSyncReloaded.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;

@Plugin(
        id = "playerdatasync",
        name = "PlayerDataSync Velocity",
        version = PluginBuildInfo.VERSION,
        authors = {"CraftingStudioPro"}
)
public class PlayerDataSyncVelocity {

    private final ProxyServer server;
    private final Logger logger;
    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("pds:sync");

    @Inject
    public PlayerDataSyncVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(IDENTIFIER);
        logger.info("PlayerDataSync Velocity Integration initialized.");
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        // When a player is switching servers, we notify the source server to save data immediately
        event.getPlayer().getCurrentServer().ifPresent(serverConnection -> {
            byte[] data = ("save:" + event.getPlayer().getUniqueId()).getBytes(StandardCharsets.UTF_8);
            serverConnection.sendPluginMessage(IDENTIFIER, data);
            logger.info("Triggered fast-save for " + event.getPlayer().getUsername() + " on " + serverConnection.getServerInfo().getName());
        });
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        // Notify the target server that the player has connected and data should be ready or loaded
        byte[] data = ("load:" + event.getPlayer().getUniqueId()).getBytes(StandardCharsets.UTF_8);
        event.getServer().sendPluginMessage(IDENTIFIER, data);
        logger.info("Notified " + event.getServer().getServerInfo().getName() + " of connection for " + event.getPlayer().getUsername());
    }
}
