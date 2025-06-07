package nl.devpieter.lobstar.socket.events.server.type;

import nl.devpieter.lobstar.models.server.type.ServerType;
import nl.devpieter.sees.Event.Event;

import java.util.List;

public record SyncServerTypesEvent(List<ServerType> serverTypes) implements Event {
}
