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

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public record Permission(@NotNull String node, @NotNull Default defaultValue) {

    Permission(@NotNull String node) {
        this(node, Default.FALSE);
    }

    @NotNull
    public static Permission defaultIfOp(@NotNull String node) {
        return new Permission(node, Default.IF_OP);
    }

    @NotNull
    public static Permission defaultTrue(@NotNull String node) {
        return new Permission(node, Default.TRUE);
    }

    @NotNull
    public static Permission defaultFalse(@NotNull String node) {
        return new Permission(node, Default.FALSE);
    }

    public enum Default {
        IF_OP,
        TRUE,
        FALSE;

        public boolean check(boolean op) {
            return switch (this) {
                case IF_OP -> op;
                case TRUE -> true;
                case FALSE -> false;
            };
        }
    }

    @NotNull
    public <S> Predicate<S> toPredicate(@NotNull BaseCommand<S> command) {
        return user -> command.getUser(user).checkPermission(this);
    }

}
