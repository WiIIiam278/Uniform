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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.william278.uniform.element.CommandElement;
import net.william278.uniform.element.LiteralElement;
import org.jetbrains.annotations.NotNull;

record Graph<S>(@NotNull Node<S> root) {

    static <S> @NotNull Graph<S> create(@NotNull BaseCommand<S> command) {
        return new Graph<>(Node.command(command));
    }

    static <S> @NotNull CommandElement<S> commandToElement(@NotNull BaseCommand<S> command) {
        return new LiteralElement<>(command.getName());
    }

    @NotNull
    LiteralCommandNode<S> build() {
        CommandNode<S> node = this.root.build();
        if (!(node instanceof LiteralCommandNode<S> literalNode)) {
            throw new IllegalStateException("Root node is somehow not a literal node. This should be impossible.");
        }
        return literalNode;
    }

    @NotNull
    LiteralArgumentBuilder<S> literal(@NotNull String name) {
        final LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder.literal(name);
        final CommandNode<S> command = this.root.build();
        builder.executes(command.getCommand());
        this.root.children().forEach(child -> builder.then(child.build()));
        return builder;
    }


}