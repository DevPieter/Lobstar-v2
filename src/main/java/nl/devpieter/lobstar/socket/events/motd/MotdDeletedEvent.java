package nl.devpieter.lobstar.socket.events.motd;

import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record MotdDeletedEvent(UUID motdId) implements Event {
}
