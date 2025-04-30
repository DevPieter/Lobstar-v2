package nl.devpieter.lobstar.models.common;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.server.ServerPing;
import org.jetbrains.annotations.NotNull;

public record MinecraftVersion(int protocol, String name) {

    public static MinecraftVersion of(@NotNull ProtocolVersion version) {
        return new MinecraftVersion(version.getProtocol(), version.getVersionsSupportedBy().getLast());
    }

    public static MinecraftVersion of(ServerPing.@NotNull Version version) {
        return new MinecraftVersion(version.getProtocol(), version.getName());
    }
}
