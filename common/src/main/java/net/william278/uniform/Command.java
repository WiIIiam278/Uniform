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

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class Command implements CommandProvider {

    private final String name;
    private List<String> aliases = List.of();
    private String description = "";
    @Getter(AccessLevel.NONE)
    private @Nullable Permission permission = null;

    public Optional<Permission> getPermission() {
        return Optional.ofNullable(permission);
    }

    public record SubCommand(@NotNull String name, @NotNull List<String> aliases, @Nullable Permission permission, @NotNull CommandProvider provider) {
        public SubCommand(@NotNull String name, @NotNull List<String> aliases, @NotNull CommandProvider provider) {
            this(name, aliases, null, provider);
        }

        public SubCommand(@NotNull String name, @NotNull CommandProvider provider) {
            this(name, List.of(),null, provider);
        }

        public SubCommand(@NotNull String name, @Nullable Permission permission, @NotNull CommandProvider provider) {
            this(name, List.of(), permission, provider);
        }

        @NotNull
        Command command() {
            return new Command(name, aliases, "", permission) {
                @Override
                public void provide(@NotNull BaseCommand<?> command) {
                    provider.provide(command);
                }
            };
        }
    }

}
