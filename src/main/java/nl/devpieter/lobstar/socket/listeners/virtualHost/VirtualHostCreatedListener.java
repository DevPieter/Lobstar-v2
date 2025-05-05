package nl.devpieter.lobstar.socket.listeners.virtualHost;

import com.microsoft.signalr.Action2;
import nl.devpieter.lobstar.models.virtualHost.VirtualHost;
import nl.devpieter.lobstar.socket.events.virtualHost.VirtualHostCreatedEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class VirtualHostCreatedListener implements ISocketListener<Action2<UUID, VirtualHost>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "VirtualHostCreated";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class, VirtualHost.class);
    }

    @Override
    public Action2<UUID, VirtualHost> getAction() {
        return (virtualHostId, virtualHost) -> this.sees.call(new VirtualHostCreatedEvent(virtualHostId, virtualHost));
    }
}
