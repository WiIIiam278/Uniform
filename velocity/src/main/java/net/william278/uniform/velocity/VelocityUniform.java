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

import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.Setter;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.CommandUser;
import net.william278.uniform.Uniform;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Function;

/**
 * A class for registering commands with the Velocity server's command manager
 *
 * @since 1.0
 */
@SuppressWarnings("unused")
public final class VelocityUniform implements Uniform {

    static VelocityUniform INSTANCE;

    private final ProxyServer server;

    @Getter
    @Setter
    Function<Object, CommandUser> commandUserSupplier = (user) -> new VelocityCommandUser((CommandSource) user);

    private VelocityUniform(@NotNull ProxyServer server) {
        this.server = server;
    }

    /**
     * Get the VelocityUniform instance for registering commands
     *
     * @param server The server instance
     * @return The VelocityUniform instance
     * @since 1.0
     */
    @NotNull
    public static VelocityUniform getInstance(@NotNull ProxyServer server) {
        return INSTANCE != null ? INSTANCE : (INSTANCE = new VelocityUniform(server));
    }

    /**
     * Register a command with the server's command manager
     *
     * @param commands The commands to register
     * @param <S>      The command source type
     * @param <T>      The command type
     * @since 1.0
     */
    @SafeVarargs
    @Override
    public final <S, T extends BaseCommand<S>> void register(@NotNull T... commands) {
        final CommandManager commandManager = server.getCommandManager();
        Arrays.stream(commands).map(c -> (VelocityCommand) c).forEach(c -> commandManager
                .register(commandManager.metaBuilder(c.getName())
                        .aliases(c.getAliases().toArray(new String[0]))
                        .build(), new BrigadierCommand(c.build())));
    }

    /**
     * Register a command with the server's command manager
     *
     * @param commands The commands to register
     * @since 1.0
     */
    @Override
    public void register(@NotNull Command... commands) {
        register(Arrays.stream(commands).map(VelocityCommand::new).toArray(VelocityCommand[]::new));
    }

    /**
     * Unregister a command with the Velocity's command manager
     *
     * @param commands The commands to unregister
     * @since 1.0
     */
    public void unregister(@NotNull String... commands) {
        for (String command : commands) {
            server.getCommandManager().unregister(command);
        }
    }

}
