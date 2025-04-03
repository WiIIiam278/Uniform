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

package net.william278.uniform.paper;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.CommandUser;
import net.william278.uniform.Uniform;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class for registering commands with the Paper server's command manager
 *
 * @since 1.0
 */
public final class PaperUniform implements Uniform {

    static PaperUniform INSTANCE;

    private final Set<PaperCommand> commands = Sets.newHashSet();
    private final boolean useModernApi = isUseModernApi();
    private final JavaPlugin plugin;

    @Getter
    @Setter
    Function<Object, CommandUser> commandUserSupplier;

    private PaperUniform(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        // Modern (1.20.6+) Lifecycle event based Paper Brigadier API
        if (useModernApi) {
            this.commandUserSupplier = PaperCommand.USER_SUPPLIER;
            PaperCommand.register(plugin, commands);
            return;
        }
        this.commandUserSupplier = LegacyPaperCommand.USER_SUPPLIER;
    }

    /**
     * Get the PaperUniform instance for registering commands
     *
     * @param plugin The plugin instance
     * @return The PaperUniform instance
     * @since 1.0
     */
    @NotNull
    public static PaperUniform getInstance(@NotNull JavaPlugin plugin) {
        return INSTANCE != null ? INSTANCE : (INSTANCE = new PaperUniform(plugin));
    }

    // Check if the modern Paper API is available
    private static boolean isUseModernApi() {
        try {
            Class.forName("io.papermc.paper.command.brigadier.CommandSourceStack");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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
        // Mark as to be registered with modern API
        final Stream<T> s = Arrays.stream(commands);
        if (useModernApi) {
            s.forEach(c -> this.commands.add((PaperCommand) c));
            return;
        }

        // Register with the legacy API
        plugin.getServer().getCommandMap().registerAll(
                plugin.getName().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9_]", ""),
                s.map(c -> (LegacyPaperCommand) c).map(c -> (org.bukkit.command.Command) c.getImpl(this)).toList()
        );
    }

    /**
     * Unregister command(s) from the server's command manager
     *
     * @param commands The commands to unregister
     */
    public void unregister(@NotNull String... commands) {
        final Set<String> commandSet = Arrays.stream(commands)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        plugin.getServer().getCommandMap().getKnownCommands()
                .entrySet()
                .removeIf(entry -> {
                    if (commandSet.contains(entry.getKey().toLowerCase())) {
                        entry.getValue().unregister(plugin.getServer().getCommandMap());
                        return true;
                    }
                    return false;
                });
    }

    /**
     * Register command(s) to be added to the server's command manager
     *
     * @param commands The commands to register
     * @since 1.0
     */
    public void register(@NotNull Command... commands) {
        if (useModernApi) {
            register(Arrays.stream(commands).map(PaperCommand::new).toArray(PaperCommand[]::new));
            return;
        }
        register(Arrays.stream(commands).map(LegacyPaperCommand::new).toArray(LegacyPaperCommand[]::new));
    }

}
