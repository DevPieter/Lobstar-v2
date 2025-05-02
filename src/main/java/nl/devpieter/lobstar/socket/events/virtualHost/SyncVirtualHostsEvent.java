package nl.devpieter.lobstar.socket.events.virtualHost;

import nl.devpieter.lobstar.models.virtualHost.VirtualHost;
import nl.devpieter.sees.Event.Event;

import java.util.List;

public record SyncVirtualHostsEvent(List<VirtualHost> virtualHosts) implements Event {
}
