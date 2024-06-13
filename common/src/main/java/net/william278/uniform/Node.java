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
import com.mojang.brigadier.tree.CommandNode;
import net.william278.uniform.element.CommandElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

record Node<S>(@NotNull CommandElement<S> element, @Nullable Execution<S> execution, @NotNull List<Node<S>> children) {

    static <S> @NotNull Node<S> command(@NotNull BaseCommand<S> command) {
        return ConversionNode.fromCommand(command).toNode();
    }

    @NotNull
    CommandNode<S> build() {
        ArgumentBuilder<S, ?> builder = this.element.toBuilder();
        if (this.execution != null) this.execution.addToBuilder(builder);

        for (Node<S> child : this.children) {
            builder.then(child.build());
        }

        return builder.build();
    }

}