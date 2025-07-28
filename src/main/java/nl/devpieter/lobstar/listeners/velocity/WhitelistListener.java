package nl.devpieter.lobstar.listeners.velocity;

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
import nl.devpieter.lobstar.helpers.ServerHelper;
import nl.devpieter.lobstar.helpers.WhitelistHelper;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.managers.ServerTypeManager;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.models.serverType.ServerType;
import nl.devpieter.lobstar.utils.PlayerUtils;
import nl.devpieter.lobstar.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class WhitelistListener {

    private final Component denied = Component.text("You are not whitelisted on this server!").color(TextColor.color(0xfe3f3f));
    private final Component noLobby = Component.text("No lobby servers available to redirect you to.").color(TextColor.color(0xfe3f3f));

    private final Lobstar lobstar = Lobstar.getInstance();
    private final Logger logger = this.lobstar.getLogger();

    private final ServerManager serverManager = ServerManager.getInstance();
    private final ServerTypeManager serverTypeManager = ServerTypeManager.getInstance();

    private final ServerHelper serverHelper = ServerHelper.getInstance();
    private final WhitelistHelper whitelistHelper = WhitelistHelper.getInstance();

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        this.logger.info("<Global> Checking whitelist status for {}", player.getUsername());

        if (!this.whitelistHelper.canJoinGlobal(player)) {
            this.logger.info("<Global> {} tried to join but is not whitelisted", player.getUsername());
            event.setResult(ResultedEvent.ComponentResult.denied(this.denied));
            return;
        }

        this.logger.info("<Global> {} is whitelisted, allowing to join", player.getUsername());
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        this.logger.info("<CIS> Choosing initial server for {}", event.getPlayer().getUsername());

        Server server = this.getServerForPlayer(event.getPlayer());
        if (server == null) {
            this.logger.warn("<CIS> No initial server found for {}", event.getPlayer().getUsername());
            return;
        }

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) {
            this.logger.warn("<CIS> Found initial server {} but it is not registered", server.getName());
            return;
        }

        this.logger.info("<CIS> Initial server found for {}, sending to {}", event.getPlayer().getUsername(), server.getName());
        event.setInitialServer(registeredServer);
    }

    private @Nullable Server getServerForPlayer(@NotNull Player player) {
        Server requestedServer = this.serverHelper.tryGetPlayerRequestedServer(player);
        if (requestedServer != null) {
            this.logger.info("<SFP> Found requested server {} for {}", requestedServer.getName(), player.getUsername());
            return requestedServer;
        }

        Server server = this.serverHelper.getAvailableLobbyServer(player);
        if (server == null) {
            this.logger.warn("<SFP> No lobby server found for {}", player.getUsername());
            return null;
        }

        this.logger.info("<SFP> Found lobby server {} for {}", server.getName(), player.getUsername());
        return server;
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        // We can skip the whitelist check if the player doesn't have a previous server,
        // this is already checked in PlayerChooseInitialServerEvent
        if (event.getPreviousServer() == null) return;

        Player player = event.getPlayer();
        String name = event.getOriginalServer().getServerInfo().getName();

        Server server = this.serverManager.getServerByName(name);
        if (server == null) {
            this.logger.error("<Server> Server {} not found", name);

            PlayerUtils.sendErrorMessage(player, "Server not found, please try again later!");
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        this.logger.info("<Server> Checking whitelist status for {} on {}", player.getUsername(), name);
        PlayerUtils.sendWhisperMessage(player, String.format("Checking your whitelist status on %s...", server.getDisplayName()));

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) {
            this.logger.error("<Server> Server {} not registered", name);

            PlayerUtils.sendErrorMessage(player, "Server not registered, please try again later!");
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        if (server.isWhitelistActive() && !this.whitelistHelper.isWhitelisted(player, server)) {
            this.logger.info("<Server> {} tried to join {} but is not whitelisted", player.getUsername(), server.getName());

            PlayerUtils.sendErrorMessage(player, this.denied);
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        boolean isJoinable = server.isJoinable();
        boolean isMaintenance = server.isUnderMaintenance();
        boolean isSuper = this.whitelistHelper.isSuper(player, server);

        if (!isJoinable && !isSuper) {
            this.logger.info("<Server> {} tried to join {} but the server is not joinable", player.getUsername(), server.getName());

            PlayerUtils.sendErrorMessage(player, "Server is not joinable at the moment, please try again later!");
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        if (isMaintenance && !isSuper) {
            this.logger.info("<Server> {} tried to join {} but the server is under maintenance", player.getUsername(), server.getName());

            String message = server.getMaintenanceMessage();
            if (message == null || message.isEmpty()) message = "Server is currently under maintenance, please try again later!";

            PlayerUtils.sendErrorMessage(player, message);
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        if (!ServerUtils.isOnline(registeredServer)) {
            this.logger.warn("<Server> Server {} not online", name);

            PlayerUtils.sendErrorMessage(player, "Server seems to be offline, please try again later!");
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        if (!isJoinable) PlayerUtils.sendWhisperMessage(player, String.format("Server %s is not joinable, bypassing...", server.getDisplayName()));
        if (isMaintenance) PlayerUtils.sendWhisperMessage(player, String.format("Server %s is under maintenance, bypassing...", server.getDisplayName()));

        this.logger.info("<Server> Allowing {} to join {}", player.getUsername(), server.getName());
        PlayerUtils.sendWhisperMessage(player, String.format("Sending you to %s...", server.getDisplayName()));
        event.setResult(ServerPreConnectEvent.ServerResult.allowed(registeredServer));
    }

    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent event) {
        String playerName = event.getPlayer().getUsername();
        this.logger.info("<KFS> {} was kicked, redirecting to lobby", playerName);

        if (event.kickedDuringServerConnect()) {
            this.logger.info("<KFS> {} was kicked during server connect, not redirecting", playerName);
            return;
        }

        Server from = this.serverManager.getServer(event.getServer());

        if (from != null) {
            ServerType serverType = this.serverTypeManager.getServerTypeById(from.getTypeId());
            if (serverType != null && serverType.isLobbyLike()) {
                this.logger.info("<KFS> {} was kicked from a lobby server, not redirecting", playerName);
                return;
            }
        }

        Server server = this.serverHelper.getAvailableLobbyServer(event.getPlayer());
        if (server == null) {
            this.logger.warn("<KFS> No lobby server found for {}", playerName);
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(this.noLobby));
            return;
        }

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) {
            this.logger.warn("<KFS> Lobby server {} not registered", server.getName());
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(this.noLobby));
            return;
        }

        if (event.getServer() == registeredServer) {
            // This should never happen, but just in case
            this.logger.error("<KFS> {} was kicked from the server they were already on, not redirecting", playerName);
            return;
        }

        event.setResult(KickedFromServerEvent.RedirectPlayer.create(registeredServer));
    }
}
