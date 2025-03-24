package nl.devpieter.lobstar.socket.events.server;

import nl.devpieter.lobstar.models.Server;
import nl.devpieter.sees.Event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ServerUpdatedEvent(UUID serverId, @Nullable Server server) implements Event {
}
