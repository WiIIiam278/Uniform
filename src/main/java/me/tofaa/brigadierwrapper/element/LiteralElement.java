package me.tofaa.brigadierwrapper.element;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.jetbrains.annotations.NotNull;

public record LiteralElement<S>(@NotNull String name) implements CommandElement<S> {

    @Override
    public @NotNull ArgumentBuilder<S, ?> toBuilder() {
        return LiteralArgumentBuilder.literal(this.name);
    }
}