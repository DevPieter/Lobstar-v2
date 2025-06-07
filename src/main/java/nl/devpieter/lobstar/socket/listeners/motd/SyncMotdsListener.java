package nl.devpieter.lobstar.socket.listeners.motd;

import com.microsoft.signalr.Action1;
import nl.devpieter.lobstar.models.motd.Motd;
import nl.devpieter.lobstar.socket.events.motd.SyncMotdsEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;

public class SyncMotdsListener implements ISocketListener<Action1<Motd[]>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "SyncMotds";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(Motd[].class);
    }

    @Override
    public Action1<Motd[]> getAction() {
        return (motds) -> this.sees.call(new SyncMotdsEvent(List.of(motds)));
    }
}
