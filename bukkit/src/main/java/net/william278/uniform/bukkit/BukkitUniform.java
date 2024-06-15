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

package net.william278.uniform.bukkit;/*
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

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.william278.uniform.Command;
import net.william278.uniform.Uniform;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.commands.CommandRegistration;

import java.util.Arrays;
import java.util.Locale;

/**
 * A class for registering commands with the Bukkit server's CommandMap
 *
 * @since 1.0
 */
public final class BukkitUniform implements Uniform<CommandSender, BukkitCommand> {

    private static BukkitUniform INSTANCE;
    private static BukkitAudiences AUDIENCES;
    private static JavaPlugin PLUGIN;

    private final CommandRegistration registrar;

    private BukkitUniform(@NotNull JavaPlugin plugin) {
        PLUGIN = plugin;
        this.registrar = new MorePaperLib(plugin).commandRegistration();
    }

    static BukkitAudiences getAudiences() {
        return AUDIENCES != null ? AUDIENCES : (AUDIENCES = BukkitAudiences.create(PLUGIN));
    }

    /**
     * Get the BukkitUniform instance for registering commands
     *
     * @param plugin The plugin instance
     * @return BukkitUniform instance
     * @since 1.0
     */
    @NotNull
    public static BukkitUniform getInstance(@NotNull JavaPlugin plugin) {
        return INSTANCE != null ? INSTANCE : (INSTANCE = new BukkitUniform(plugin));
    }

    /**
     * Register a command to be added to the server's command map
     *
     * @param commands The commands to register
     * @since 1.0
     */
    @Override
    public void register(@NotNull BukkitCommand... commands) {
        registrar.getServerCommandMap().registerAll(
            PLUGIN.getName().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9_]", ""),
            Arrays.stream(commands).map(c -> (org.bukkit.command.Command) c.getImpl()).toList()
        );
    }

    /**
     * Register command(s) to be added to the server's command map
     *
     * @param commands The commands to register
     * @since 1.0
     */
    @Override
    public void register(@NotNull Command... commands) {
        register(Arrays.stream(commands).map(BukkitCommand::new).toArray(BukkitCommand[]::new));
    }

}
