package nl.devpieter.lobstar.models.server;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.enums.ServerType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Server {

    private final UUID id;
    private final UUID ownerId;

    private final String name;
    private @Nullable String prefix;
    private String displayName;

    private int type;
    private boolean isWhitelistEnabled;

    private final String address;
    private final int port;

    public Server(UUID id, UUID ownerId, String name, @Nullable String prefix, String displayName, int type, boolean isWhitelistEnabled, String address, int port) {
        this.id = id;
        this.ownerId = ownerId;

        this.name = name;
        this.prefix = prefix;
        this.displayName = displayName;

        this.type = type;
        this.isWhitelistEnabled = isWhitelistEnabled;

        this.address = address;
        this.port = port;
    }

    public @Nullable RegisteredServer findRegisteredServer() {
        return Lobstar.getInstance().getProxy().getServer(name()).orElse(null);
    }

    public boolean isCriticallyDifferent(@NotNull Server server) {
        return !server.name().equals(this.name) ||
                !server.address().equals(this.address) ||
                server.port() != this.port;
    }

    public UUID id() {
        return id;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public String name() {
        return name;
    }

    public @Nullable String prefix() {
        return prefix;
    }

    public void setPrefix(@Nullable String prefix) {
        this.prefix = prefix;
    }

    public String displayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int type() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ServerType getType() {
        return ServerType.fromInt(type);
    }

    public boolean isWhitelistEnabled() {
        return isWhitelistEnabled;
    }

    public void setWhitelistEnabled(boolean whitelistEnabled) {
        isWhitelistEnabled = whitelistEnabled;
    }

    public String address() {
        return address;
    }

    public int port() {
        return port;
    }
}
