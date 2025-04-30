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
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.managers.WhitelistManager;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.models.whitelist.WhitelistEntry;
import nl.devpieter.lobstar.utils.PlayerUtils;
import nl.devpieter.lobstar.utils.ServerUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class WhitelistListener {

    private final Component denied = Component.text("You are not whitelisted on this server!").color(TextColor.color(0xfe3f3f));
    //    private final Component banned = Component.text("You are banned from this server!").color(TextColor.color(0xfe3f3f));

    private final String pending = "Please wait, your whitelist status is being checked!";
    private final Component pendingComponent = Component.text(this.pending).color(TextColor.color(0xfe3f3f));

    private final Component error = Component.text("An error occurred while checking if you can join this server!").color(TextColor.color(0xfe3f3f));
    private final Component noLobby = Component.text("No lobby servers available to redirect you to.").color(TextColor.color(0xfe3f3f));

    private final Lobstar lobstar = Lobstar.getInstance();
    private final Logger logger = this.lobstar.getLogger();

    private final ServerManager serverManager = this.lobstar.getServerManager();
    private final WhitelistManager whitelistManager = this.lobstar.getWhitelistManager();

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        this.logger.info("<Global> Checking whitelist status for {}", player.getUsername());

        if (this.whitelistManager.hasPendingRequest(player.getUniqueId())) {
            this.logger.info("<Global> {} has a pending request", player.getUsername());
            event.setResult(ResultedEvent.ComponentResult.denied(this.pendingComponent));
            return;
        }

        try {
            CompletableFuture<@Nullable WhitelistEntry> future = this.whitelistManager.getWhitelistEntry(player.getUniqueId());
            if (future == null) {
                // Null if still pending, we should never get here
                this.logger.error("<Global> {} has a null future, this should never happen", player.getUsername());
                event.setResult(ResultedEvent.ComponentResult.denied(this.error));
                return;
            }

            WhitelistEntry entry = future.join();
            if (entry == null) {
                this.logger.info("<Global> {} tried to join but no whitelist entry found", player.getUsername());
                event.setResult(ResultedEvent.ComponentResult.denied(this.denied));
                return;
            }

            // TODO - Check ban status

            if (!entry.isWhitelisted()) {
                this.logger.info("<Global> {} tried to join but is not whitelisted", player.getUsername());
                event.setResult(ResultedEvent.ComponentResult.denied(this.denied));
            }
        } catch (Exception e) {
            this.logger.error("<Global> An error occurred while checking whitelist status for {}", player.getUsername(), e);
            event.setResult(ResultedEvent.ComponentResult.denied(this.error));
        }
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        this.logger.info("<CIS> Choosing initial lobby server for {}", event.getPlayer().getUsername());

        Server server = this.serverManager.getLobbyServer(event.getPlayer());
        if (server == null) {
            this.logger.warn("<CIS> No lobby server found for {}", event.getPlayer().getUsername());
            return;
        }

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) {
            this.logger.warn("<CIS> Lobby server {} not registered", server.name());
            return;
        }

        event.setInitialServer(registeredServer);
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        // We can skip the whitelist check if the player doesn't have a previous server,
        // this is already checked in PlayerChooseInitialServerEvent
        if (event.getPreviousServer() == null) return;

        // TODO - Check if already checking whitelist, for when player spams commands

        Player player = event.getPlayer();
        String name = event.getOriginalServer().getServerInfo().getName();

        Server server = this.serverManager.getServer(name);
        if (server == null) {
            this.logger.error("<Server> Server {} not found", name);

            PlayerUtils.sendErrorMessage(player, "Server not found, please try again later!");
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        this.logger.info("<Server> Checking whitelist status for {} on {}", player.getUsername(), name);
        PlayerUtils.sendWhisperMessage(player, String.format("Checking your whitelist status on %s...", server.displayName()));

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) {
            this.logger.error("<Server> Server {} not registered", name);

            PlayerUtils.sendErrorMessage(player, "Server not registered, please try again later!");
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        if (!ServerUtils.isOnline(registeredServer)) {
            this.logger.warn("<Server> Server {} not online", name);

            PlayerUtils.sendErrorMessage(player, "Server seems to be offline, please try again later!");
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        if (!server.isWhitelistEnabled()) {
            this.logger.info("<Server> Server {} does not have its whitelist enabled, allowing {} to join", name, player.getUsername());

            PlayerUtils.sendWhisperMessage(player, String.format("Sending you to %s...", server.displayName()));
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(registeredServer));
            return;
        }

        if (this.whitelistManager.hasPendingRequest(player.getUniqueId())) {
            this.logger.info("<Server> {} has a pending request", player.getUsername());

            PlayerUtils.sendErrorMessage(player, this.pending);
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        try {
            CompletableFuture<@Nullable WhitelistEntry> future = this.whitelistManager.getWhitelistEntry(server.id(), player.getUniqueId());
            if (future == null) {
                // Null if still pending, we should never get here
                this.logger.error("<Server> {} has a null future, this should never happen", player.getUsername());

                PlayerUtils.sendErrorMessage(player, "An error occurred while checking your whitelist status, please try again later!");
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }

            WhitelistEntry entry = future.join();
            if (entry == null) {
                this.logger.info("<Server> {} tried to join {} but no whitelist entry found", player.getUsername(), server.name());

                player.sendMessage(this.error);
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }

            // TODO - Check ban status

            if (!entry.isWhitelisted()) {
                this.logger.info("<Server> {} tried to join {} but is not whitelisted", player.getUsername(), server.name());

                player.sendMessage(this.denied);
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }

            this.logger.info("<Server> Allowing {} to join {}", player.getUsername(), server.name());

            PlayerUtils.sendWhisperMessage(player, String.format("Sending you to %s...", server.displayName()));
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(registeredServer));
        } catch (Exception e) {
            this.logger.error("<Server> An error occurred while checking whitelist status for {} on {}", player.getUsername(), server.name(), e);

            player.sendMessage(this.error);
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }

    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent event) {
        this.logger.info("<KFS> {} was kicked, redirecting to lobby", event.getPlayer().getUsername());

        Server server = this.serverManager.getLobbyServer(event.getPlayer());
        if (server == null) {
            this.logger.warn("<KFS> No lobby server found for {}", event.getPlayer().getUsername());
            event.setResult(KickedFromServerEvent.Notify.create(this.noLobby));
            return;
        }

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) {
            this.logger.warn("<KFS> Lobby server {} not registered", server.name());
            event.setResult(KickedFromServerEvent.Notify.create(this.noLobby));
            return;
        }

        event.setResult(KickedFromServerEvent.RedirectPlayer.create(registeredServer));
    }
}
