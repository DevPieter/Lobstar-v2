package nl.devpieter.lobstar.socket.listeners.player;

import com.microsoft.signalr.Action3;
import nl.devpieter.lobstar.socket.events.player.KickPlayerEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.lobstar.socket.listeners.SocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class KickPlayerListener extends SocketListener<Action3<UUID, String, Boolean>> {

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
        return (playerId, reason, toLobby) -> this.sees.call(new KickPlayerEvent(playerId, reason, toLobby));
    }
}
