package nl.devpieter.lobstar.models;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.enums.ServerType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

//public record Server(UUID id, String name, String displayName, int type, String ip, int port, boolean isWhitelistEnabled) {
public class Server {

    private final UUID id;

    private final String name;
    private String displayName;

    private int type;

    private final String ip;
    private final int port;

    private boolean isWhitelistEnabled;

    public Server(UUID id, String name, String displayName, int type, String ip, int port, boolean isWhitelistEnabled) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.ip = ip;
        this.port = port;
        this.isWhitelistEnabled = isWhitelistEnabled;
    }

    public @Nullable RegisteredServer findRegisteredServer() {
        return Lobstar.getInstance().getProxy().getServer(name()).orElse(null);
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
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

    public ServerType getType() {
        return ServerType.fromInt(type);
    }

    public void setType(int type) {
        this.type = type;
    }

    public String ip() {
        return ip;
    }

    public int port() {
        return port;
    }

    public boolean isWhitelistEnabled() {
        return isWhitelistEnabled;
    }

    public void setWhitelistEnabled(boolean whitelistEnabled) {
        isWhitelistEnabled = whitelistEnabled;
    }
}
