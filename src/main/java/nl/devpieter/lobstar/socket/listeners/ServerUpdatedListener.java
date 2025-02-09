package nl.devpieter.lobstar.socket.listeners;

import com.microsoft.signalr.Action3;
import nl.devpieter.lobstar.models.Server;
import nl.devpieter.lobstar.sees.events.ServerUpdatedEvent;
import nl.devpieter.sees.Sees;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class ServerUpdatedListener implements ISocketListener<Action3<UUID, @Nullable Server, Boolean>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "ServerUpdated";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class, Server.class, Boolean.class);
    }

    @Override
    public Action3<UUID, @Nullable Server, Boolean> getAction() {
        return (serverId, server, kickPlayers) -> sees.call(new ServerUpdatedEvent(serverId, server, kickPlayers));
    }
}
