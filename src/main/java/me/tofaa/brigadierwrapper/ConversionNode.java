package me.tofaa.brigadierwrapper;

import me.tofaa.brigadierwrapper.element.CommandElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static me.tofaa.brigadierwrapper.Graph.commandToElement;

record ConversionNode<S>(@NotNull CommandElement<S> element, @Nullable Execution<S> execution,
                                 @NotNull Map<CommandElement<S>, ConversionNode<S>> nextMap) {

    static <S> @NotNull ConversionNode<S> fromCommand(@NotNull Command<S> command) {
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

        for (Command<S> subCommand : command.getSubCommands()) {
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