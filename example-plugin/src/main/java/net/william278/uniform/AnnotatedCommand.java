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

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.william278.uniform.annotations.*;
import net.william278.uniform.element.ArgumentElement;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@CommandNode(
        value = "annotated",
        permission = @PermissionNode(value = "uniform.annotated", defaultValue = Permission.Default.TRUE)
)
public class AnnotatedCommand {

    @CommandDescription // Can use @CommandDescription instead of @CommandNode field for localization support here
    public final String DESCRIPTION = "An example Uniform annotated command";

    public AnnotatedCommand() {
    }

    @Syntax
    void defaultExecutor(CommandUser user) {
        user.getAudience().sendMessage(Component.text("No arguments passed!"));
    }

    @CommandNode("ping")
    static class Ping {

        public Ping() {
        }

        @Syntax
        public void pong(CommandUser user) {
            user.getAudience().sendMessage(Component.text("Pong!"));
        }

        @Syntax
        public void pongMessage(
                CommandUser user,
                @Argument(name = "message", parser = Argument.StringArg.class) String message
        ) {
            user.getAudience().sendMessage(Component.text("Pong! " + message, NamedTextColor.GREEN));
        }

        @Syntax
        public void pongMessageWithColor(
                CommandUser user,
                @Argument(name = "message", parser = Argument.StringArg.class) String message,
                @Argument(name = "color", parser = ColorArg.class) NamedTextColor color
        ) {
            user.getAudience().sendMessage(Component.text("Colored Pong! " + message, color));
        }

        public static class ColorArg extends Argument.ArgumentProvider<NamedTextColor> {
            @Override
            public ArgumentElement<?, NamedTextColor> provide(@NotNull String name) {
                return new ArgumentElement<>(name, (r) -> {
                    final NamedTextColor color = NamedTextColor.NAMES.value(r.readString().toLowerCase(Locale.ENGLISH));
                    if (color == null) {
                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
                    }
                    return color;
                }, (context, builder) -> {
                    NamedTextColor.NAMES.keys().forEach(color -> builder.suggest(color.toLowerCase(Locale.ENGLISH)));
                    return builder.buildFuture();
                });
            }
        }

    }

}
