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
import net.william278.uniform.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * A class for registering commands with the Fabric (1.20.6) server
 *
 * @since 1.0
 */
public final class FabricUniform {

    private static FabricUniform INSTANCE;

    private final Set<FabricCommand> commands = Sets.newHashSet();

    private FabricUniform() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
                commands.forEach(command -> dispatcher.register(command.build().createBuilder()))
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
     * Register a command to be added to the server's command manager
     *
     * @param commands The commands to register
     * @since 1.0
     */
    public void register(@NotNull FabricCommand... commands) {
        Collections.addAll(this.commands, commands);
    }

    /**
     * Register a command to be added to the server's command manager
     *
     * @param commands The commands to register
     * @since 1.0
     */
    public void register(@NotNull Command... commands) {
        register(Arrays.stream(commands).map(FabricCommand::new).toArray(FabricCommand[]::new));
    }

}
