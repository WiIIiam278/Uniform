package me.tofaa.brigadierwrapper;

import com.mojang.brigadier.context.CommandContext;
import org.jetbrains.annotations.NotNull;

public interface CommandExecutor<S> {

    void execute(@NotNull CommandContext<S> context);
}
