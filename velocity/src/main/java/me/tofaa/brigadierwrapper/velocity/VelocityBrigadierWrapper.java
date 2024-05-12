package me.tofaa.brigadierwrapper.velocity;

import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.proxy.ProxyServer;

public final class VelocityBrigadierWrapper {

    private VelocityBrigadierWrapper() {}

    public static void registerCommands(
            ProxyServer server,
            VelocityCommand... commands
    ) {
        for (VelocityCommand command : commands) {
            server.getCommandManager().register(new BrigadierCommand(command.build()));
        }
    }

}
