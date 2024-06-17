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
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/network/packet/s2c/play/CommandTreeS2CPacket$ArgumentNode")
public class ArgumentNodeMixin {

    @Mutable
    @Final
    @Shadow
    private String name;
    @Mutable
    @Final
    @Shadow
    private ArgumentSerializer.ArgumentTypeProperties<?> properties;
    @Mutable
    @Final
    @Nullable
    @Shadow
    private Identifier id; // Actually, the suggestion provider. Possibly the worst field name in the entire yarn mappings?

    @Inject(method = "<init>(Lcom/mojang/brigadier/tree/ArgumentCommandNode;)V", at = @At("RETURN"))
    private <A> void onConstruct(ArgumentCommandNode<CommandSource, A> node, CallbackInfo ci) {
        this.name = node.getName();
        this.id = node.getCustomSuggestions() != null ? SuggestionProviders.computeId(node.getCustomSuggestions()) : null;
        try {
            this.properties = ArgumentTypes.get(node.getType()).getArgumentTypeProperties(node.getType());
        } catch (IllegalArgumentException e) {
            this.properties = ArgumentTypes.get(StringArgumentType.string()).getArgumentTypeProperties(StringArgumentType.string());
        }
    }

}
