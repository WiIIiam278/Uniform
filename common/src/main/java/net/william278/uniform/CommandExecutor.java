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

import com.mojang.brigadier.context.CommandContext;
import net.william278.uniform.annotations.Argument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface CommandExecutor<S> {

    void execute(@NotNull CommandContext<S> context);

    @NotNull
    static <S> CommandExecutor<S> methodToExecutor(@NotNull Method method, @NotNull Object instance,
                                                   @NotNull BaseCommand<?> cmd) {
        return (context) -> {
            try {
                method.invoke(instance, injectParams(method, context, cmd));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to invoke command executor from annotated method", e);
            }
        };
    }

    @Nullable
    private static Object @NotNull [] injectParams(@NotNull Method method, @NotNull CommandContext<?> context,
                                                   @NotNull BaseCommand<?> cmd) {
        final Object[] params = new Object[method.getParameterCount()];
        for (int i = 0; i < method.getParameterCount(); i++) {
            final Parameter param = method.getParameters()[i];
            final Class<?> type = param.getType();
            final Argument arg = param.getAnnotation(Argument.class);
            if (arg != null) {
                params[i] = context.getArgument(arg.name(), type);
                continue;
            }
            if (type.isAssignableFrom(CommandUser.class)) {
                params[i] = cmd.getUser(context.getSource());
                continue;
            }
            if (type.isAssignableFrom(context.getClass())) {
                params[i] = context;
                continue;
            }
            params[i] = null;
        }
        return params;
    }

}
