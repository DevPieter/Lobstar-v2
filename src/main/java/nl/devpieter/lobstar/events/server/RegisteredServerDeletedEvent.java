package nl.devpieter.lobstar.events.server;

import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record RegisteredServerDeletedEvent(UUID serverId, boolean permanently) implements Event {
}
