package nl.devpieter.lobstar.socket.listeners.player;

import com.microsoft.signalr.Action3;
import nl.devpieter.lobstar.sees.events.player.KickPlayerEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class KickPlayerListener implements ISocketListener<Action3<UUID, String, Boolean>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "KickPlayer";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class, String.class, Boolean.class);
    }

    @Override
    public Action3<UUID, String, Boolean> getAction() {
        return (playerId, reason, toLobby) -> sees.call(new KickPlayerEvent(playerId, reason, toLobby));
    }
}
