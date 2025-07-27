package nl.devpieter.lobstar.socket.listeners.whitelist;

import com.microsoft.signalr.Action2;
import nl.devpieter.lobstar.models.whitelist.WhitelistEntry;
import nl.devpieter.lobstar.socket.events.whitelist.WhitelistEntryCreatedEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class WhitelistEntryCreatedListener implements ISocketListener<Action2<UUID, WhitelistEntry>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "WhitelistEntryCreated";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(UUID.class, WhitelistEntry.class);
    }

    @Override
    public Action2<UUID, WhitelistEntry> getAction() {
        return (entryId, entry) -> this.sees.call(new WhitelistEntryCreatedEvent(entryId, entry));
    }
}
