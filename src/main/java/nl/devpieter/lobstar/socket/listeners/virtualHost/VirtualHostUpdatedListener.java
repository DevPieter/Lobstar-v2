package nl.devpieter.lobstar.socket.listeners.virtualHost;

import com.microsoft.signalr.Action2;
import nl.devpieter.lobstar.models.virtualHost.VirtualHost;
import nl.devpieter.lobstar.socket.events.virtualHost.VirtualHostUpdatedEvent;
import nl.devpieter.lobstar.socket.listeners.SocketListener;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class VirtualHostUpdatedListener extends SocketListener<Action2<UUID, VirtualHost>> {

    @Override
    public String getTarget() {
        return "VirtualHostUpdated";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class, VirtualHost.class);
    }

    @Override
    public Action2<UUID, VirtualHost> getAction() {
        return (virtualHostId, virtualHost) -> this.sees.call(new VirtualHostUpdatedEvent(virtualHostId, virtualHost));
    }
}
