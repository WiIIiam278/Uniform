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

package net.william278.uniform.sponge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.Permission;
import net.william278.uniform.Uniform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.Command.Raw;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class SpongeCommand extends BaseCommand<CommandCause> {

    private @Nullable Permission permission;

    public SpongeCommand(@NotNull Command command) {
        super(command);
        this.permission = command.getPermission().orElse(null);
    }

    public SpongeCommand(@NotNull String name, @NotNull String description, @NotNull List<String> aliases) {
        super(name, description, aliases);
    }

    public SpongeCommand(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    @NotNull
    Impl getImpl() {
        return new Impl(this);
    }

    static final class Impl implements Raw {

        private static final int COMMAND_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;
        private final CommandDispatcher<CommandCause> dispatcher = new CommandDispatcher<>();
        private final SpongeCommand command;
        private final @Nullable Permission permission;

        public Impl(@NotNull SpongeCommand command) {
            this.dispatcher.register(command.createBuilder());
            this.command = command;
            this.permission = command.permission;
        }

        @Override
        public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
            try {
                return dispatcher.execute(arguments.immutable().remaining(), cause) == COMMAND_SUCCESS
                        ? CommandResult.success()
                        : CommandResult.error(Component.translatable("command.failed", NamedTextColor.RED));
            } catch (CommandSyntaxException e) {
                throw new CommandException(Component
                        .translatable("command.context.parse_error", NamedTextColor.RED).arguments(
                                Component.text(e.getRawMessage().getString()),
                                Component.text(e.getCursor()),
                                Component.text(e.getContext())
                        ), e, true);
            }
        }

        @Override
        public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
            return dispatcher.getCompletionSuggestions(
                            dispatcher.parse(arguments.remaining(), cause),
                            arguments.cursor()
                    )
                    .thenApply(suggestions -> suggestions.getList().stream().map(s -> CommandCompletion.of(
                            s.getText(), Component.text(s.getTooltip().getString())
                    )).toList())
                    .join();
        }

        @Override
        public boolean canExecute(CommandCause cause) {
            if (permission == null) {
                return true;
            }
            return new SpongeCommandUser(cause).checkPermission(permission);
        }

        @Override
        public Optional<Component> shortDescription(CommandCause cause) {
            return Optional.of(Component.text(command.getDescription()));
        }

        @Override
        public Optional<Component> extendedDescription(CommandCause cause) {
            return Optional.of(Component.text(command.getDescription()));
        }

        @Override
        public Component usage(CommandCause cause) {
            return Component.text(dispatcher.getSmartUsage(dispatcher.getRoot(), cause)
                    .values().stream().map("/%s"::formatted).collect(Collectors.joining("\n")));
        }

    }

    @Override
    public void addSubCommand(@NotNull Command command) {
        addSubCommand(new SpongeCommand(command));
    }

    @Override
    public Uniform getUniform() {
        return SpongeUniform.INSTANCE;
    }

}
