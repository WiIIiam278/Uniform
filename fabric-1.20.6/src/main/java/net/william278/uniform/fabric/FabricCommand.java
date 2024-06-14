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

package net.william278.uniform.fabric;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.CommandUser;
import net.william278.uniform.element.ArgumentElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public class FabricCommand extends BaseCommand<ServerCommandSource> {

    public FabricCommand(@NotNull Command command) {
        super(command);
    }

    public FabricCommand(@NotNull String name, @NotNull List<String> aliases) {
        super(name, aliases);
    }

    public FabricCommand(@NotNull String name, @NotNull String description, @NotNull List<String> aliases) {
        super(name, description, aliases);
    }

    protected static ArgumentElement<ServerCommandSource, Item> itemArg(String name) {
        return registryArg(name, Registries.ITEM);
    }

    protected static ArgumentElement<ServerCommandSource, Block> blockArg(String name) {
        return registryArg(name, Registries.BLOCK);
    }

    protected static <T> ArgumentElement<ServerCommandSource, T> registryArg(String name, Registry<T> registry) {
        return new ArgumentElement<>(name, reader -> {
            String itemId = reader.readString();
            final Identifier id;
            try {
                id = Identifier.tryParse(itemId);
            } catch (InvalidIdentifierException e) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
            if (registry.getOrEmpty(id).isEmpty()) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
            return registry.get(id);
        }, (context, builder) -> {
            registry.getIds().forEach(id -> builder.suggest(id.toString()));
            return builder.buildFuture();
        });
    }

    @Override
    @NotNull
    protected CommandUser getUser(@NotNull ServerCommandSource user) {
        return new FabricCommandUser(user);
    }

}
