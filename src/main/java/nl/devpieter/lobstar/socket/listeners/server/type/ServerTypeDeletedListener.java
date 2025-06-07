package nl.devpieter.lobstar.socket.listeners.server.type;

import com.microsoft.signalr.Action1;
import nl.devpieter.lobstar.socket.events.server.ServerDeletedEvent;
import nl.devpieter.lobstar.socket.events.server.type.ServerTypeDeletedEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class ServerTypeDeletedListener implements ISocketListener<Action1<UUID>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "ServerTypeDeleted";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class);
    }

    @Override
    public Action1<UUID> getAction() {
        return (serverTypeId) -> this.sees.call(new ServerTypeDeletedEvent(serverTypeId));
    }
}
