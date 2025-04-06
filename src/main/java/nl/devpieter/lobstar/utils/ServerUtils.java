package nl.devpieter.lobstar.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.PingOptions;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.models.server.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class ServerUtils {

    public static void kickAllPlayers(Component reason) {
        ProxyServer proxy = Lobstar.getInstance().getProxy();
        proxy.getAllPlayers().forEach(player -> player.disconnect(reason));
    }

    public static boolean kickAllPlayers(@NotNull Server server, Component reason) {
        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) return false;

        registeredServer.getPlayersConnected().forEach(player -> player.disconnect(reason));
        return true;
    }

    public static boolean isOnline(RegisteredServer server) {
        return isOnline(server, Duration.ofMillis(500));
    }

    public static boolean isOnline(RegisteredServer server, Duration timeout) {
        return getServerPing(server, timeout) != null;
    }

    public static int getPlayerCount(@NotNull RegisteredServer server) {
        return server.getPlayersConnected().size();
    }

    public static @Nullable ServerPing getServerPing(RegisteredServer server, Duration timeout) {
        try {
            PingOptions options = PingOptions.builder().timeout(timeout).build();
            return server.ping(options).join();
        } catch (Exception e) {
            return null;
        }
    }
}
