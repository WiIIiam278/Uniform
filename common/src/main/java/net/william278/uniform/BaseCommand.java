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
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.AccessLevel;
import lombok.Getter;
import net.william278.uniform.element.ArgumentElement;
import net.william278.uniform.element.CommandElement;
import net.william278.uniform.element.LiteralElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public abstract class BaseCommand<S> {

    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter
    private final List<String> aliases;

    public BaseCommand(@NotNull Command command) {
        this.name = command.getName();
        this.aliases = command.getAliases();
        this.description = command.getDescription();
        command.provide(this);
    }

    public BaseCommand(@NotNull String name, @NotNull String description, @NotNull List<String> aliases) {
        this.name = name;
        this.aliases = aliases;
        this.description = description;
    }

    public BaseCommand(@NotNull String name, @NotNull List<String> aliases) {
        this(name, "", aliases);
    }

    @Nullable
    @Getter(AccessLevel.PACKAGE)
    private Predicate<S> condition;
    @Nullable
    @Getter(AccessLevel.PACKAGE)
    private CommandExecutor<S> defaultExecutor;
    @Getter(AccessLevel.PACKAGE)
    private final List<CommandSyntax<S>> syntaxes = new ArrayList<>();
    @Getter(AccessLevel.PACKAGE)
    private final List<BaseCommand<S>> subCommands = new ArrayList<>();

    @NotNull
    protected abstract CommandUser getUser(@NotNull S user);

    protected final void setCondition(@NotNull Predicate<S> condition) {
        this.condition = condition;
    }

    protected final void setDefaultExecutor(@NotNull CommandExecutor<S> executor) {
        this.defaultExecutor = executor;
    }

    @SafeVarargs
    protected final void addConditionalSyntax(@Nullable Predicate<S> condition, @NotNull CommandExecutor<S> executor,
                                              @NotNull CommandElement<S>... elements) {
        var syntax = new CommandSyntax<>(condition, executor, List.of(elements));
        this.syntaxes.add(syntax);
    }

    @SafeVarargs
    protected final void addSyntax(@NotNull CommandExecutor<S> executor, @NotNull CommandElement<S>... elements) {
        this.addConditionalSyntax(null, executor, elements);
    }

    protected final void addSubCommand(@NotNull BaseCommand<S> command) {
        this.subCommands.add(command);
    }

    @NotNull
    public final LiteralCommandNode<S> build() {
        return Graph.create(this).build();
    }

    @NotNull
    protected static <S> LiteralElement<S> literalArg(@NotNull String name) {
        return new LiteralElement<>(name);
    }

    @NotNull
    protected static <S> ArgumentElement<S, String> stringArg(@NotNull String name) {
        return argument(name, StringArgumentType.string());
    }

    @NotNull
    protected static <S> ArgumentElement<S, Integer> integerArg(@NotNull String name) {
        return argument(name, IntegerArgumentType.integer());
    }

    @NotNull
    protected static <S> ArgumentElement<S, Integer> integerArg(@NotNull String name, int min) {
        return argument(name, IntegerArgumentType.integer(min));
    }

    @NotNull
    protected static <S> ArgumentElement<S, Integer> integerArg(@NotNull String name, int min, int max) {
        return argument(name, IntegerArgumentType.integer(min, max));
    }

    @NotNull
    protected static <S> ArgumentElement<S, Float> floatArg(@NotNull String name) {
        return argument(name, FloatArgumentType.floatArg());
    }

    @NotNull
    protected static <S> ArgumentElement<S, Float> floatArg(@NotNull String name, float min) {
        return argument(name, FloatArgumentType.floatArg(min));
    }

    @NotNull
    protected static <S> ArgumentElement<S, Float> floatArg(@NotNull String name, float min, float max) {
        return argument(name, FloatArgumentType.floatArg(min, max));
    }

    @NotNull
    protected static <S> ArgumentElement<S, Boolean> booleanArg(@NotNull String name) {
        return argument(name, BoolArgumentType.bool());
    }

    @NotNull
    protected static <S, T> ArgumentElement<S, T> argument(@NotNull String name, @NotNull ArgumentType<T> type,
                                                           @Nullable SuggestionProvider<S> suggestionProvider) {
        return new ArgumentElement<>(name, type, suggestionProvider);
    }

    @NotNull
    protected static <S, T> ArgumentElement<S, T> argument(@NotNull String name, @NotNull ArgumentType<T> type) {
        return argument(name, type, null);
    }

}