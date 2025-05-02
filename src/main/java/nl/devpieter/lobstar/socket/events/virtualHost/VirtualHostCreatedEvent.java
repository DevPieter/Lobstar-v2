package nl.devpieter.lobstar.socket.events.virtualHost;

import nl.devpieter.lobstar.models.virtualHost.VirtualHost;
import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record VirtualHostCreatedEvent(UUID virtualHostId, VirtualHost virtualHost) implements Event {
}
