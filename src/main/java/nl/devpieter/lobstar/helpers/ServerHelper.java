package nl.devpieter.lobstar.helpers;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.managers.ServerTypeManager;
import nl.devpieter.lobstar.managers.VirtualHostManager;
import nl.devpieter.lobstar.managers.WhitelistManager;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.models.server.type.ServerType;
import nl.devpieter.lobstar.models.virtualHost.VirtualHost;
import nl.devpieter.lobstar.models.whitelist.WhitelistEntry;
import nl.devpieter.lobstar.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServerHelper {

    private static ServerHelper INSTANCE;

    private final ServerTypeManager serverTypeManager = ServerTypeManager.getInstance();
    private final ServerManager serverManager = ServerManager.getInstance();
    private final VirtualHostManager virtualHostManager = VirtualHostManager.getInstance();
    private final WhitelistManager whitelistManager = WhitelistManager.getInstance();

    private final Lobstar lobstar = Lobstar.getInstance();
    private final Logger logger = this.lobstar.getLogger();

    private ServerHelper() {
    }

    public static ServerHelper getInstance() {
        if (INSTANCE == null) INSTANCE = new ServerHelper();
        return INSTANCE;
    }

    /**
     * Retrieves a list of all servers that are considered "lobby-like".
     *
     * @return a list of servers that are of lobby-like types.
     */
    public List<Server> getLobbyLikeServers() {
        List<ServerType> lobbyLikeTypes = this.serverTypeManager.getLobbyLikeServerTypes();
        List<Server> lobbyLikeServers = new ArrayList<>();

        for (ServerType serverType : lobbyLikeTypes) {
            List<Server> servers = this.serverManager.getServersByTypeId(serverType.id());
            if (servers.isEmpty()) continue;

            lobbyLikeServers.addAll(servers);
        }

        return lobbyLikeServers;
    }

    /**
     * Retrieves an available lobby server for the given player.
     * <p>
     * This method prioritizes non-whitelisted servers over whitelisted servers to minimize
     * the need for whitelist checks. If no suitable server is found, it returns null.
     *
     * @param player The player for whom to find an available lobby server.
     * @return The available lobby server, or null if no suitable server is found.
     */
    // TODO - Add proper logging
    public @Nullable Server getAvailableLobbyServer(@NotNull Player player) {
        List<Server> lobbyServers = this.getLobbyLikeServers();
        if (lobbyServers.isEmpty()) return null;

        // We prioritize non-whitelisted servers over whitelisted servers to avoid having to check the whitelist for every player
        for (var server : lobbyServers.stream().filter(s -> !s.isWhitelistEnabled()).toList()) {
            RegisteredServer registeredServer = server.findRegisteredServer();
            if (registeredServer == null || !ServerUtils.isOnline(registeredServer)) continue;

            return server;
        }

        for (var server : lobbyServers.stream().filter(Server::isWhitelistEnabled).toList()) {
            RegisteredServer registeredServer = server.findRegisteredServer();

            if (registeredServer == null || !ServerUtils.isOnline(registeredServer)) continue;
            if (!this.isWhitelisted(player, server)) continue;

            return server;
        }

        return null;
    }

    // TODO - Add proper logging
    public @Nullable Server tryGetVirtualHostServer(@NotNull Player player) {
        InetSocketAddress requestedAddress = player.getVirtualHost().orElse(null);
        if (requestedAddress == null) return null;

        String requestedHost = requestedAddress.getHostString(); // TODO - Add more validation
        VirtualHost virtualHost = this.virtualHostManager.findMatchingVirtualHost(requestedHost);
        if (virtualHost == null) return null;

        Server server = this.serverManager.getServerById(virtualHost.serverId());
        if (server == null) return null;

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) return null;

        if (!ServerUtils.isOnline(registeredServer)) return null;
        if (!this.isWhitelisted(player, server)) return null;

        return server;
    }

    // TODO - Add proper logging
    public boolean isWhitelisted(@NotNull Player player, @NotNull Server server) {
        if (!server.isWhitelistEnabled()) return true;

        if (this.whitelistManager.hasPendingRequest(server.id())) {
            return false;
        }

        try {
            CompletableFuture<@Nullable WhitelistEntry> future = this.whitelistManager.getWhitelistEntry(server.id(), player.getUniqueId());
            if (future == null) return false;

            WhitelistEntry entry = future.join();
            if (entry == null) return false;

            // TODO - Check ban status

            return entry.isWhitelisted();
        } catch (Exception e) {
            this.logger.error("An error occurred while checking whitelist status for {} on {}", player.getUsername(), server.name(), e);
            return false;
        }
    }
}
