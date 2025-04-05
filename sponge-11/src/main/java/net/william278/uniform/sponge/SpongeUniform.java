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

package net.william278.uniform.sponge;

import lombok.Getter;
import lombok.Setter;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.CommandUser;
import net.william278.uniform.Uniform;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.Command.Raw;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@SuppressWarnings("unused")
public final class SpongeUniform implements Uniform {

    static SpongeUniform INSTANCE;

    private final Set<SpongeCommand> commands = new HashSet<>();
    private final PluginContainer plugin;

    @Getter
    @Setter
    Function<Object, CommandUser> commandUserSupplier = (cause) -> new SpongeCommandUser((CommandCause) cause);

    private SpongeUniform(@NotNull PluginContainer plugin, @NotNull Game game) {
        this.plugin = plugin;
        game.eventManager().registerListeners(plugin, this);
    }

    /**
     * Get the SpongeUniform instance for registering commands
     *
     * @param plugin The plugin container
     * @param game   The game instance
     * @return The SpongeUniform instance
     * @since 1.1.10
     */
    @NotNull
    public static SpongeUniform getInstance(@NotNull PluginContainer plugin, @NotNull Game game) {
        return INSTANCE != null ? INSTANCE : (INSTANCE = new SpongeUniform(plugin, game));
    }

    @Listener
    public void onRegisterCommands(@NotNull RegisterCommandEvent<Raw> event) {
        commands.forEach(command -> event.register(
                plugin, command.getImpl(), command.getName(), command.getAliases().toArray(String[]::new)
        ));
    }

    /**
     * Register a command with the server's command manager
     *
     * @param commands The commands to register
     * @param <S>      The command source type
     * @param <T>      The command type
     * @since 1.1.10
     */
    @SafeVarargs
    @Override
    public final <S, T extends BaseCommand<S>> void register(T... commands) {
        Arrays.stream(commands).map(c -> (SpongeCommand) c).forEach(this.commands::add);
    }

    /**
     * Register a command with the server's command manager
     *
     * @param commands The commands to register
     * @since 1.1.10
     */
    @Override
    public void register(Command... commands) {
        register(Arrays.stream(commands).map(SpongeCommand::new).toArray(SpongeCommand[]::new));
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
