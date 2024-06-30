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

package net.william278.uniform.sponge;

import net.kyori.adventure.audience.Audience;
import net.william278.uniform.CommandUser;
import net.william278.uniform.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Tristate;

import java.util.UUID;

public record SpongeCommandUser(@NotNull CommandCause executor) implements CommandUser {

    @Override
    @NotNull
    public Audience getAudience() {
        return executor.audience();
    }

    @Override
    @Nullable
    public String getName() {
        return executor instanceof Player player ? player.name() : null;
    }

    @Override
    @Nullable
    public UUID getUuid() {
        return executor instanceof Player player ? player.uniqueId() : null;
    }

    @Override
    public boolean checkPermission(@NotNull Permission permission) {
        final Tristate state = executor.permissionValue(permission.toString());
        if (state == Tristate.UNDEFINED && permission.defaultValue() == Permission.Default.IF_OP) {
            return executor.hasPermission(permission.toString());
        }
        return state == Tristate.TRUE;
    }

}
