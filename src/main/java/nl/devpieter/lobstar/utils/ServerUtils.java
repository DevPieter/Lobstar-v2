package nl.devpieter.lobstar.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.PingOptions;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.models.server.Server;
import org.jetbrains.annotations.NotNull;

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
        try {
            var options = PingOptions.builder().timeout(timeout).build();
            server.ping(options).join();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int getPlayerCount(RegisteredServer server) {
        return server.getPlayersConnected().size();
    }
}
