package nl.devpieter.lobstar.socket.listeners.player;

import com.microsoft.signalr.Action3;
import nl.devpieter.lobstar.socket.events.player.KickAllPlayersEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.lobstar.socket.listeners.SocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class KickAllPlayersListener extends SocketListener<Action3<UUID, String, Boolean>> {

    @Override
    public String getTarget() {
        return "KickAllPlayers";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class, String.class, Boolean.class);
    }

    @Override
    public Action3<UUID, String, Boolean> getAction() {
        return (serverId, reason, toLobby) -> this.sees.call(new KickAllPlayersEvent(serverId, reason, toLobby));
    }
}
