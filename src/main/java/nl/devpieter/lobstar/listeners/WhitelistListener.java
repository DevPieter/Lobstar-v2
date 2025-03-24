package nl.devpieter.lobstar.listeners;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.enums.ServerType;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.managers.WhitelistManager;
import nl.devpieter.lobstar.models.Server;
import nl.devpieter.lobstar.models.whitelist.WhitelistEntry;
import nl.devpieter.lobstar.utils.PlayerUtils;
import nl.devpieter.lobstar.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

public class WhitelistListener {

    private final Component denied = Component.text("You are not whitelisted on this server!").color(TextColor.color(0xfe3f3f));
//    private final Component banned = Component.text("You are banned from this server!").color(TextColor.color(0xfe3f3f));
//    private final Component pending = Component.text("Your whitelist request is pending. Please check back later.").color(TextColor.color(0xfe3f3f));

    private final Component error = Component.text("An error occurred while checking if you can join this server!").color(TextColor.color(0xfe3f3f));
    private final Component noLobby = Component.text("No lobby servers available to redirect you to.").color(TextColor.color(0xfe3f3f));

    private final Lobstar lobstar = Lobstar.getInstance();
    private final Logger logger = lobstar.getLogger();

    private final ServerManager serverManager = lobstar.getServerManager();
    private final WhitelistManager whitelistManager = lobstar.getWhitelistManager();

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        logger.info("<Global> Checking whitelist status for {}", player.getUsername());

        try {
            WhitelistEntry entry = whitelistManager.getWhitelistEntry(player.getUniqueId()).join();
            if (entry == null) {
                logger.info("<Global> {} tried to join but no whitelist entry found", player.getUsername());
                event.setResult(ResultedEvent.ComponentResult.denied(denied));
                return;
            }

            // TODO - Check ban status

            if (!entry.isWhitelisted()) {
                logger.info("<Global> {} tried to join but is not whitelisted", player.getUsername());
                event.setResult(ResultedEvent.ComponentResult.denied(denied));
            }
        } catch (Exception e) {
            logger.error("<Global> An error occurred while checking whitelist status for {}", player.getUsername(), e);
            event.setResult(ResultedEvent.ComponentResult.denied(error));
        }
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        logger.info("Choosing initial lobby server for {}", event.getPlayer().getUsername());

        RegisteredServer lobbyServer = getLobbyServer(event.getPlayer());
        event.setInitialServer(lobbyServer);
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        // We can skip the whitelist check if the player doesn't have a previous server,
        // this is already checked in PlayerChooseInitialServerEvent
        if (event.getPreviousServer() == null) return;

        // TODO - Check if already checking whitelist, for when player spams commands

        Player player = event.getPlayer();
        var name = event.getOriginalServer().getServerInfo().getName();

        Server server = serverManager.getServer(name);
        if (server == null) {
            logger.error("Server {} not found", name);

            PlayerUtils.sendErrorMessage(player, "Server not found, please try again later!");
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        logger.info("<Server> Checking whitelist status for {} on {}", player.getUsername(), name);
        PlayerUtils.sendWhisperMessage(player, String.format("Checking your whitelist status on %s...", server.displayName()));

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) {
            logger.error("Server {} not registered", name);

            PlayerUtils.sendErrorMessage(player, "Server not registered, please try again later!");
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        if (!ServerUtils.isOnline(registeredServer)) {
            logger.warn("Server {} not online", name);

            PlayerUtils.sendErrorMessage(player, "Server seems to be offline, please try again later!");
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        if (!server.isWhitelistEnabled()) {
            logger.info("<Server> Server {} does not have its whitelist enabled, allowing {} to join", name, player.getUsername());
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(registeredServer));
            return;
        }

        try {
            WhitelistEntry entry = whitelistManager.getWhitelistEntry(server.id(), player.getUniqueId()).join();
            if (entry == null) {
                logger.info("<Server> {} tried to join {} but no whitelist entry found", player.getUsername(), server.name());

                player.sendMessage(error);
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }

            // TODO - Check ban status

            if (!entry.isWhitelisted()) {
                logger.info("<Server> {} tried to join {} but is not whitelisted", player.getUsername(), server.name());

                player.sendMessage(denied);
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }

            logger.info("<Server> Allowing {} to join {}", player.getUsername(), server.name());

            PlayerUtils.sendWhisperMessage(player, String.format("Sending you to %s...", server.displayName()));
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(registeredServer));
        } catch (Exception e) {
            logger.error("<Server> An error occurred while checking whitelist status for {} on {}", player.getUsername(), server.name(), e);

            player.sendMessage(error);
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }

    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent event) {
        logger.info("{} was kicked, redirecting to lobby", event.getPlayer().getUsername());

        RegisteredServer lobbyServer = getLobbyServer(event.getPlayer());
        if (lobbyServer == null) event.setResult(KickedFromServerEvent.Notify.create(noLobby));
        else event.setResult(KickedFromServerEvent.RedirectPlayer.create(lobbyServer));
    }

    private @Nullable RegisteredServer getLobbyServer(@NotNull Player player) {
        List<Server> lobbyServers = serverManager.getServers(ServerType.Lobby);
        if (lobbyServers.isEmpty()) {
            logger.warn("No lobby servers registered");
            return null;
        }

        // We prioritize non-whitelisted servers over whitelisted servers to avoid having to check the whitelist for every player
        for (var server : lobbyServers.stream().filter(s -> !s.isWhitelistEnabled()).toList()) {
            RegisteredServer registeredServer = server.findRegisteredServer();
            if (registeredServer == null || !ServerUtils.isOnline(registeredServer)) continue;

            return registeredServer;
        }

        for (var server : lobbyServers.stream().filter(Server::isWhitelistEnabled).toList()) {
            RegisteredServer registeredServer = server.findRegisteredServer();
            if (registeredServer == null || !ServerUtils.isOnline(registeredServer)) continue;

            try {
                WhitelistEntry entry = whitelistManager.getWhitelistEntry(server.id(), player.getUniqueId()).join();
                if (entry == null) {
                    logger.info("No whitelist entry found for {} on {}", player.getUsername(), server.name());
                    continue;
                }

                // TODO - Check ban status

                if (entry.isWhitelisted()) return registeredServer;
            } catch (Exception e) {
                logger.error("An error occurred while checking whitelist status for {} on {}", player.getUsername(), server.name(), e);
            }
        }

        return null;
    }
}
