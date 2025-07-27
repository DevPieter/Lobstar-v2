package nl.devpieter.lobstar.socket.events.whitelist;

import nl.devpieter.sees.Event.Event;

import java.util.UUID;

public record WhitelistEntryDeletedEvent(UUID entryId) implements Event {
}
