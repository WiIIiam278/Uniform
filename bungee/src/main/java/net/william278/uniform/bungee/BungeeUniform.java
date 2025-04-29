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

package net.william278.uniform.bungee;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.CommandUser;
import net.william278.uniform.Uniform;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

/**
 * A class for registering commands with the Bungee server's command manager
 *
 * @since 1.3.3
 */
@SuppressWarnings("unused")
public final class BungeeUniform implements Uniform {
	private static Plugin PLUGIN;
	static BungeeUniform INSTANCE;
	private static BungeeAudiences AUDIENCES;


	@Getter
	@Setter
	Function<Object, CommandUser> commandUserSupplier = (user) -> new BungeeCommandUser((CommandSender) user);

	private BungeeUniform(@NotNull Plugin plugin) {
		PLUGIN = plugin;
	}

	static BungeeAudiences getAudiences() {
		return AUDIENCES != null ? AUDIENCES : (AUDIENCES = BungeeAudiences.create(PLUGIN));
	}

	/**
	 * Get the BungeeUniform instance for registering commands
	 *
	 * @param server The server instance
	 * @return The BungeeUniform instance
	 * @since 1.3.3
	 */
	@NotNull
	public static BungeeUniform getInstance(@NotNull Plugin server) {
		return INSTANCE != null ? INSTANCE : (INSTANCE = new BungeeUniform(server));
	}

	/**
	 * Register a command with the server's command manager
	 *
	 * @param commands The commands to register
	 * @param <S>      The command source type
	 * @param <T>      The command type
	 * @since 1.3.3
	 */
	@SafeVarargs
	@Override
	public final <S, T extends BaseCommand<S>> void register(@NotNull T... commands) {
		getCommandMap().putAll(Arrays.stream(commands)
				.map(command -> ((BungeeCommand) command))
				.collect(java.util.stream.Collectors.toMap(
						BaseCommand::getName,
						c -> (Command) c.getImpl(this)
				)));
	}

	/**
	 * Register a command with the server's command manager
	 *
	 * @param commands The commands to register
	 * @since 1.3.3
	 */
	@Override
	public void register(@NotNull net.william278.uniform.Command... commands) {
		register(Arrays.stream(commands).map(BungeeCommand::new).toArray(BungeeCommand[]::new));
	}

	/**
	 * Get the command map from the server's command manager
	 */
	private Map<String, Command> getCommandMap() {
		try {
			return getFieldValue(PLUGIN.getProxy().getPluginManager().getClass(), PLUGIN.getProxy().getPluginManager(),
					"commandMap", Map.class);
		} catch (FeatureFailedException ex) {
			throw new FeatureFailedException("Unable to access command map", ex);
		}
	}

	/**
	 * Access the private commandMap from PluginManager using reflection
	 *
	 * @return The command map or null if access failed
	 */
	<T, F> F getFieldValue(Class<? extends T> clazz, T object, String fieldName, Class<F> fieldType) {
		Field field;
		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException ex) {
			throw new FeatureFailedException("Unable to find field " + fieldName, ex);
		}
		Object fieldValue;
		try {
			field.setAccessible(true);
			fieldValue = field.get(object);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			throw new FeatureFailedException("Unable to access field " + fieldName, ex);
		}
		if (!fieldType.isInstance(fieldValue)) {
			throw new FeatureFailedException(
					"Field " + fieldName + " not an instance of " + fieldType.getName());
		}
		return fieldType.cast(fieldValue);
	}

	public static class FeatureFailedException extends RuntimeException {
		public FeatureFailedException(String message, Throwable cause) {
			super(message, cause);
		}

		public FeatureFailedException(String message) {
			super(message);
		}
	}
}
