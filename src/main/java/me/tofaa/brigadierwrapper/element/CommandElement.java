package me.tofaa.brigadierwrapper.element;

import com.mojang.brigadier.builder.ArgumentBuilder;
import org.jetbrains.annotations.NotNull;

public interface CommandElement<S> {

    @NotNull ArgumentBuilder<S, ?> toBuilder();
}