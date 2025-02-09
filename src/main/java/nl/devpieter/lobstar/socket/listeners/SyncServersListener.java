package nl.devpieter.lobstar.socket.listeners;

import com.microsoft.signalr.Action2;
import nl.devpieter.lobstar.models.Server;
import nl.devpieter.lobstar.sees.events.SyncServersEvent;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;

public class SyncServersListener implements ISocketListener<Action2<Server[], Boolean>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "SyncServers";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(Server[].class, Boolean.class);
    }

    @Override
    public Action2<Server[], Boolean> getAction() {
        return (servers, kickPlayers) -> sees.call(new SyncServersEvent(List.of(servers), kickPlayers));
    }
}
