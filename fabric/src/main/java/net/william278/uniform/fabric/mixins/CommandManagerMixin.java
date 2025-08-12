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

//#if MC>=12108
package net.william278.uniform.fabric.mixins;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandTreeS2CPacket.class)
public class CommandManagerMixin {

    @Redirect(
            method = "createNodeData",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/command/argument/ArgumentTypes;getArgumentTypeProperties(Lcom/mojang/brigadier/arguments/ArgumentType;)Lnet/minecraft/command/argument/serialize/ArgumentSerializer$ArgumentTypeProperties;"
            )
    )
    private static ArgumentSerializer.ArgumentTypeProperties<?> onConstruct(ArgumentType<?> type) {
        ArgumentSerializer.ArgumentTypeProperties<?> properties;
        try {
            properties = ArgumentTypes.getArgumentTypeProperties(type);
        } catch (IllegalArgumentException e) {
            properties = ArgumentTypes.getArgumentTypeProperties(StringArgumentType.string());
        }
        return properties;
    }

}
//#endif