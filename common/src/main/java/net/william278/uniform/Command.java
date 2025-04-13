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

package net.william278.uniform;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.william278.uniform.annotations.Argument;
import net.william278.uniform.annotations.CommandDescription;
import net.william278.uniform.annotations.CommandNode;
import net.william278.uniform.annotations.Syntax;
import net.william278.uniform.element.ArgumentElement;
import net.william278.uniform.element.CommandElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static net.william278.uniform.CommandExecutor.methodToExecutor;

@Getter
@Setter
@AllArgsConstructor
public abstract class Command implements CommandProvider {

    private final String name;
    private List<String> aliases = List.of();
    private String description = "";
    @Getter(AccessLevel.NONE)
    private @Nullable Permission permission = null;
    private ExecutionScope scope = ExecutionScope.ALL;

    public Optional<Permission> getPermission() {
        return Optional.ofNullable(permission);
    }

    Command(@NotNull String name) {
        this.name = name;
    }

    Command(@Nullable CommandNode node) {
        if (node == null) {
            throw new IllegalArgumentException("@CommandNode annotation is required on annotated command/sub-commands");
        }
        this.name = node.value();
        this.aliases = List.of(node.aliases());
        this.description = node.description();
        this.scope = node.scope();
        Permission.annotated(node.permission()).ifPresent(this::setPermission);
    }

    public enum ExecutionScope {
        IN_GAME,
        CONSOLE,
        ALL;

        @Nullable
        public <S> Predicate<S> toPredicate(@NotNull BaseCommand<S> command) {
            return this == ALL ? null : user -> this.contains(command.getUser(user));
        }

        public boolean contains(@NotNull CommandUser user) {
            return switch (this) {
                case IN_GAME -> !user.isConsole();
                case CONSOLE -> user.isConsole();
                case ALL -> true;
            };
        }
    }

    static class AnnotatedCommand extends Command {

        private final Object annotated;

        AnnotatedCommand(@NotNull Object annotated) {
            super(annotated.getClass().getAnnotation(CommandNode.class));
            this.annotated = annotated;
            this.setDescriptionFromAnnotation();
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public void provide(@NotNull BaseCommand<?> cmd) {
            // Add syntaxes
            for (Method method : annotated.getClass().getDeclaredMethods()) {
                method.setAccessible(true);
                final Syntax syntax = method.getAnnotation(Syntax.class);
                if (syntax == null) {
                    continue;
                }

                // Default executor
                final CommandElement[] args = getMethodArguments(method);
                if (args.length == 0) {
                    cmd.setDefaultExecutor(methodToExecutor(method, annotated, cmd));
                    continue;
                }

                // Determine predicates
                final Optional<Predicate> perm = Permission.annotated(syntax.permission()).map(p -> p.toPredicate(cmd));
                final Optional<Predicate> scope = Optional.ofNullable(syntax.scope().toPredicate(cmd));
                final Optional<Predicate> combined = scope.map(sp -> perm.map(pp -> sp.and(pp)).orElse(sp));

                // Conditional & unconditional syntax
                final CommandExecutor executor = methodToExecutor(method, annotated, cmd);
                if (combined.isPresent()) {
                    cmd.addConditionalSyntax(combined.get(), executor, args);
                    continue;
                }
                cmd.addSyntax(executor, args);
            }

            // Add subcommands
            for (Class<?> subClass : annotated.getClass().getDeclaredClasses()) {
                if (subClass.getAnnotation(CommandNode.class) == null) {
                    continue;
                }
                try {
                    cmd.addSubCommand(new AnnotatedCommand(
                            subClass.getDeclaredConstructor().newInstance()
                    ));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new IllegalArgumentException(
                            "Failed to create sub-command instance (does it have a zero-arg constructor?)", e
                    );
                }
            }
        }

        @NotNull
        private static CommandElement<?>[] getMethodArguments(@NotNull Method method) {
            try {
                final List<ArgumentElement<?, ?>> elements = new ArrayList<>();
                for (Parameter param : method.getParameters()) {
                    final Argument arg = param.getAnnotation(Argument.class);
                    if (arg == null) {
                        continue;
                    }
                    // Get the argument name
                    final String argName = arg.name().isEmpty() ? param.getName() : arg.name();

                    // Pass parser properties if needed
                    if (arg.parserProperties().length == 0) {
                        elements.add(arg.parser().getDeclaredConstructor()
                                .newInstance().provide(argName));
                        continue;
                    }
                    elements.add(arg.parser().getDeclaredConstructor(String[].class)
                            .newInstance((Object) arg.parserProperties()).provide(argName));
                }
                return elements.toArray(new ArgumentElement[0]);
            } catch (Throwable e) {
                throw new IllegalArgumentException("Failed to create argument elements from method parameters", e);
            }
        }

        private void setDescriptionFromAnnotation() {
            Arrays.stream(annotated.getClass().getFields())
                    .filter(f -> f.getAnnotation(CommandDescription.class) != null)
                    .findFirst().ifPresent(f -> {
                        try {
                            f.setAccessible(true);
                            this.setDescription((String) f.get(annotated));
                        } catch (IllegalAccessException e) {
                            throw new IllegalArgumentException("Failed to set command description from field", e);
                        }
                    });
        }
    }

    public record SubCommand(@NotNull String name, @NotNull List<String> aliases, @Nullable Permission permission,
                             @NotNull ExecutionScope scope, @NotNull CommandProvider provider) {
        public SubCommand(@NotNull String name, @NotNull List<String> aliases, @Nullable Permission permission,
                          @NotNull CommandProvider provider) {
            this(name, aliases, permission, ExecutionScope.ALL, provider);
        }

        public SubCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull CommandProvider provider) {
            this(name, aliases, null, ExecutionScope.ALL, provider);
        }

        public SubCommand(@NotNull String name, @Nullable Permission permission, @NotNull CommandProvider provider) {
            this(name, List.of(), permission, ExecutionScope.ALL, provider);
        }

        public SubCommand(@NotNull String name, @NotNull CommandProvider provider) {
            this(name, List.of(), null, ExecutionScope.ALL, provider);
        }


        @NotNull
        Command command() {
            return new Command(name, aliases, "", permission, scope) {
                @Override
                public void provide(@NotNull BaseCommand<?> command) {
                    provider.provide(command);
                }
            };
        }
    }

}
