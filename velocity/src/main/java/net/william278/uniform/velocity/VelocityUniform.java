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
import com.velocitypowered.api.proxy.ProxyServer;
import net.william278.uniform.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A class for registering commands with the Velocity server's command manager
 *
 * @since 1.0
 */
@SuppressWarnings("unused")
public final class VelocityUniform {

    private static VelocityUniform INSTANCE;

    private final ProxyServer server;

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
     * @since 1.0
     */
    public void register(@NotNull VelocityCommand... commands) {
        Arrays.stream(commands).forEach(cmd -> server.getCommandManager().register(new BrigadierCommand(cmd.build())));
    }

    /**
     * Register a command with the server's command manager
     *
     * @param commands The commands to register
     * @since 1.0
     */
    public void register(@NotNull Command... commands) {
        register(Arrays.stream(commands).map(VelocityCommand::new).toArray(VelocityCommand[]::new));
    }

}
