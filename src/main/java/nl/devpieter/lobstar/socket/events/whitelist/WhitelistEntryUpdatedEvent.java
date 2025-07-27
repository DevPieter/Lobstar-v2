package nl.devpieter.lobstar.socket.events.whitelist;

import nl.devpieter.lobstar.models.whitelist.WhitelistEntry;
import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record WhitelistEntryUpdatedEvent(UUID entryId, WhitelistEntry entry) implements Event {
}
