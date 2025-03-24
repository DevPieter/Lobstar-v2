package nl.devpieter.lobstar.socket.listeners.server;

import com.microsoft.signalr.Action2;
import nl.devpieter.lobstar.models.Server;
import nl.devpieter.lobstar.sees.events.server.ServerUpdatedEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class ServerUpdatedListener implements ISocketListener<Action2<UUID, @Nullable Server>> {

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
    public Action2<UUID, @Nullable Server> getAction() {
        return (serverId, server) -> sees.call(new ServerUpdatedEvent(serverId, server));
    }
}
