package nl.devpieter.lobstar.models.common;

import com.velocitypowered.api.proxy.server.ServerPing;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record MotdSamplePlayer(int index, UUID uuid, String name) {

    public ServerPing.SamplePlayer toServerPingSamplePlayer() {
        return new ServerPing.SamplePlayer(this.name, this.uuid);
    }

    public static Collection<ServerPing.SamplePlayer> toServerPingSamplePlayer(@NotNull List<MotdSamplePlayer> samplePlayers) {
        return samplePlayers.stream().map(MotdSamplePlayer::toServerPingSamplePlayer).toList();
    }
}
