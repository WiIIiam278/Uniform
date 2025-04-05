/*
 * This file is part of Uniform, licensed under the GNU General Public License v3.0.
 *
 *  Copyright (c) Tofaa2
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.william278.uniform;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.william278.uniform.paper.PaperCommand;
import net.william278.uniform.paper.PaperUniform;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@SuppressWarnings("unused")
public class UniformExample extends JavaPlugin {

    @Override
    public void onEnable() {
        PaperUniform uniform = PaperUniform.getInstance(this);
        uniform.register(new ExtendedCommand(), getBuiltCommand(),new AnnotatedCommand());
    }

    public BaseCommand getBuiltCommand() {
        return PaperCommand.builder("builded")
                .setDescription("A builded command")
                .setAliases(List.of("builded", "build"))
                .setPermission(new Permission("uniform.builded"))
                .addSubCommand(builtSubCommand())
                .build();
    }

    private BaseCommand<CommandSourceStack> builtSubCommand() {
        return PaperCommand.builder("get-pet")
                .setDescription("Gets a pet")
                .setAliases(List.of("pet", "lilpet"))
                .setPermission(new Permission("uniform.pet"))
                .addStringArgument("petName", ((commandContext, suggestionsBuilder) -> {
                    if (commandContext.getSource().getSender().hasPermission("uniform.cat")) {
                        suggestionsBuilder.suggest("cat");
                    }
                    return suggestionsBuilder.suggest("dog").buildFuture();
                }))
                .execute(commandContext -> {
                    String petName = commandContext.getArgument("petName", String.class);
                    commandContext.getSource().getSender().sendMessage("You have a " + petName);
                },"petName") //Here you can specify the argument to use for this "command syntax"
                .execute(commandContext -> {
                    commandContext.getSource().getSender().sendMessage("Missing argument petName");
                })           //Syntax without arguments
                .build();
    }

}