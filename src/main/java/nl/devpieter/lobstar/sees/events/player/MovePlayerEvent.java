package nl.devpieter.lobstar.sees.events.player;

import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record MovePlayerEvent(UUID playerId, UUID serverId) implements Event {
}
