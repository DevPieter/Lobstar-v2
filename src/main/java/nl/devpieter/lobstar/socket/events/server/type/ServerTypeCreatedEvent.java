package nl.devpieter.lobstar.socket.events.server.type;

import nl.devpieter.lobstar.models.serverType.ServerType;
import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record ServerTypeCreatedEvent(UUID serverTypeId, ServerType serverType) implements Event {
}
