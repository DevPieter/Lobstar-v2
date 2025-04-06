package nl.devpieter.lobstar.socket.events.server;

import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record ServerDeletedEvent(UUID serverId) implements Event {
}
