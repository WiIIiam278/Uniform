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
import net.william278.uniform.Command;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * A class for registering commands with the Paper server's command manager
 *
 * @since 1.0
 */
public final class PaperUniform {

    private static PaperUniform INSTANCE;

    private final Set<LegacyPaperCommand> legacyCommands = Sets.newHashSet();
    private final Set<PaperCommand> commands = Sets.newHashSet();

    private PaperUniform(@NotNull JavaPlugin plugin) {
        if (isUseModernApi()) {
            PaperCommand.register(plugin, commands);
        } else {
            plugin.getServer().getPluginManager().registerEvents(
                new LegacyPaperCommand.Registrar(plugin, legacyCommands), plugin
            );
        }
    }

    private static boolean isUseModernApi() {
        try {
            Class.forName("io.papermc.paper.command.brigadier.CommandSourceStack");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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

    /**
     * Register a command to be added to the server's command manager
     *
     * @param commands The commands to register
     * @since 1.0
     */
    public void register(PaperCommand... commands) {
        Collections.addAll(this.commands, commands);
    }

    /**
     * Register a command to be added to the server's command manager
     *
     * @param commands The commands to register
     * @since 1.0
     */
    public void register(LegacyPaperCommand... commands) {
        Collections.addAll(this.legacyCommands, commands);
    }

    /**
     * Register command(s) to be added to the server's command manager
     *
     * @param commands The commands to register
     * @since 1.0
     */
    public void register(@NotNull Command... commands) {
        if (isUseModernApi()) {
            register(Arrays.stream(commands).map(PaperCommand::new).toArray(PaperCommand[]::new));
        } else {
            register(Arrays.stream(commands).map(LegacyPaperCommand::new).toArray(LegacyPaperCommand[]::new));
        }
    }

}
