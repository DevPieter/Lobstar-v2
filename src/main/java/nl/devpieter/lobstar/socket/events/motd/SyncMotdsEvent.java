package nl.devpieter.lobstar.socket.events.motd;

import nl.devpieter.lobstar.models.motd.Motd;
import nl.devpieter.sees.Event.Event;

import java.util.List;

public record SyncMotdsEvent(List<Motd> motds) implements Event {
}
