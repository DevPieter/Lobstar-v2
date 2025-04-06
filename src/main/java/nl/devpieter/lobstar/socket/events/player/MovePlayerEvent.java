package nl.devpieter.lobstar.socket.events.player;

import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record MovePlayerEvent(UUID playerId, UUID serverId) implements Event {
}
