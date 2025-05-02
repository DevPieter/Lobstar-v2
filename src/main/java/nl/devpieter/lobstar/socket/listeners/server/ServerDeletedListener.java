package nl.devpieter.lobstar.socket.listeners.server;

import com.microsoft.signalr.Action1;
import nl.devpieter.lobstar.socket.events.server.ServerDeletedEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.lobstar.socket.listeners.SocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class ServerDeletedListener extends SocketListener<Action1<UUID>> {

    @Override
    public String getTarget() {
        return "ServerDeleted";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class);
    }

    @Override
    public Action1<UUID> getAction() {
        return (serverId) -> this.sees.call(new ServerDeletedEvent(serverId));
    }
}
