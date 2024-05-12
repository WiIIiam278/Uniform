package me.tofaa.brigadierwrapper.element;


import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ArgumentElement<S, T>(@NotNull String name, @NotNull ArgumentType<T> type,
                                    @Nullable SuggestionProvider<S> suggestionProvider) implements CommandElement<S> {

    @Override
    public @NotNull ArgumentBuilder<S, ?> toBuilder() {
        var builder = RequiredArgumentBuilder.<S, T>argument(this.name, this.type);
        if (this.suggestionProvider != null) builder.suggests(this.suggestionProvider);
        return builder;
    }
}