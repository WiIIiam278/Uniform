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

package net.william278.uniform.paper;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import net.kyori.adventure.audience.Audience;
import net.william278.uniform.CommandUser;
import net.william278.uniform.Permission;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@SuppressWarnings("removal")
public record LegacyPaperCommandUser(@NotNull BukkitBrigadierCommandSource source) implements CommandUser {

    @Override
    @NotNull
    public Audience getAudience() {
        return source.getBukkitSender();
    }

    @Override
    @Nullable
    public String getName() {
        return source.getBukkitEntity() != null ? source.getBukkitEntity().getName() : null;
    }

    @Override
    @Nullable
    public UUID getUuid() {
        return source.getBukkitEntity() != null ? source.getBukkitEntity().getUniqueId() : null;
    }

    @Override
    public boolean checkPermission(@NotNull Permission permission) {
        if (source.getBukkitSender().isPermissionSet(permission.node())) {
            return source.getBukkitSender().hasPermission(permission.node());
        }
        return permission.defaultValue().check(
            source.getBukkitSender().isOp() || source.getBukkitSender() instanceof ConsoleCommandSender
        );
    }

}
