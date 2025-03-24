package nl.devpieter.lobstar.sees.events.player;

import nl.devpieter.sees.Event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record KickPlayerEvent(UUID playerId, @Nullable String reason, boolean toLobby) implements Event {
}
