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

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.google.common.collect.Sets;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.AllArgsConstructor;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.CommandUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

@SuppressWarnings({"removal", "deprecation", "UnstableApiUsage"})
public class LegacyPaperCommand extends BaseCommand<BukkitBrigadierCommandSource> {

    static final Function<Object, CommandUser> USER_SUPPLIER = (user) -> new LegacyPaperCommandUser(
        (BukkitBrigadierCommandSource) user
    );

    public LegacyPaperCommand(@NotNull Command command) {
        super(command);
    }

    public LegacyPaperCommand(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    public LegacyPaperCommand(@NotNull String name, @NotNull String description, @NotNull List<String> aliases) {
        super(name, description, aliases);
    }

    @Override
    public void addSubCommand(@NotNull Command command) {
        addSubCommand(new LegacyPaperCommand(command));
    }

    @AllArgsConstructor
    static class Registrar implements Listener {
        @NotNull
        private final PaperUniform uniform;
        @NotNull
        private final JavaPlugin plugin;
        @NotNull
        private final Set<LegacyPaperCommand> commands;

        @EventHandler
        public void commandRegisterEvent(CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
            commands.forEach(command -> {
                // Register root command
                final LiteralCommandNode<BukkitBrigadierCommandSource> built = command.build(uniform);
                event.getRoot().addChild(built);

                // Register aliases
                final Set<String> aliases = Sets.newHashSet(command.getAliases());
                aliases.add("%s:%s".formatted(
                    plugin.getName().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9_]", ""),
                    command.getName())
                );
                aliases.forEach(alias -> event.getRoot().addChild(
                    LiteralArgumentBuilder.<BukkitBrigadierCommandSource>literal(alias)
                        .requires(built.getRequirement()).executes(built.getCommand()).redirect(built)
                        .build()
                ));
            });
            commands.clear();
        }
    }

}
