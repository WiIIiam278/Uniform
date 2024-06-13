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
import net.william278.uniform.element.ArgumentElement;
import net.william278.uniform.element.CommandElement;
import net.william278.uniform.element.LiteralElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public abstract class Command<S> {

    private final String name;
    private final String[] aliases;

    private @Nullable Predicate<S> condition;
    private @Nullable CommandExecutor<S> defaultExecutor;

    private final List<CommandSyntax<S>> syntaxes = new ArrayList<>();
    private final List<Command<S>> subCommands = new ArrayList<>();

    public Command(@NotNull String name, @NotNull String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    public Command(@NotNull String name) {
        this(name, new String[0]);
    }

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

    protected final void addSubCommand(@NotNull Command<S> command) {
        this.subCommands.add(command);
    }

    public final @NotNull LiteralCommandNode<S> build() {
        return Graph.create(this).build();
    }

    public final @NotNull String getName() {
        return this.name;
    }

    public final @NotNull String[] getAliases() {
        return this.aliases;
    }

    final @Nullable Predicate<S> getCondition() {
        return this.condition;
    }

    final @Nullable CommandExecutor<S> getDefaultExecutor() {
        return this.defaultExecutor;
    }

    final @NotNull Collection<CommandSyntax<S>> getSyntaxes() {
        return this.syntaxes;
    }

    final @NotNull Collection<Command<S>> getSubCommands() {
        return this.subCommands;
    }

    protected static <S> @NotNull LiteralElement<S> literalArg(@NotNull String name) {
        return new LiteralElement<>(name);
    }

    protected static <S> @NotNull ArgumentElement<S, String> stringArg(@NotNull String name) {
        return argument(name, StringArgumentType.string());
    }

    protected static <S> @NotNull ArgumentElement<S, Integer> integerArg(@NotNull String name) {
        return argument(name, IntegerArgumentType.integer());
    }

    protected static <S> @NotNull ArgumentElement<S, Integer> integerArg(@NotNull String name, int min) {
        return argument(name, IntegerArgumentType.integer(min));
    }

    protected static <S> @NotNull ArgumentElement<S, Integer> integerArg(@NotNull String name, int min, int max) {
        return argument(name, IntegerArgumentType.integer(min, max));
    }

    protected static <S> @NotNull ArgumentElement<S, Float> floatArg(@NotNull String name) {
        return argument(name, FloatArgumentType.floatArg());
    }

    protected static <S> @NotNull ArgumentElement<S, Float> floatArg(@NotNull String name, float min) {
        return argument(name, FloatArgumentType.floatArg(min));
    }

    protected static <S> @NotNull ArgumentElement<S, Float> floatArg(@NotNull String name, float min, float max) {
        return argument(name, FloatArgumentType.floatArg(min, max));
    }

    protected static <S> @NotNull ArgumentElement<S, Boolean> booleanArg(@NotNull String name) {
        return argument(name, BoolArgumentType.bool());
    }

    protected static <S, T> @NotNull ArgumentElement<S, T> argument(@NotNull String name, @NotNull ArgumentType<T> type,
                                                                    @Nullable SuggestionProvider<S> suggestionProvider) {
        return new ArgumentElement<>(name, type, suggestionProvider);
    }

    protected static <S, T> @NotNull ArgumentElement<S, T> argument(@NotNull String name, @NotNull ArgumentType<T> type) {
        return argument(name, type, null);
    }

}