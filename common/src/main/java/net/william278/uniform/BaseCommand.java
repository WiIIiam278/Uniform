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

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.Getter;
import net.william278.uniform.element.ArgumentElement;
import net.william278.uniform.element.CommandElement;
import net.william278.uniform.element.LiteralElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

@Getter
@SuppressWarnings("unused")
public abstract class BaseCommand<S> {

    protected static final ExecutorService CACHED_EXECUTOR = Executors.newCachedThreadPool();

    private final String name;
    private final String description;
    private final List<String> aliases;
    protected final List<CommandSyntax<S>> syntaxes = new ArrayList<>();
    protected final List<BaseCommand<S>> subCommands = new ArrayList<>();

    @Nullable
    private Predicate<S> condition;
    @Nullable
    private CommandExecutor<S> defaultExecutor;

    public BaseCommand(@NotNull Command command) {
        this.name = command.getName();
        this.aliases = command.getAliases();
        this.description = command.getDescription();
        setExecutionScope(command.getScope());
        command.getPermission().ifPresent(this::setPermission);
        command.provide(this);
    }

    public BaseCommand(@NotNull String name, @NotNull String description,
                       @NotNull List<String> aliases) {
        this.name = name;
        this.aliases = aliases;
        this.description = description;
    }

    public BaseCommand(@NotNull String name, @NotNull List<String> aliases) {
        this(name, "", aliases);
    }

    @NotNull
    public CommandUser getUser(@NotNull Object user) {
        return getUniform().getCommandUserSupplier().apply(user);
    }

    public void setCondition(@NotNull Predicate<S> condition) {
        this.condition = condition;
    }

    public void addCondition(@NotNull Predicate<S> condition) {
        if (this.condition == null) {
            this.condition = condition;
        } else {
            this.condition = this.condition.and(condition);
        }
    }

    public void setPermission(@NotNull Permission permission) {
        this.addPermissions(permission);
    }

    public void addPermissions(@NotNull Permission... permission) {
        for (Permission singlePerm : permission) {
            this.addCondition(this.createPermission(singlePerm));
        }
    }

    public void addPermissions(@NotNull List<Permission> permissionList) {
        permissionList.forEach(perm -> this.addCondition(this.createPermission(perm)));
    }

    public void addPermissions(@NotNull String... permission) {
        this.addPermissions(Arrays.stream(permission)
                .map(Permission::new)
                .toArray(Permission[]::new));
    }

    public void addPermission(@NotNull String permission, @NotNull Permission.Default permissionDefault) {
        this.addPermissions(new Permission(permission, permissionDefault));
    }

    public void setExecutionScope(@NotNull Command.ExecutionScope scope) {
        final Predicate<S> predicate = scope.toPredicate(this);
        if (predicate != null) {
            this.addCondition(predicate);
        }
    }

    public void setDefaultExecutor(@Nullable CommandExecutor<S> executor) {
        this.defaultExecutor = executor;
    }

    @SafeVarargs
    public final void addConditionalSyntax(@Nullable Predicate<S> condition, @NotNull CommandExecutor<S> executor,
                                           @NotNull CommandElement<S>... elements) {
        var syntax = new CommandSyntax<>(condition, executor, List.of(elements));
        this.syntaxes.add(syntax);
    }

    @SafeVarargs
    public final void addSyntax(@NotNull CommandExecutor<S> executor, @NotNull CommandElement<S>... elements) {
        this.addConditionalSyntax(null, executor, elements);
    }

    public void addSubCommand(@NotNull BaseCommand<S> command) {
        this.subCommands.add(command);
    }

    public void addSubCommand(@NotNull String name, @NotNull CommandProvider provider) {
        this.addSubCommand(new Command.SubCommand(name, provider).command());
    }

    public void addSubCommand(@NotNull String name, @NotNull Permission permission,
                              @NotNull CommandProvider provider) {
        this.addSubCommand(new Command.SubCommand(name, permission, provider).command());
    }

    public void addSubCommand(@NotNull String name, @NotNull List<String> aliases,
                              @NotNull CommandProvider provider) {
        this.addSubCommand(new Command.SubCommand(name, aliases, provider).command());
    }

    public void addSubCommand(@NotNull String name, @NotNull List<String> aliases,
                              @NotNull Permission permission, @NotNull CommandProvider provider) {
        this.addSubCommand(new Command.SubCommand(name, aliases, permission, provider).command());
    }

    public void addSubCommand(@NotNull String name, @NotNull List<String> aliases,
                              @NotNull Permission permission, @NotNull Command.ExecutionScope scope,
                              @NotNull CommandProvider provider) {
        this.addSubCommand(new Command.SubCommand(name, aliases, permission, scope, provider).command());
    }

    public abstract void addSubCommand(@NotNull Command command);

    public abstract Uniform getUniform();

    @NotNull
    public Predicate<S> createPermission(@NotNull Permission permission) {
        return permission.toPredicate(this);
    }

    @NotNull
    public LiteralCommandNode<S> build() {
        return Graph.create(this).build();
    }

    @NotNull
    public LiteralArgumentBuilder<S> createBuilder() {
        return Graph.create(this).literal(this.name);
    }

    @NotNull
    public static <S> LiteralElement<S> literal(@NotNull String name) {
        return new LiteralElement<>(name);
    }

    @NotNull
    public static <S> ArgumentElement<S, String> greedyString(@NotNull String name) {
        return arg(name, StringArgumentType.greedyString());
    }

    @NotNull
    public static <S> ArgumentElement<S, String> string(@NotNull String name) {
        return arg(name, StringArgumentType.string());
    }

    @NotNull
    public static <S> ArgumentElement<S, String> word(@NotNull String name) {
        return arg(name, StringArgumentType.word());
    }

    @NotNull
    public static <S> ArgumentElement<S, Integer> intNum(@NotNull String name) {
        return arg(name, IntegerArgumentType.integer());
    }

    @NotNull
    public static <S> ArgumentElement<S, Integer> intNum(@NotNull String name, int min) {
        return arg(name, IntegerArgumentType.integer(min));
    }

    @NotNull
    public static <S> ArgumentElement<S, Integer> intNum(@NotNull String name, int min, int max) {
        return arg(name, IntegerArgumentType.integer(min, max));
    }

    @NotNull
    public static <S> ArgumentElement<S, Float> floatNum(@NotNull String name) {
        return arg(name, FloatArgumentType.floatArg());
    }

    @NotNull
    public static <S> ArgumentElement<S, Float> floatNum(@NotNull String name, float min) {
        return arg(name, FloatArgumentType.floatArg(min));
    }

    @NotNull
    public static <S> ArgumentElement<S, Float> floatNum(@NotNull String name, float min, float max) {
        return arg(name, FloatArgumentType.floatArg(min, max));
    }

    @NotNull
    public static <S> ArgumentElement<S, Double> doubleNum(@NotNull String name) {
        return arg(name, DoubleArgumentType.doubleArg());
    }

    @NotNull
    public static <S> ArgumentElement<S, Double> doubleNum(@NotNull String name, double min) {
        return arg(name, DoubleArgumentType.doubleArg(min));
    }

    @NotNull
    public static <S> ArgumentElement<S, Double> doubleNum(@NotNull String name, double min, double max) {
        return arg(name, DoubleArgumentType.doubleArg(min, max));
    }

    @NotNull
    public static <S> ArgumentElement<S, Boolean> bool(@NotNull String name) {
        return arg(name, BoolArgumentType.bool());
    }

    @NotNull
    public static <S, T> ArgumentElement<S, T> arg(@NotNull String name, @NotNull ArgumentType<T> type,
                                                   @Nullable SuggestionProvider<S> suggestionProvider) {
        return new ArgumentElement<>(name, type, suggestionProvider, false);
    }

    @NotNull
    public static <S, T> ArgumentElement<S, T> arg(@NotNull String name, @NotNull ArgumentType<T> type) {
        return arg(name, type, null);
    }

    @NotNull
    public static <S, T extends Enum<T>> ArgumentElement<S, T> enumArgument(String name, Class<T> enumClass) {
        return new ArgumentElement<>(name, reader -> {
            String enumName = reader.readString();
            T enumValue;
            try {
                enumValue = Enum.valueOf(enumClass, enumName);
            } catch (IllegalArgumentException e) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
            return enumValue;
        }, (context, builder) -> {
            for (T enumValue : enumClass.getEnumConstants()) {
                builder.suggest(enumValue.name());
            }
            return builder.buildFuture();
        });
    }

    public static abstract class BaseCommandBuilder<S> {
        protected final String name;
        protected String description = "";
        protected List<String> aliases = new ArrayList<>();
        protected Predicate<S> condition = null;
        protected CommandExecutor<S> defaultExecutor = null;
        protected Command.ExecutionScope executionScope = Command.ExecutionScope.ALL;
        protected final List<Permission> permissions = new ArrayList<>();
        protected final List<BaseCommand<S>> subCommands = new ArrayList<>();
        protected final Map<String, ArgumentElement<S, ?>> argumentElements = new HashMap<>();
        protected final List<CommandSyntax<S>> syntaxes = new ArrayList<>();

        public BaseCommandBuilder(String name) {
            this.name = name;
        }

        public final BaseCommandBuilder<S> setDescription(@NotNull String description) {
            this.description = description;
            return this;
        }

        public final BaseCommandBuilder<S> setAliases(@NotNull List<String> aliases) {
            this.aliases = aliases;
            return this;
        }

        public final BaseCommandBuilder<S> addPermissions(@NotNull Permission... permissions) {
            this.permissions.addAll(List.of(permissions));
            return this;
        }

        public final BaseCommandBuilder<S> setPermission(@NotNull Permission permission) {
            return addPermissions(permission);
        }

        public final BaseCommandBuilder<S> addPermissions(@NotNull String... permissions) {
            return addPermissions(Arrays.stream(permissions)
                    .map(Permission::new)
                    .toArray(Permission[]::new));
        }

        public final BaseCommandBuilder<S> setPermission(@NotNull String permission) {
            return addPermissions(permission);
        }

        public final BaseCommandBuilder<S> setCondition(@NotNull Predicate<S> condition) {
            this.condition = condition;
            return this;
        }

        public final BaseCommandBuilder<S> addCondition(@NotNull Predicate<S> condition) {
            if (this.condition == null) {
                this.condition = condition;
            } else {
                this.condition = this.condition.and(condition);
            }
            return this;
        }

        public final BaseCommandBuilder<S> addSubCommand(@NotNull BaseCommand<S> paperCommand) {
            this.subCommands.add(paperCommand);
            return this;
        }

        public final BaseCommandBuilder<S> setDefaultExecutor(@NotNull CommandExecutor<S> executor) {
            this.defaultExecutor = executor;
            return this;
        }

        public final BaseCommandBuilder<S> addArgument(String argName, @NotNull ArgumentType<?> argumentType,
                                                       @NotNull SuggestionProvider<S> suggestionProvider) {
            return addArgument(new ArgumentElement<>(argName, argumentType, suggestionProvider));
        }

        public final BaseCommandBuilder<S> addArgument(@NotNull ArgumentElement<S, ?> argumentElement) {
            this.argumentElements.put(argumentElement.name(), argumentElement);
            return this;
        }

        public final BaseCommandBuilder<S> setExecutionScope(@NotNull Command.ExecutionScope executionScope) {
            this.executionScope = executionScope;
            return this;
        }

        public final BaseCommandBuilder<S> addStringArgument(String argName, @NotNull SuggestionProvider<S> suggestionProvider) {
            return addArgument(argName, StringArgumentType.string(), suggestionProvider);
        }

        public final BaseCommandBuilder<S> execute(@NotNull CommandExecutor<S> executor, String... requiredArgs) {
            return executeConditional(null, executor, requiredArgs);
        }

        public final BaseCommandBuilder<S> executeConditional(@Nullable Predicate<S> condition,
                                                              @NotNull CommandExecutor<S> executor,
                                                              String... requiredArgs) {
            this.syntaxes.add(new CommandSyntax<>(condition, executor, Arrays.stream(requiredArgs).map(argString -> {
                CommandElement<S> argumentElement = argumentElements.get(argString);
                if (argumentElement == null) {
                    throw new IllegalArgumentException("Argument " + argString + " not found");
                }
                return argumentElement;
            }).toList()));
            return this;
        }

        public abstract BaseCommand<S> build();

    }

}