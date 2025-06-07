package nl.devpieter.lobstar.models.common;

import com.velocitypowered.api.proxy.server.ServerPing;

import java.util.Collection;
import java.util.UUID;

public record MotdSamplePlayer(int index, UUID uuid, String name) {

    public ServerPing.SamplePlayer toServerPingSamplePlayer() {
        return new ServerPing.SamplePlayer(this.name, this.uuid);
    }

    public static Collection<ServerPing.SamplePlayer> toServerPingSamplePlayer(MotdSamplePlayer[] samplePlayers) {
        return java.util.Arrays.stream(samplePlayers).map(MotdSamplePlayer::toServerPingSamplePlayer).toList();
    }
}
