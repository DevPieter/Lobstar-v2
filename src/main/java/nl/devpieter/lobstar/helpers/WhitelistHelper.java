package nl.devpieter.lobstar.helpers;

import com.velocitypowered.api.proxy.Player;
import nl.devpieter.lobstar.managers.WhitelistManager;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.models.whitelist.WhitelistEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class WhitelistHelper {

    private static WhitelistHelper INSTANCE;

    private final WhitelistManager whitelistManager = WhitelistManager.getInstance();

    private WhitelistHelper() {
    }

    public static WhitelistHelper getInstance() {
        if (INSTANCE == null) INSTANCE = new WhitelistHelper();
        return INSTANCE;
    }

    public boolean canJoinGlobal(@NotNull Player player) {
        WhitelistEntry globalEntry = this.whitelistManager.getGlobalEntry(player.getUniqueId());
        if (globalEntry == null) return false;

        if (this.isEntryExpired(globalEntry)) return false;
        return globalEntry.isWhitelisted();
    }

    public boolean canJoinServer(@NotNull Player player, @NotNull Server server) {
        if (!server.isWhitelistActive()) return true;

        WhitelistEntry globalEntry = this.whitelistManager.getGlobalEntry(player.getUniqueId());
        boolean isGlobalSuper = globalEntry != null &&
                !this.isEntryExpired(globalEntry) &&
                globalEntry.isWhitelisted() &&
                globalEntry.isSuperEntry();

        if (isGlobalSuper) return true;

        WhitelistEntry serverEntry = this.whitelistManager.getServerEntry(server.getId(), player.getUniqueId());
        if (serverEntry == null) return false;

        if (this.isEntryExpired(serverEntry) || !serverEntry.isWhitelisted()) return false;

        return server.isJoinable() || serverEntry.isSuperEntry();
    }

    public boolean isEntryExpired(@Nullable WhitelistEntry entry) {
        if (entry == null) return true;

        if (!entry.getHasExpiration()) return false;
        if (entry.getExpirationDate() == null) return true;

        return entry.getExpirationDate().toInstant().isBefore(Instant.now());
    }
}
