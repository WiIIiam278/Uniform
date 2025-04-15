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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.CommandUser;
import net.william278.uniform.Uniform;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

/**
 * A class for registering commands with the Fabric (1.21) server
 *
 * @since 1.0
 */
@SuppressWarnings("unused")
public final class FabricUniform implements Uniform {

    static FabricUniform INSTANCE;

    private final Set<FabricCommand> commands = Sets.newHashSet();
    @Getter
    private final String modId;

    @Getter
    @Setter
    Function<Object, CommandUser> commandUserSupplier = (user) -> new FabricCommandUser((ServerCommandSource) user);

    private FabricUniform(@NotNull String modId) {
        this.modId = modId.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9_-]", "");
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
                commands.forEach(command -> {
                    final LiteralArgumentBuilder<ServerCommandSource> builder = command.createBuilder();
                    dispatcher.register(builder);

                    final Set<String> aliases = Sets.newHashSet(command.getAliases());
                    command.getAliases().forEach(a -> aliases.add(modId + ":" + a));
                    aliases.add(modId + ":" + command.getName());
                    aliases.forEach(alias -> dispatcher.register(
                            LiteralArgumentBuilder.<ServerCommandSource>literal(alias)
                                    .requires(builder.getRequirement()).executes(builder.getCommand())
                                    .redirect(builder.build())
                    ));
                })
        );

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> shutdown());
    }

    /**
     * Get the FabricUniform instance for registering commands
     *
     * @return The FabricUniform instance
     * @since 1.0
     */
    @NotNull
    public static FabricUniform getInstance(@NotNull String modId) {
        return INSTANCE != null ? INSTANCE : (INSTANCE = new FabricUniform(modId));
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
        Arrays.stream(commands).map(c -> (FabricCommand) c).forEach(this.commands::add);
    }

    /**
     * Register a command with the server's command manager
     *
     * @param commands The commands to register
     * @since 1.0
     */
    @Override
    public void register(@NotNull Command... commands) {
        register(Arrays.stream(commands).map(FabricCommand::new).toArray(FabricCommand[]::new));
    }

    /**
     * Unregister command(s) from the server's command manager
     *
     * @param commands The commands to unregister
     */
    public void unregister(@NotNull String... commands) {
        this.commands.removeIf(command -> Arrays.stream(commands).anyMatch(c -> command.getName().equals(c)));
    }

}
