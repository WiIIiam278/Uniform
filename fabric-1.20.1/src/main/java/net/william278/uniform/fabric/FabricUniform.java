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

package net.william278.uniform.fabric;

import com.google.common.collect.Sets;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.Uniform;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

/**
 * A class for registering commands with the Fabric (1.20.1) server
 *
 * @since 1.0
 */
@SuppressWarnings("unused")
public final class FabricUniform implements Uniform {

    private static FabricUniform INSTANCE;

    private final Set<FabricCommand> commands = Sets.newHashSet();

    private FabricUniform() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
            commands.forEach(command -> dispatcher.register(command.createBuilder()))
        );
    }

    /**
     * Get the FabricUniform instance for registering commands
     *
     * @return The FabricUniform instance
     * @since 1.0
     */
    @NotNull
    public static FabricUniform getInstance() {
        return INSTANCE != null ? INSTANCE : (INSTANCE = new FabricUniform());
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
    public final <S, T extends BaseCommand<S>> void register(T... commands) {
        Arrays.stream(commands).map(c -> (FabricCommand) c).forEach(this.commands::add);
    }

    /**
     * Register a command with the server's command manager
     *
     * @param commands The commands to register
     * @since 1.0
     */
    @Override
    public void register(Command... commands) {
        register(Arrays.stream(commands).map(FabricCommand::new).toArray(FabricCommand[]::new));
    }

}
