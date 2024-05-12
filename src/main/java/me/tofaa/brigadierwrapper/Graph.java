package me.tofaa.brigadierwrapper;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.tofaa.brigadierwrapper.element.CommandElement;
import me.tofaa.brigadierwrapper.element.LiteralElement;
import org.jetbrains.annotations.NotNull;

record Graph<S>(@NotNull Node<S> root) {


    static <S> @NotNull Graph<S> create(@NotNull Command<S> command) {
        return new Graph<>(Node.command(command));
    }

    static <S> @NotNull CommandElement<S> commandToElement(@NotNull Command<S> command) {
        return new LiteralElement<>(command.getName());
    }

    @NotNull LiteralCommandNode<S> build() {
        CommandNode<S> node = this.root.build();
        if (!(node instanceof LiteralCommandNode<S> literalNode)) {
            throw new IllegalStateException("Root node is somehow not a literal node. This should be impossible.");
        }
        return literalNode;
    }
}