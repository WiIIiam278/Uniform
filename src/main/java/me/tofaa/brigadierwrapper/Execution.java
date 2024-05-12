package me.tofaa.brigadierwrapper;


import com.mojang.brigadier.builder.ArgumentBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

record Execution<S>(@NotNull Predicate<S> predicate, @Nullable CommandExecutor<S> defaultExecutor, @Nullable CommandExecutor<S> executor,
                    @Nullable Predicate<S> condition) implements Predicate<S> {

    private static final Executor CACHED_EXECUTOR = Executors.newCachedThreadPool();


    static <S> @NotNull Execution<S> fromCommand(@NotNull Command<S> command) {
        CommandExecutor<S> defaultExecutor = command.getDefaultExecutor();
        Predicate<S> defaultCondition = command.getCondition();

        CommandExecutor<S> executor = defaultExecutor;
        Predicate<S> condition = defaultCondition;
        for (CommandSyntax<S> syntax : command.getSyntaxes()) {
            if (!syntax.elements().isEmpty()) continue;
            executor = syntax.executor();
            condition = syntax.condition();
            break;
        }

        return new Execution<>(source -> defaultCondition == null || defaultCondition.test(source), defaultExecutor, executor, condition);
    }

    static <S> @NotNull Execution<S> fromSyntax(@NotNull CommandSyntax<S> syntax) {
        CommandExecutor<S> executor = syntax.executor();
        Predicate<S> condition = syntax.condition();
        return new Execution<>(source -> condition == null || condition.test(source), null, executor, condition);
    }

    @Override
    public boolean test(@NotNull S source) {
        return this.predicate.test(source);
    }

    void addToBuilder(@NotNull ArgumentBuilder<S, ?> builder) {
        if (this.condition != null) builder.requires(this.condition);
        if (this.executor != null) {
            builder.executes(convertExecutor(this.executor));
        } else if (this.defaultExecutor != null) {
            builder.executes(convertExecutor(this.defaultExecutor));
        }
    }

    private static <S> com.mojang.brigadier.@NotNull Command<S> convertExecutor(@NotNull CommandExecutor<S> executor) {
        return context -> {
            CACHED_EXECUTOR.execute(() -> executor.execute(context));
            return 1;
        };
    }
}
