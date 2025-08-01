package nl.devpieter.lobstar.socket.listeners.player;

import com.microsoft.signalr.Action2;
import nl.devpieter.lobstar.socket.events.player.MovePlayerEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class MovePlayerListener implements ISocketListener<Action2<UUID, UUID>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "MovePlayer";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class, UUID.class);
    }

    @Override
    public Action2<UUID, UUID> getAction() {
        return (playerId, serverId) -> this.sees.call(new MovePlayerEvent(playerId, serverId));
    }
}
