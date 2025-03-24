package nl.devpieter.lobstar.socket.events.player;

import nl.devpieter.sees.Event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record KickAllPlayersEvent(@Nullable UUID serverId, @Nullable String reason, boolean toLobby) implements Event {
}
