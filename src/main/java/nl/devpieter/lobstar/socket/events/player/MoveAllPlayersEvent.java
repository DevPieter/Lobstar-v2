package nl.devpieter.lobstar.socket.events.player;

import nl.devpieter.sees.Event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record MoveAllPlayersEvent(@Nullable UUID fromServerId, UUID toServerId) implements Event {
}