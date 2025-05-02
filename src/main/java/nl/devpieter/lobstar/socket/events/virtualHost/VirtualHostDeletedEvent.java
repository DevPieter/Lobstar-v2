package nl.devpieter.lobstar.socket.events.virtualHost;

import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record VirtualHostDeletedEvent(UUID virtualHostId) implements Event {
}
