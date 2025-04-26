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

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.william278.uniform.*;
import net.william278.uniform.element.ArgumentElement;
import net.william278.uniform.element.CommandElement;
import net.william278.uniform.paper.element.PaperArgumentElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class PaperCommand extends BaseCommand<CommandSourceStack> {

    static final Function<Object, CommandUser> USER_SUPPLIER = (user) -> new PaperCommandUser(
            (CommandSourceStack) user
    );

    public PaperCommand(@NotNull Command command) {
        super(command);
    }

    public PaperCommand(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    public PaperCommand(@NotNull String name, @NotNull String description, @NotNull List<String> aliases) {
        super(name, description, aliases);
    }

    static void register(@NotNull JavaPlugin plugin, @NotNull Set<PaperCommand> commands) {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, (event) -> {
            commands.forEach(command -> event.registrar().register(
                plugin.getPluginMeta(),
                command.build(),
                command.getDescription(),
                command.getAliases()
            ));
            commands.clear();
        });
    }

    public static PaperCommandBuilder builder(@NotNull String name) {
        return new PaperCommandBuilder(name);
    }

    public static ArgumentElement<CommandSourceStack, Material> material(@NotNull String name) {
        return new ArgumentElement<>(name, reader -> {
            try {
                return Material.valueOf(reader.readString());
            } catch (IllegalArgumentException e) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
        }, (context, builder) -> {
            if (builder.getRemainingLowerCase().isEmpty()) return builder.buildFuture();
            Arrays.stream(Material.values())
                    .filter(material -> material.name().toLowerCase().contains(builder.getRemainingLowerCase()))
                    .forEach(material -> builder.suggest(material.name()));
            return builder.buildFuture();
        });
    }

    public static ArgumentElement<CommandSourceStack, Collection<? extends Player>> player(@NotNull String name) {
        return new ArgumentElement<>(name, reader -> {
            String playerName = reader.readString();
            if (playerName.equals("@a")) {
                return Bukkit.getOnlinePlayers();
            }
            var player = Bukkit.getPlayer(playerName);
            if (player == null) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
            return List.of(player);
        }, (context, builder) -> {
            builder.suggest("@a");
            for (Player player : Bukkit.getOnlinePlayers()) {
                builder.suggest(player.getName());
            }
            return builder.buildFuture();
        });
    }

    public static ArgumentElement<CommandSourceStack, Sound> sound(@NotNull String name) {
        return enumArgument(name, Sound.class);
    }

    public static ArgumentElement<CommandSourceStack, EntityType> entityType(@NotNull String name) {
        return enumArgument(name, EntityType.class);
    }

    public static ArgumentElement<CommandSourceStack, World> world(@NotNull String name) {
        return new ArgumentElement<>(name, reader -> {
            String worldName = reader.readString();
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
            return world;
        }, (context, builder) -> {
            for (World world : Bukkit.getWorlds()) {
                builder.suggest(world.getName());
            }
            return builder.buildFuture();
        });
    }

    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public List<CommandSyntax<CommandSourceStack>> getSyntaxes() {
        return super.getSyntaxes().stream().map(
            syntax -> new CommandSyntax<>(
                syntax.condition(),
                syntax.executor(),
                syntax.elements().stream()
                    .filter(e -> e instanceof ArgumentElement)
                    .map(e -> (ArgumentElement<?, ?>) e)
                    .map(e -> e.custom() ? new ArgumentElement<>(
                        e.name(),
                        new PaperArgumentElement<>(e.type()),
                        e.suggestionProvider()
                    ) : e)
                    .map(e -> (CommandElement<CommandSourceStack>) e)
                    .toList()
            )
        ).toList();
    }

    @Override
    public void addSubCommand(@NotNull Command command) {
        addSubCommand(new PaperCommand(command));
    }

    @Override
    public Uniform getUniform() {
        return PaperUniform.INSTANCE;
    }

    public static class PaperCommandBuilder extends BaseCommandBuilder<CommandSourceStack, PaperCommandBuilder> {

        public PaperCommandBuilder(@NotNull String name) {
            super(name);
        }

        public final PaperCommandBuilder addSubCommand(@NotNull Command command) {
            subCommands.add(new PaperCommand(command));
            return this;
        }

        @Override
        public @NotNull PaperCommand build() {
            final PaperCommand command = new PaperCommand(name, description, aliases);
            command.addPermissions(permissions);
            subCommands.forEach(command::addSubCommand);
            command.setDefaultExecutor(defaultExecutor);
            command.syntaxes.addAll(syntaxes);
            command.setExecutionScope(executionScope);
            command.setCondition(condition);

            return command;
        }

        @NotNull
        public PaperCommand register(@NotNull JavaPlugin plugin) {
            final PaperCommand builtCmd = build();
            PaperCommand.register(plugin, Set.of(builtCmd));
            return builtCmd;
        }

    }
}
