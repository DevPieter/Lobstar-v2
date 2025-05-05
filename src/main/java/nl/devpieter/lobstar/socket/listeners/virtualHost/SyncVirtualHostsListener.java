package nl.devpieter.lobstar.socket.listeners.virtualHost;

import com.microsoft.signalr.Action1;
import nl.devpieter.lobstar.models.virtualHost.VirtualHost;
import nl.devpieter.lobstar.socket.events.virtualHost.SyncVirtualHostsEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;

public class SyncVirtualHostsListener implements ISocketListener<Action1<VirtualHost[]>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "SyncVirtualHosts";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(VirtualHost[].class);
    }

    @Override
    public Action1<VirtualHost[]> getAction() {
        return (virtualHosts) -> this.sees.call(new SyncVirtualHostsEvent(List.of(virtualHosts)));
    }
}
