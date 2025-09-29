package nl.devpieter.lobstar.utils;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public class PlayerUtils {

    public static void sendMiniMessage(@NotNull Player player, String message) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static void sendErrorMessage(@NotNull Player player, String message) {
        player.sendMessage(Component.text(message).color(TextColor.color(0xfe3f3f)));
    }

    public static void sendErrorMessage(@NotNull Player player, Component message) {
        player.sendMessage(message.color(TextColor.color(0xfe3f3f)));
    }

    public static void sendWhisperMessage(@NotNull Player player, String message) {
        player.sendMessage(Component.text(message).color(TextColor.color(0x3f3f3f)));
    }
}
