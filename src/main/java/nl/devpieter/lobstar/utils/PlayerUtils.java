package nl.devpieter.lobstar.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.enums.ServerType;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.models.server.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerUtils {

    public static @Nullable ServerType getCurrentServerType(@NotNull Player player) {
        ServerManager serverManager = ServerManager.getInstance();

        ServerConnection serverConnection = player.getCurrentServer().orElse(null);
        if (serverConnection == null) return null;

        Server server = serverManager.getServerByName(serverConnection.getServerInfo().getName());
        if (server == null) return null;

        return server.getType();
    }

    public static void sendErrorMessage(@NotNull Player player, String message) {
        player.sendMessage(Component.text(message).color(TextColor.color(0xfe3f3f)));
    }

    public static void sendWhisperMessage(@NotNull Player player, String message) {
        player.sendMessage(Component.text(message).color(TextColor.color(0x3f3f3f)));
    }
}
