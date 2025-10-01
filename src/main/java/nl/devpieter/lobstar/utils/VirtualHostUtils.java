package nl.devpieter.lobstar.utils;

import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

public class VirtualHostUtils {

    public static @Nullable String getVirtualHost(@NotNull Player player) {
        InetSocketAddress requestedAddress = player.getVirtualHost().orElse(null);
        if (requestedAddress == null) return null;

        String hostString = getSanitizedHost(requestedAddress);
        if (hostString == null || hostString.isEmpty()) return null;

        return hostString;
    }

    public static @Nullable String getSanitizedHost(@NotNull InetSocketAddress address) {
        String hostString = address.getHostString();
        if (hostString == null || hostString.isEmpty()) return null;

        String sanitizedHost = sanitizeHost(hostString);
        if (sanitizedHost.isEmpty()) return null;

        return sanitizedHost;
    }

    private static String sanitizeHost(@NotNull String host) {
        return host.replaceAll("[^a-zA-Z0-9-._]", "");
    }

}
