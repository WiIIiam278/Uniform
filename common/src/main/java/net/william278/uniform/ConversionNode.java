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

import net.william278.uniform.element.CommandElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.william278.uniform.Graph.commandToElement;

record ConversionNode<S>(@NotNull CommandElement<S> element, @Nullable Execution<S> execution,
                         @NotNull Map<CommandElement<S>, ConversionNode<S>> nextMap) {

    static <S> @NotNull ConversionNode<S> fromCommand(@NotNull BaseCommand<S> command) {
        ConversionNode<S> root = new ConversionNode<>(commandToElement(command), Execution.fromCommand(command));

        for (CommandSyntax<S> syntax : command.getSyntaxes()) {
            ConversionNode<S> syntaxNode = root;

            for (CommandElement<S> element : syntax.elements()) {
                boolean last = element == syntax.elements().get(syntax.elements().size() - 1);
                syntaxNode = syntaxNode.nextMap.computeIfAbsent(element, e -> {
                    Execution<S> execution = last ? Execution.fromSyntax(syntax) : null;
                    return new ConversionNode<>(e, execution);
                });
            }
        }

        for (BaseCommand<S> subCommand : command.getSubCommands()) {
            root.nextMap.put(commandToElement(subCommand), fromCommand(subCommand));
        }

        return root;
    }

    ConversionNode(@NotNull CommandElement<S> element, @Nullable Execution<S> execution) {
        this(element, execution, new LinkedHashMap<>());
    }

    Node<S> toNode() {
        @SuppressWarnings("unchecked") // this is fine - we only put Node<S> in to this array
        Node<S>[] nodes = (Node<S>[]) new Node<?>[this.nextMap.size()];

        int i = 0;
        for (ConversionNode<S> entry : this.nextMap.values()) {
            nodes[i++] = entry.toNode();
        }

        return new Node<>(this.element, this.execution, List.of(nodes));
    }

}