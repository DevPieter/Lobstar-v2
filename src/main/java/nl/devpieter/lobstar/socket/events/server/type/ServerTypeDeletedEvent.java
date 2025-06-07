package nl.devpieter.lobstar.socket.events.server.type;

import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record ServerTypeDeletedEvent(UUID serverTypeId) implements Event {
}
