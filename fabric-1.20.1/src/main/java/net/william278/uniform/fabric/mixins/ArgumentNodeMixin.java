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

package net.william278.uniform.fabric.mixins;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket.ArgumentNode;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ArgumentNode.class)
public class ArgumentNodeMixin {

    @Invoker("<init>")
    private static ArgumentNode createArgumentNode(String name, ArgumentSerializer.ArgumentTypeProperties<?> properties,
                                                   @Nullable Identifier id) {
        throw new AssertionError();
    }

    @Redirect(method = "<init>(Lcom/mojang/brigadier/tree/ArgumentCommandNode;)V", at = @At("HEAD"))
    private static ArgumentNode onConstruct(ArgumentCommandNode<CommandSource, ?> node) {
        ArgumentSerializer.ArgumentTypeProperties<?> properties;
        try {
            properties = ArgumentTypes.getArgumentTypeProperties(node.getType());
        } catch (IllegalArgumentException e) {
            properties = ArgumentTypes.getArgumentTypeProperties(StringArgumentType.string());
        }

        return createArgumentNode(
            node.getName(),
            properties,
            node.getCustomSuggestions() != null ? SuggestionProviders.computeId(node.getCustomSuggestions()) : null
        );
    }

}
