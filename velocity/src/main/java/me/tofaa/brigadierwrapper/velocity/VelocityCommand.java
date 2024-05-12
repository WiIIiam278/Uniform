package me.tofaa.brigadierwrapper.velocity;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.tofaa.brigadierwrapper.Command;
import me.tofaa.brigadierwrapper.element.ArgumentElement;
import org.jetbrains.annotations.NotNull;

public class VelocityCommand extends Command<CommandSource> {

    public VelocityCommand(@NotNull String name, @NotNull String... aliases) {
        super(name, aliases);
    }

    protected static ArgumentElement<CommandSource, RegisteredServer> serverArg(ProxyServer server, String name, SuggestionProvider<CommandSource> suggestionProvider) {
        ArgumentType<RegisteredServer> argumentType = reader -> {
            String s = reader.readUnquotedString();
            RegisteredServer server1 = server.getServer(s).orElse(null);
            if (server1 == null) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
            return server1;
        };
        return new ArgumentElement<>(name, argumentType, suggestionProvider);
    }

    protected static ArgumentElement<CommandSource, RegisteredServer> serverArg(ProxyServer server, String name) {
        return serverArg(server, name, (context, builder) -> {
            for (RegisteredServer server1 : server.getAllServers()) {
                builder.suggest(server1.getServerInfo().getName());
            }
            return builder.buildFuture();
        });
    }

    protected static ArgumentElement<CommandSource, CommandSource> sourceArg(ProxyServer server, String name, SuggestionProvider<CommandSource> suggestionProvider) {
        ArgumentType<CommandSource> argumentType = reader -> {
            String s = reader.readUnquotedString();
            CommandSource source = server.getPlayer(s).orElse(null);
            if (source == null) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
            return source;
        };
        return new ArgumentElement<>(name, argumentType, suggestionProvider);
    }

    protected static ArgumentElement<CommandSource, CommandSource> sourceArg(ProxyServer server, String name) {
        return sourceArg(server, name, (context, builder) -> {
            for (Player source : server.getAllPlayers()) {
                builder.suggest(source.getUsername());
            }
            return builder.buildFuture();
        });
    }
}
