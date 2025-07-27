package nl.devpieter.lobstar.socket.listeners.whitelist;

import com.microsoft.signalr.Action1;
import nl.devpieter.lobstar.models.whitelist.WhitelistEntry;
import nl.devpieter.lobstar.socket.events.whitelist.SyncWhitelistEntriesEvent;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.sees.Sees;

import java.lang.reflect.Type;
import java.util.List;

public class SyncWhitelistEntriesListener implements ISocketListener<Action1<WhitelistEntry[]>> {

    private final Sees sees = Sees.getInstance();

    @Override
    public String getTarget() {
        return "SyncWhitelistEntries";
    }

    @Override
    public List<Type> getTypes() {
        return List.of(WhitelistEntry[].class);
    }

    @Override
    public Action1<WhitelistEntry[]> getAction() {
        return (entries) -> this.sees.call(new SyncWhitelistEntriesEvent(List.of(entries)));
    }
}
