package nl.devpieter.lobstar.socket.listeners.server;

import com.microsoft.signalr.Action1;
import nl.devpieter.lobstar.models.Server;
import nl.devpieter.lobstar.sees.events.server.SyncServersEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;

public class SyncServersListener implements ISocketListener<Action1<Server[]>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "SyncServers";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(Server[].class);
    }

    @Override
    public Action1<Server[]> getAction() {
        return (servers) -> sees.call(new SyncServersEvent(List.of(servers)));
    }
}
