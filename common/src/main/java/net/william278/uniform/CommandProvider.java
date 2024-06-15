package net.william278.uniform;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CommandProvider {

    void provide(@NotNull BaseCommand<?> command);

}
