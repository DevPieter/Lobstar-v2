package nl.devpieter.lobstar.socket.listeners.motd;

import com.microsoft.signalr.Action2;
import nl.devpieter.lobstar.models.motd.Motd;
import nl.devpieter.lobstar.socket.events.motd.MotdUpdatedEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class MotdUpdatedListener implements ISocketListener<Action2<UUID, Motd>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "MotdUpdated";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class, Motd.class);
    }

    @Override
    public Action2<UUID, Motd> getAction() {
        return (motdId, motd) -> this.sees.call(new MotdUpdatedEvent(motdId, motd));
    }
}
