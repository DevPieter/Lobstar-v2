package nl.devpieter.lobstar.helpers;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.managers.ServerTypeManager;
import nl.devpieter.lobstar.managers.VirtualHostManager;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.models.serverType.ServerType;
import nl.devpieter.lobstar.models.virtualHost.VirtualHost;
import nl.devpieter.lobstar.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ServerHelper {

    private static ServerHelper INSTANCE;

    private final ServerManager serverManager = ServerManager.getInstance();
    private final ServerTypeManager serverTypeManager = ServerTypeManager.getInstance();
    private final VirtualHostManager virtualHostManager = VirtualHostManager.getInstance();

    private final WhitelistHelper whitelistHelper = WhitelistHelper.getInstance();

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
            List<Server> servers = this.serverManager.getServersByTypeId(serverType.getId());
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
        for (var server : lobbyServers.stream().filter(s -> !s.isWhitelistActive()).toList()) {
            RegisteredServer registeredServer = server.findRegisteredServer();
            if (registeredServer == null || !ServerUtils.isOnline(registeredServer)) continue;

            if (!this.whitelistHelper.canJoinServer(player, server)) continue;
            return server;
        }

        for (var server : lobbyServers.stream().filter(Server::isWhitelistActive).toList()) {
            RegisteredServer registeredServer = server.findRegisteredServer();
            if (registeredServer == null || !ServerUtils.isOnline(registeredServer)) continue;

            if (!this.whitelistHelper.canJoinServer(player, server)) continue;
            return server;
        }

        return null;
    }

    // TODO - Add proper logging
    public @Nullable Server tryGetPlayerRequestedServer(@NotNull Player player) {
        InetSocketAddress requestedAddress = player.getVirtualHost().orElse(null);
        if (requestedAddress == null) return null;

        String requestedHost = requestedAddress.getHostString(); // TODO - Add more validation
        VirtualHost virtualHost = this.virtualHostManager.findMatchingVirtualHost(requestedHost, false);
        if (virtualHost == null) return null;

        Server server = this.serverManager.getServerById(virtualHost.getServerId());
        if (server == null) return null;

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) return null;

        if (!ServerUtils.isOnline(registeredServer)) return null;
        if (!this.whitelistHelper.canJoinServer(player, server)) return null;

        return server;
    }
}
