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

package net.william278.uniform.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.audience.Audience;
import net.minecraft.server.command.ServerCommandSource;
import net.william278.uniform.CommandUser;
import net.william278.uniform.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record FabricCommandUser(@NotNull ServerCommandSource source) implements CommandUser {

    @Override
    public @NotNull Audience getAudience() {
        return (Audience) source.getPlayer();
    }

    @Override
    @Nullable
    public String getName() {
        //#if MC>=260100
        //$$ return source.getTextName();
        //#else
        return source.getName();
        //#endif
    }

    @Override
    @Nullable
    public UUID getUuid() {
        //#if MC>=260100
        //$$ return source.getPlayer() != null ? source.getPlayer().getUUID() : null;
        //#else
        return source.getPlayer() != null ? source.getPlayer().getUuid() : null;
        //#endif
    }

    @Override
    public boolean checkPermission(@NotNull Permission permission) {
        // Use the permission-level overload for IF_OP rather than querying the source's op level directly,
        // as vanilla's permission level API changed in 1.21.11
        if (permission.defaultValue() == Permission.Default.IF_OP) {
            return Permissions.check(source, permission.node(), 4);
        }
        return Permissions.check(source, permission.node(), permission.defaultValue().check(false));
    }

}
