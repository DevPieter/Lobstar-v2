package nl.devpieter.lobstar.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.enums.ServerType;
import nl.devpieter.lobstar.managers.ServerManager;
import nl.devpieter.lobstar.models.Server;
import nl.devpieter.lobstar.utils.PlayerUtils;
import nl.devpieter.lobstar.utils.ServerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LobbyCommand {

    private final ServerManager serverManager = Lobstar.getInstance().getServerManager();

    public final BrigadierCommand lobbyCommand = new BrigadierCommand(
            BrigadierCommand.literalArgumentBuilder("lobby")
                    .executes(this::executeCommand)
                    .then(this.createLobbyArgument()
                            .executes(this::executeCommand))
                    .build()
    );

    private @NotNull RequiredArgumentBuilder<CommandSource, String> createLobbyArgument() {
        return BrigadierCommand.requiredArgumentBuilder("lobby", StringArgumentType.word())
                .suggests((context, builder) -> {

                    List<Server> lobbyServers = serverManager.getServers(ServerType.Lobby);
                    lobbyServers.forEach(lobby -> builder.suggest(lobby.name()));

                    return builder.buildFuture();
                });
    }

    private int executeCommand(@NotNull CommandContext<CommandSource> context) {
        if (!(context.getSource() instanceof Player player)) return 0;
        String lobbyName = readOptionalArgument(context, "lobby");

        if (lobbyName == null || lobbyName.isEmpty()) {
            // TODO - Send player to 'random' lobby
            return 1;
        }

        Server server = serverManager.getServer(lobbyName);
        if (server == null) {
            PlayerUtils.sendErrorMessage(player, "Lobby not found, please try again later!");
            return 1;
        }

        RegisteredServer registeredServer = server.findRegisteredServer();
        if (registeredServer == null) {
            PlayerUtils.sendErrorMessage(player, "Server not registered, please try again later!");
            return 1;
        }

        if (!ServerUtils.isOnline(registeredServer)) {
            PlayerUtils.sendErrorMessage(player, "Server seems to be offline, please try again later!");
            return 1;
        }

        // TODO - Check whitelist and send player to lobby

        return 0;
    }

    private @Nullable String readOptionalArgument(CommandContext<CommandSource> context, String argument) {
        try {
            return StringArgumentType.getString(context, argument);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
