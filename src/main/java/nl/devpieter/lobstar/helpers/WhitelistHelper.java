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

    /**
     * Checks if a player can join a specific server.
     * <p>
     *     This method checks if the player is a superuser, if the server is joinable,
     *     if the server is under maintenance, and if the server's whitelist is active.
     * </p>
     */
    public boolean canJoinServer(@NotNull Player player, @NotNull Server server) {
        if (this.isSuper(player, server)) return true;

        if (!server.isJoinable() || server.isUnderMaintenance()) return false;
        if (!server.isWhitelistActive()) return true;

        WhitelistEntry serverEntry = this.whitelistManager.getServerEntry(server.getId(), player.getUniqueId());
        if (serverEntry == null) return false;

        return !this.isEntryExpired(serverEntry) && serverEntry.isWhitelisted();
    }

    /**
     * Checks if a player is whitelisted on a specific server.
     * <p>
     *     This method checks if the player is a superuser or if they have a valid whitelist entry
     *     for the server that is not expired and is marked as whitelisted.
     * </p>
     *
     * <p>
     *     This does NOT check if the player can join the server, as that may depend on other factors
     *     such as whitelist status, server joinability, and maintenance status.
     *     Use {@link #canJoinServer(Player, Server)} to check if a player can actually join the server.
     * </p>
     */
    public boolean isWhitelisted(@NotNull Player player, @NotNull Server server) {
        if (this.isSuper(player, server)) return true;

        WhitelistEntry serverEntry = this.whitelistManager.getServerEntry(server.getId(), player.getUniqueId());
        if (serverEntry == null) return false;

        if (this.isEntryExpired(serverEntry)) return false;
        return serverEntry.isWhitelisted();
    }

    public boolean isSuper(@NotNull Player player, @NotNull Server server) {
        WhitelistEntry globalEntry = this.whitelistManager.getGlobalEntry(player.getUniqueId());
        if (this.isValidSuperEntry(globalEntry)) return true;

        WhitelistEntry serverEntry = this.whitelistManager.getServerEntry(server.getId(), player.getUniqueId());
        return this.isValidSuperEntry(serverEntry);
    }

    public boolean isValidSuperEntry(@Nullable WhitelistEntry entry) {
        return entry != null && !this.isEntryExpired(entry) && entry.isWhitelisted() && entry.isSuperEntry();
    }

    public boolean isEntryExpired(@Nullable WhitelistEntry entry) {
        if (entry == null) return true;

        if (!entry.getHasExpiration()) return false;
        if (entry.getExpirationDate() == null) return true;

        return entry.getExpirationDate().toInstant().isBefore(Instant.now());
    }
}
