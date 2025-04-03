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

    public static ArgumentElement<CommandSourceStack, Material> material(String name) {
        return new ArgumentElement<>(name, reader -> {
            String materialName = reader.readString();
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
            return material;
        }, (context, builder) -> {
            for (Material material : Material.values()) {
                builder.suggest(material.name());
            }
            return builder.buildFuture();
        });
    }

    public static ArgumentElement<CommandSourceStack, Sound> sound(String name) {
        return enumArgument(name, Sound.class);
    }

    public static ArgumentElement<CommandSourceStack, EntityType> entityType(String name) {
        return enumArgument(name, EntityType.class);
    }

    /**
     * Argument element for a generic enum class
     *
     * @param name the name of the argument
     * @return the argument element
     */
    private static <E extends Enum<E>> ArgumentElement<CommandSourceStack, E> enumArgument(String name, Class<E> enumClass) {
        return new ArgumentElement<>(name, reader -> {
            String enumName = reader.readString();
            E enumValue;
            try {
                enumValue = Enum.valueOf(enumClass, enumName);
            } catch (IllegalArgumentException e) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
            return enumValue;
        }, (context, builder) -> {
            for (E enumValue : enumClass.getEnumConstants()) {
                builder.suggest(enumValue.name());
            }
            return builder.buildFuture();
        });
    }

    public static ArgumentElement<CommandSourceStack, World> world(String name) {
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

    public static ArgumentElement<CommandSourceStack, Collection<? extends Player>> player(String name) {
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

}
