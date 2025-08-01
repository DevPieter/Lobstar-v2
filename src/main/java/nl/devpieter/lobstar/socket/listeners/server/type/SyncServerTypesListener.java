package nl.devpieter.lobstar.socket.listeners.server.type;

import com.microsoft.signalr.Action1;
import nl.devpieter.lobstar.models.serverType.ServerType;
import nl.devpieter.lobstar.socket.events.server.type.SyncServerTypesEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;

public class SyncServerTypesListener implements ISocketListener<Action1<ServerType[]>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "SyncServerTypes";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(ServerType[].class);
    }

    @Override
    public Action1<ServerType[]> getAction() {
        return (serverTypes) -> this.sees.call(new SyncServerTypesEvent(List.of(serverTypes)));
    }
}
