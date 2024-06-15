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

package net.william278.uniform.bukkit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.CommandUser;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class BukkitCommand extends BaseCommand<CommandSender> {

    private final JavaPlugin plugin;
    private BukkitAudiences audiences;

    public BukkitCommand(@NotNull Command command, @NotNull JavaPlugin plugin) {
        super(command);
        this.plugin = plugin;
    }

    public BukkitCommand(@NotNull JavaPlugin plugin, @NotNull String name, @NotNull String description,
                         @NotNull List<String> aliases) {
        super(name, description, aliases);
        this.plugin = plugin;
    }

    public BukkitCommand(@NotNull JavaPlugin plugin, @NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
        this.plugin = plugin;
    }

    @NotNull
    Impl getImpl() {
        return new Impl(this);
    }

    static final class Impl extends org.bukkit.command.Command {

        private static final int COMMAND_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;

        private final LiteralCommandNode<CommandSender> commandNode;
        private final CommandDispatcher<CommandSender> dispatcher;
        private final JavaPlugin plugin;
        private BukkitAudiences audiences;

        public Impl(@NotNull BukkitCommand command) {
            super(command.getName());
            this.audiences = command.audiences;
            this.plugin = command.plugin;

            // Register command, setup description and aliases
            this.dispatcher = new CommandDispatcher<>();
            this.commandNode = this.dispatcher.register(command.build().createBuilder());
            this.setDescription(command.getDescription());
            this.setAliases(command.getAliases());
        }

        @Override
        public boolean execute(@NotNull CommandSender commandSender, @NotNull String alias, @NotNull String[] args) {
            try {
                final String string = getInput(alias, args);
                System.out.println("Usage: \"" + Arrays.toString(dispatcher.getAllUsage(commandNode, commandSender, false)) + "\"");
                return dispatcher.execute(string, commandSender) == COMMAND_SUCCESS;
            } catch (CommandSyntaxException e) {
                getAudience(commandSender).sendMessage(Component
                    .translatable("command.context.parse_error", NamedTextColor.RED)
                    .arguments(
                        Component.text(e.getRawMessage().getString()),
                        Component.text(e.getCursor()),
                        Component.text(e.getContext())
                    ));
                return false;
            } catch (CommandException e) {
                getAudience(commandSender).sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
                return true;
            }
        }

        @NotNull
        @Override
        public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
            throws IllegalArgumentException {
            final String passed = getInput(alias, args);
            return dispatcher.getCompletionSuggestions(
                    dispatcher.parse(passed, sender),
                    passed.length() // Spigot API limitation - we can only TAB complete the full text length :(
                )
                .thenApply(suggestions -> suggestions.getList().stream().map(Suggestion::getText).toList())
                .join();
        }

        @NotNull
        private Audience getAudience(@NotNull CommandSender sender) {
            if (audiences == null) {
                audiences = BukkitAudiences.create(plugin);
            }
            return audiences.sender(sender);
        }

        @NotNull
        private String getInput(@NotNull String alias, @NotNull String[] args) {
            return args.length == 0 ? alias : "%s %s".formatted(alias, String.join(" ", args));
        }
    }

    @Override
    @NotNull
    protected CommandUser getUser(@NotNull Object user) {
        if (audiences == null) {
            audiences = BukkitAudiences.create(plugin);
        }
        return new BukkitCommandUser((CommandSender) user, audiences);
    }

    @Override
    public void addSubCommand(@NotNull Command command) {
        addSubCommand(new BukkitCommand(command, plugin));
    }

}
