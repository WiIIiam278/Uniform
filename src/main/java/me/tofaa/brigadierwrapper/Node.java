package me.tofaa.brigadierwrapper;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.tofaa.brigadierwrapper.element.CommandElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

record Node<S>(@NotNull CommandElement<S> element, @Nullable Execution<S> execution, @NotNull List<Node<S>> children) {

    static <S> @NotNull Node<S> command(@NotNull Command<S> command) {
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