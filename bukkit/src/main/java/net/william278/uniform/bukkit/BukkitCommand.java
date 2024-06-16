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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.Uniform;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public class BukkitCommand extends BaseCommand<CommandSender> {

    private BukkitAudiences audiences;

    public BukkitCommand(@NotNull Command command) {
        super(command);
    }

    public BukkitCommand(@NotNull String name, @NotNull String description, @NotNull List<String> aliases) {
        super(name, description, aliases);
    }

    public BukkitCommand(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    @NotNull
    Impl getImpl(@NotNull Uniform uniform) {
        return new Impl(uniform, this);
    }

    static final class Impl extends org.bukkit.command.Command {

        private static final int COMMAND_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;

        private final CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher<>();

        public Impl(@NotNull Uniform uniform, @NotNull BukkitCommand command) {
            super(command.getName());
            this.dispatcher.register(command.createBuilder());
            this.setDescription(command.getDescription());
            this.setAliases(command.getAliases());
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean execute(@NotNull CommandSender commandSender, @NotNull String alias, @NotNull String[] args) {
            try {
                return dispatcher.execute(getInput(args), commandSender) == COMMAND_SUCCESS;
            } catch (CommandSyntaxException e) {
                getAudience(commandSender).sendMessage(Component
                    .translatable("command.context.parse_error", NamedTextColor.RED)
                    .args(
                        Component.text(e.getRawMessage().getString()),
                        Component.text(e.getCursor()),
                        Component.text(e.getContext())
                    ));
                return false;
            } catch (CommandException e) {
                getAudience(commandSender).sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @NotNull
        @Override
        public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
            throws IllegalArgumentException {
            final String passed = getInput(args);
            return dispatcher.getCompletionSuggestions(
                    dispatcher.parse(passed, sender),
                    passed.length() // Spigot API limitation - we can only TAB complete the full text length :(
                )
                .thenApply(suggestions -> suggestions.getList().stream().map(Suggestion::getText).toList())
                .join();
        }

        @NotNull
        private Audience getAudience(@NotNull CommandSender sender) {
            return BukkitUniform.getAudiences().sender(sender);
        }

        @NotNull
        private String getInput(@NotNull String[] args) {
            return args.length == 0 ? getName() : "%s %s".formatted(getName(), String.join(" ", args));
        }
    }

    @Override
    public void addSubCommand(@NotNull Command command) {
        addSubCommand(new BukkitCommand(command));
    }

    @Override
    public Uniform getUniform() {
        return BukkitUniform.INSTANCE;
    }

}
