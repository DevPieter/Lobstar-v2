package nl.devpieter.lobstar.socket.events.server.type;

import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.models.server.type.ServerType;
import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record ServerTypeUpdatedEvent(UUID serverTypeId, ServerType serverType) implements Event {
}
