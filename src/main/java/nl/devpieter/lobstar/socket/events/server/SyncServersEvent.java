package nl.devpieter.lobstar.socket.events.server;

import nl.devpieter.lobstar.models.Server;
import nl.devpieter.sees.Event.Event;

import java.util.List;

public record SyncServersEvent(List<Server> servers) implements Event {
}
