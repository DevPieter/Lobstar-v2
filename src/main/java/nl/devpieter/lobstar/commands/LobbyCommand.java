package nl.devpieter.lobstar.commands;

import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.enums.ServerType;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.models.server.Server;
import nl.devpieter.lobstar.utils.PlayerUtils;
import org.jetbrains.annotations.NotNull;

public class LobbyCommand {

    private final ServerManager serverManager = Lobstar.getInstance().getServerManager();

    public final BrigadierCommand lobbyCommand = new BrigadierCommand(
            BrigadierCommand.literalArgumentBuilder("lobby")
                    .executes(this::executeCommand)
                    .build()
    );

    private int executeCommand(@NotNull CommandContext<CommandSource> context) {
        if (!(context.getSource() instanceof Player player)) return 0;

        ServerConnection currentConnection = player.getCurrentServer().orElse(null);
        if (currentConnection != null) {
            Server currentServer = serverManager.getServer(currentConnection);
            if (currentServer != null && currentServer.getType() == ServerType.Lobby) {
                PlayerUtils.sendErrorMessage(player, "You are already in a lobby server!");
                return 1;
            }
        }

        Server server = this.serverManager.getAvailableLobbyServer(player);
        if (server == null) {
            PlayerUtils.sendErrorMessage(player, "No lobby servers registered");
            return 1;
        }

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) {
            PlayerUtils.sendErrorMessage(player, "Lobby server not found");
            return 1;
        }

        player.createConnectionRequest(registeredServer).fireAndForget();
        return 1;
    }
}
