package nl.devpieter.lobstar.socket.listeners.virtualHost;

import com.microsoft.signalr.Action1;
import nl.devpieter.lobstar.socket.events.virtualHost.VirtualHostDeletedEvent;
import nl.devpieter.lobstar.socket.listeners.SocketListener;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class VirtualHostDeletedListener extends SocketListener<Action1<UUID>> {

    @Override
    public String getTarget() {
        return "VirtualHostDeleted";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class);
    }

    @Override
    public Action1<UUID> getAction() {
        return (virtualHostId) -> this.sees.call(new VirtualHostDeletedEvent(virtualHostId));
    }
}
