package nl.devpieter.lobstar.socket.events.server;

import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record ServerUpdatedEvent(UUID serverId, Server server) implements Event {
}
