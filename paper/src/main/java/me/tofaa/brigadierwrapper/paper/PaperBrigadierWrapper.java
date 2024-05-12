package me.tofaa.brigadierwrapper.paper;

import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class PaperBrigadierWrapper {

    private PaperBrigadierWrapper() {}

    private static final Set<PaperCommand> COMMANDS = new HashSet<>();

    public static void register(PaperCommand... commands) {
        Collections.addAll(COMMANDS, commands);
    }

    public static void init(
            @NotNull JavaPlugin plugin
    ) {
        Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onCmd(CommandRegisteredEvent event) {
                for (PaperCommand command : COMMANDS) {
                    event.getRoot().addChild(command.build());
                    COMMANDS.remove(command);
                }
            }
        }, plugin);
    }

}
