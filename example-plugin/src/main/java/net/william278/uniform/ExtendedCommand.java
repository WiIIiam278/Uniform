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
import net.william278.uniform.element.ArgumentElement;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static net.william278.uniform.BaseCommand.greedyString;

// Example using the API to extend "Command"
public class ExtendedCommand extends Command {

    public ExtendedCommand() {
        super("example");
        setDescription("An example command for Uniform");
        setAliases(List.of("helloworld"));
        setPermission(Permission.defaultIfOp("uniform.example"));
    }

    @Override
    public void provide(@NotNull BaseCommand<?> command) {
        command.setDefaultExecutor((ctx) -> {
            final CommandUser user = command.getUser(ctx.getSource());
            user.getAudience().sendMessage(Component.text("Hello, World!"));
        });
        command.addSubCommand("message", (sub) -> sub.addSyntax((ctx) -> {
            final CommandUser user = sub.getUser(ctx.getSource());
            user.getAudience().sendMessage(Component.text(ctx.getArgument("message", String.class)));
        }, greedyString("message")));
        command.addSubCommand("flavor", (sub) -> sub.addSyntax((ctx) -> {
            final CommandUser user = sub.getUser(ctx.getSource());
            final IceCreamFlavor flavor = ctx.getArgument("flavor", IceCreamFlavor.class);
            switch (flavor) {
                case VANILLA -> user.getAudience().sendMessage(Component.text("Vanilla ice cream is fine!"));
                case CHOCOLATE -> user.getAudience().sendMessage(Component.text("Chocolate ice cream is kino!"));
                case STRAWBERRY -> user.getAudience().sendMessage(Component.text("Strawberry ice cream is ok..."));
            }
        }, exampleCustomArg()));
    }

    private static <S> ArgumentElement<S, ExtendedCommand.IceCreamFlavor> exampleCustomArg() {
        return new ArgumentElement<>("flavor", reader -> {
            final String flavor = reader.readString();
            try {
                return IceCreamFlavor.valueOf(flavor.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException e) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
            }
        }, (context, builder) -> {
            Arrays.stream(IceCreamFlavor.values()).forEach(
                flavor -> builder.suggest(flavor.name().toLowerCase(Locale.ENGLISH))
            );
            return builder.buildFuture();
        });
    }

    enum IceCreamFlavor {
        VANILLA,
        CHOCOLATE,
        STRAWBERRY
    }

}
