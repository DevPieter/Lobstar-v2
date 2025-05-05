package nl.devpieter.lobstar.socket.listeners.server;

import com.microsoft.signalr.Action2;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.socket.events.server.ServerCreatedEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class ServerCreatedListener implements ISocketListener<Action2<UUID, Server>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "ServerCreated";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class, Server.class);
    }

    @Override
    public Action2<UUID, Server> getAction() {
        return (serverId, server) -> this.sees.call(new ServerCreatedEvent(serverId, server));
    }
}
