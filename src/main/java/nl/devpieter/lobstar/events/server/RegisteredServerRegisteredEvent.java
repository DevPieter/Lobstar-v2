package nl.devpieter.lobstar.events.server;

import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record RegisteredServerRegisteredEvent(UUID serverId) implements Event {
}
