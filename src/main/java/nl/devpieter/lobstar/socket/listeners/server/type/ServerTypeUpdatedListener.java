package nl.devpieter.lobstar.socket.listeners.server.type;

import com.microsoft.signalr.Action2;
import nl.devpieter.lobstar.models.server.type.ServerType;
import nl.devpieter.lobstar.socket.events.server.ServerUpdatedEvent;
import nl.devpieter.lobstar.socket.events.server.type.ServerTypeUpdatedEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class ServerTypeUpdatedListener implements ISocketListener<Action2<UUID, ServerType>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "ServerTypeUpdated";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class, ServerType.class);
    }

    @Override
    public Action2<UUID, ServerType> getAction() {
        return (serverTypeId, serverType) -> this.sees.call(new ServerTypeUpdatedEvent(serverTypeId, serverType));
    }
}
