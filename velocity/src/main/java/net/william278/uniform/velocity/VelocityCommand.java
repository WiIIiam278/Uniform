/*
 * This file is part of Uniform, licensed under the GNU General Public License v3.0.
 *
 *  Copyright (c) Tofaa2
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.william278.uniform.velocity;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.CommandUser;
import net.william278.uniform.element.ArgumentElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public class VelocityCommand extends BaseCommand<CommandSource> {

    public VelocityCommand(@NotNull Command command) {
        super(command);
    }

    public VelocityCommand(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    public VelocityCommand(@NotNull String name, @NotNull String description, @NotNull List<String> aliases) {
        super(name, description, aliases);
    }

    protected static ArgumentElement<CommandSource, RegisteredServer> serverArg(ProxyServer server, String name,
                                                                                SuggestionProvider<CommandSource> suggestionProvider) {
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

    protected static ArgumentElement<CommandSource, CommandSource> sourceArg(ProxyServer server, String name,
                                                                             SuggestionProvider<CommandSource> suggestionProvider) {
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

    @Override
    @NotNull
    protected CommandUser getUser(@NotNull CommandSource user) {
        return new VelocityCommandUser(user);
    }
}
