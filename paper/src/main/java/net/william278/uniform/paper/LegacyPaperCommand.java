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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.william278.uniform.*;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unused")
public class LegacyPaperCommand extends BaseCommand<CommandSender> {

    private @Nullable Permission permission;

    static final Function<Object, CommandUser> USER_SUPPLIER = (user) -> new LegacyPaperCommandUser(
            (CommandSender) user
    );

    public LegacyPaperCommand(@NotNull Command command) {
        super(command);
        this.permission = command.getPermission().orElse(null);
    }

    public LegacyPaperCommand(@NotNull String name, @NotNull String description, @NotNull List<String> aliases) {
        super(name, description, aliases);
    }

    public LegacyPaperCommand(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    @NotNull
    Impl getImpl(@NotNull Uniform uniform) {
        return new Impl(uniform, this);
    }

    @Override
    public void setPermission(@NotNull Permission permission) {
        this.permission = permission;
        super.setPermission(permission);
    }

    static final class Impl extends org.bukkit.command.Command {

        private static final int COMMAND_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;

        private final CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher<>();
        private final @Nullable Permission permission;

        public Impl(@NotNull Uniform uniform, @NotNull LegacyPaperCommand command) {
            super(command.getName());
            this.dispatcher.register(command.createBuilder());
            this.permission = command.permission;

            // Setup command properties
            this.setDescription(command.getDescription());
            this.setAliases(command.getAliases());
            this.setUsage(command.build().getUsageText());
            if (permission != null) {
                this.setPermission(permission.node());
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean execute(@NotNull CommandSender commandSender, @NotNull String alias, @NotNull String[] args) {
            try {
                return dispatcher.execute(getInput(args), commandSender) == COMMAND_SUCCESS;
            } catch (CommandSyntaxException e) {
                commandSender.sendMessage(Component
                        .translatable("command.context.parse_error", NamedTextColor.RED)
                        .args(
                                Component.text(e.getRawMessage().getString()),
                                Component.text(e.getCursor()),
                                Component.text(e.getContext())
                        ));
                return false;
            } catch (CommandException e) {
                commandSender.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
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

        @Override
        public boolean testPermissionSilent(@NotNull CommandSender target) {
            if (permission == null || permission.node().isBlank()) {
                return true;
            }
            return new LegacyPaperCommandUser(target).checkPermission(permission);
        }

        @NotNull
        private String getInput(@NotNull String[] args) {
            return args.length == 0 ? getName() : "%s %s".formatted(getName(), String.join(" ", args));
        }
    }

    @Override
    public void addSubCommand(@NotNull Command command) {
        addSubCommand(new LegacyPaperCommand(command));
    }

    @Override
    public Uniform getUniform() {
        return PaperUniform.INSTANCE;
    }

}
