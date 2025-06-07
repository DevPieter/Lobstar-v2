package nl.devpieter.lobstar.socket.events.motd;

import nl.devpieter.lobstar.models.motd.Motd;
import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record MotdUpdatedEvent(UUID motdId, Motd motd) implements Event {
}
