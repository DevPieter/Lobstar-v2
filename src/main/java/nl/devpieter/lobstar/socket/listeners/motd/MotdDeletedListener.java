package nl.devpieter.lobstar.socket.listeners.motd;

import com.microsoft.signalr.Action1;
import nl.devpieter.lobstar.socket.events.motd.MotdDeletedEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class MotdDeletedListener implements ISocketListener<Action1<UUID>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "MotdDeleted";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class);
    }

    @Override
    public Action1<UUID> getAction() {
        return (motdId) -> this.sees.call(new MotdDeletedEvent(motdId));
    }
}
