package nl.devpieter.lobstar.socket.events.whitelist;

import nl.devpieter.lobstar.models.whitelist.WhitelistEntry;
import nl.devpieter.sees.Event.Event;

import java.util.List;

public record SyncWhitelistEntriesEvent(List<WhitelistEntry> entries) implements Event {
}
