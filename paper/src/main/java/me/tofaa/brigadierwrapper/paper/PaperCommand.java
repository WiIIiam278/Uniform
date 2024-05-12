package me.tofaa.brigadierwrapper.paper;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.tofaa.brigadierwrapper.Command;
import me.tofaa.brigadierwrapper.element.ArgumentElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class PaperCommand extends Command<BukkitBrigadierCommandSource> {

    public PaperCommand(@NotNull String name, @NotNull String... aliases) {
        super(name, aliases);
    }

    protected static ArgumentElement<BukkitBrigadierCommandSource, Material> materialArg(String name) {
        return new ArgumentElement<>(name, reader -> {
            String materialName = reader.readString();
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
            return material;
        }, (context, builder) -> {
            for (Material material : Material.values()) {
                builder.suggest(material.name());
            }
            return builder.buildFuture();
        });
    }

    protected static ArgumentElement<BukkitBrigadierCommandSource, Collection<? extends Player>> playerArg(String name) {
        return new ArgumentElement<>(name, reader -> {
            String playerName = reader.readString();
            if (playerName.equals("@a")) {
                return Bukkit.getOnlinePlayers();
            }
            var player = Bukkit.getPlayer(playerName);
            if (player == null) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
            return List.of(player);
        }, (context, builder) -> {
            builder.suggest("@a");
            for (Player player : Bukkit.getOnlinePlayers()) {
                builder.suggest(player.getName());
            }
            return builder.buildFuture();
        });
    }

}
