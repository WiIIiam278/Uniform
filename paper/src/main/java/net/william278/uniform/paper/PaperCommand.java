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

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.william278.uniform.*;
import net.william278.uniform.element.ArgumentElement;
import net.william278.uniform.element.CommandElement;
import net.william278.uniform.paper.element.PaperArgumentElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

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

    public static Builder builder(String name) {
        return new Builder(name);
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

    public static class Builder {
        private final String name;
        private String description = "";
        private List<String> aliases = new ArrayList<>();
        private Permission permission;
        private CommandExecutor<CommandSourceStack> defaultExecutor;
        private final List<PaperCommand> subCommands = new ArrayList<>();
        private final List<CommandElement<CommandSourceStack>> elements = new ArrayList<>();

        public Builder(String name) {
            this.name = name;
        }

        public final Builder setDescription(@NotNull String description) {
            this.description = description;
            return this;
        }

        public final Builder setAliases(@NotNull List<String> aliases) {
            this.aliases = aliases;
            return this;
        }

        public final Builder setPermission(@NotNull Permission permission) {
            this.permission = permission;
            return this;
        }

        public final Builder addSubCommand(@NotNull Command command) {
            subCommands.add(new PaperCommand(command));
            return this;
        }

        public final Builder setDefaultExecutor(@NotNull CommandExecutor<CommandSourceStack> executor) {
            this.defaultExecutor = executor;
            return this;
        }

        public final Builder addArgument(String argName,
                                         @NotNull ArgumentType<?> argumentType,
                                         @NotNull SuggestionProvider<CommandSourceStack> suggestionProvider) {
            this.elements.add(new ArgumentElement<>(argName, argumentType, suggestionProvider));
            return this;
        }

        public final Builder addStringArgument(String argName, @NotNull SuggestionProvider<CommandSourceStack> suggestionProvider) {
            return addArgument(argName, StringArgumentType.string(), suggestionProvider);
        }

        public final BuilderExecuteStage execute(@NotNull CommandExecutor<CommandSourceStack> executor) {
            return executeConditional(null, executor);
        }

        public final BuilderExecuteStage executeConditional(@Nullable Predicate<CommandSourceStack> condition, @NotNull CommandExecutor<CommandSourceStack> executor) {
            CommandSyntax<CommandSourceStack> syntax = new CommandSyntax<>(condition, executor, elements);
            return new BuilderExecuteStage(syntax);
        }

        public class BuilderExecuteStage {
            private final List<CommandSyntax<CommandSourceStack>> syntaxes = new ArrayList<>();

            private BuilderExecuteStage(CommandSyntax<CommandSourceStack> defaultSyntax) {
                this.syntaxes.add(defaultSyntax);
            }

            @SafeVarargs
            public final BuilderExecuteStage addConditionalSyntax(@Nullable Predicate<CommandSourceStack> condition, @NotNull CommandExecutor<CommandSourceStack> executor,
                                                                  @NotNull CommandElement<CommandSourceStack>... elements) {
                var syntax = new CommandSyntax<>(condition, executor, List.of(elements));
                this.syntaxes.add(syntax);
                return this;
            }

            @SafeVarargs
            public final BuilderExecuteStage addSyntax(@NotNull CommandExecutor<CommandSourceStack> executor, @NotNull CommandElement<CommandSourceStack>... elements) {
                return addConditionalSyntax(null, executor, elements);
            }

            public PaperCommand build() {
                var command = new PaperCommand(name, description, aliases);
                command.setPermission(permission);
                subCommands.forEach(command::addSubCommand);
                command.setDefaultExecutor(defaultExecutor);
                command.syntaxes.addAll(syntaxes);
                return command;
            }

            public PaperCommand register(JavaPlugin plugin) {
                var builtCmd = build();
                PaperCommand.register(plugin, Set.of(builtCmd));
                return builtCmd;
            }
        }
    }
}
