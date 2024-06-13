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


import com.mojang.brigadier.builder.ArgumentBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

record Execution<S>(@NotNull Predicate<S> predicate, @Nullable CommandExecutor<S> defaultExecutor, @Nullable CommandExecutor<S> executor,
                    @Nullable Predicate<S> condition) implements Predicate<S> {

    private static final Executor CACHED_EXECUTOR = Executors.newCachedThreadPool();


    static <S> @NotNull Execution<S> fromCommand(@NotNull Command<S> command) {
        CommandExecutor<S> defaultExecutor = command.getDefaultExecutor();
        Predicate<S> defaultCondition = command.getCondition();

        CommandExecutor<S> executor = defaultExecutor;
        Predicate<S> condition = defaultCondition;
        for (CommandSyntax<S> syntax : command.getSyntaxes()) {
            if (!syntax.elements().isEmpty()) continue;
            executor = syntax.executor();
            condition = syntax.condition();
            break;
        }

        return new Execution<>(source -> defaultCondition == null || defaultCondition.test(source), defaultExecutor, executor, condition);
    }

    static <S> @NotNull Execution<S> fromSyntax(@NotNull CommandSyntax<S> syntax) {
        CommandExecutor<S> executor = syntax.executor();
        Predicate<S> condition = syntax.condition();
        return new Execution<>(source -> condition == null || condition.test(source), null, executor, condition);
    }

    @Override
    public boolean test(@NotNull S source) {
        return this.predicate.test(source);
    }

    void addToBuilder(@NotNull ArgumentBuilder<S, ?> builder) {
        if (this.condition != null) builder.requires(this.condition);
        if (this.executor != null) {
            builder.executes(convertExecutor(this.executor));
        } else if (this.defaultExecutor != null) {
            builder.executes(convertExecutor(this.defaultExecutor));
        }
    }

    private static <S> com.mojang.brigadier.@NotNull Command<S> convertExecutor(@NotNull CommandExecutor<S> executor) {
        return context -> {
            CACHED_EXECUTOR.execute(() -> executor.execute(context));
            return 1;
        };
    }
}
