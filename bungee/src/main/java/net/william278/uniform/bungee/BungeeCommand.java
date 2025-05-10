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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.william278.uniform.BaseCommand;
import net.william278.uniform.Command;
import net.william278.uniform.Permission;
import net.william278.uniform.Uniform;
import net.william278.uniform.element.ArgumentElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public class BungeeCommand extends BaseCommand<CommandSender> {

	private @Nullable Permission permission;

	public BungeeCommand(@NotNull Command command) {
		super(command);
		this.permission = command.getPermission().orElse(null);
	}

	public BungeeCommand(@NotNull String name, @NotNull List<String> aliases) {
		super(name, aliases);
	}

	public BungeeCommand(@NotNull String name, @NotNull String description,
						 @NotNull List<String> aliases) {
		super(name, description, aliases);
	}

	public static BungeeCommandBuilder builder(@NotNull String name) {
		return new BungeeCommandBuilder(name);
	}

	public static ArgumentElement<CommandSender, ServerInfo> server(@NotNull ProxyServer server, @NotNull String name,
																	@NotNull SuggestionProvider<CommandSender> suggestionProvider) {
		ArgumentType<ServerInfo> argumentType = reader -> {
			String s = reader.readUnquotedString();
			ServerInfo server1 = server.getServerInfo(s);
			if (server1 == null) {
				throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
			}
			return server1;
		};
		return new ArgumentElement<>(name, argumentType, suggestionProvider);
	}

	public static ArgumentElement<CommandSender, ServerInfo> server(@NotNull ProxyServer server, @NotNull String name) {
		return server(server, name, (context, builder) -> {
			server.getServers().forEach((name1, server1) -> {
				builder.suggest(server1.getName());
			});
			return builder.buildFuture();
		});
	}

	public static ArgumentElement<CommandSender, CommandSender> source(@NotNull ProxyServer server, @NotNull String name,
																	   @NotNull SuggestionProvider<CommandSender> suggestionProvider) {
		ArgumentType<CommandSender> argumentType = reader -> {
			String s = reader.readUnquotedString();
			CommandSender source = server.getPlayer(s);
			if (source == null) {
				throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
			}
			return source;
		};
		return new ArgumentElement<>(name, argumentType, suggestionProvider);
	}

	public static ArgumentElement<CommandSender, CommandSender> source(@NotNull ProxyServer server, @NotNull String name) {
		return source(server, name, (context, builder) -> {
			for (ProxiedPlayer source : server.getPlayers()) {
				builder.suggest(source.getName());
			}
			return builder.buildFuture();
		});
	}

	@Override
	public void addSubCommand(@NotNull Command command) {
		addSubCommand(new BungeeCommand(command));
	}

	@Override
	public Uniform getUniform() {
		return BungeeUniform.INSTANCE;
	}

	@NotNull
	Impl getImpl(@NotNull Uniform uniform) {
		return new Impl(uniform, this);
	}

	static final class Impl extends net.md_5.bungee.api.plugin.Command implements TabExecutor {

		private final CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher<>();
		private final @Nullable Permission permission;

		public Impl(@NotNull Uniform uniform, @NotNull BungeeCommand command) {
			super(command.getName(),
					command.permission != null ? command.permission.node() : null,
					command.getAliases().toArray(new String[0]));
			this.dispatcher.register(command.createBuilder());
			this.permission = command.permission;
		}

		@Override
		public void execute(CommandSender commandSender, String[] args) {
			try {
				dispatcher.execute(getInput(args), commandSender);
			} catch (CommandSyntaxException e) {
				getAudience(commandSender).sendMessage(Component
						.translatable("command.context.parse_error", NamedTextColor.RED)
						.args(
								Component.text(e.getRawMessage().getString()),
								Component.text(e.getCursor()),
								Component.text(e.getContext())
						));
			} catch (Exception e) {
				getAudience(commandSender).sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
			}
		}

		@NotNull
		@Override
		public Iterable<String> onTabComplete(CommandSender sender, String[] args)
				throws IllegalArgumentException {
			if (!testPermissionSilent(sender)) {
				return List.of();
			}
			final String passed = getInput(args);
			return dispatcher.getCompletionSuggestions(
							dispatcher.parse(passed, sender),
							passed.length() // Spigot API limitation - we can only TAB complete the full text length :( - Also making a guess that this is the same on Bungee
					)
					.thenApply(suggestions -> suggestions.getList().stream().map(Suggestion::getText).toList())
					.join();
		}

		public boolean testPermissionSilent(@NotNull CommandSender target) {
			if (permission == null || permission.node().isBlank()) {
				return true;
			}
			return new BungeeCommandUser(target).checkPermission(permission);
		}

		@NotNull
		private Audience getAudience(@NotNull CommandSender sender) {
			return BungeeUniform.getAudiences().sender(sender);
		}

		@NotNull
		private String getInput(@NotNull String[] args) {
			return args.length == 0 ? getName() : "%s %s".formatted(getName(), String.join(" ", args));
		}
	}

	public static class BungeeCommandBuilder extends BaseCommandBuilder<CommandSender, BungeeCommandBuilder> {

		public BungeeCommandBuilder(@NotNull String name) {
			super(name);
		}

		public final BungeeCommandBuilder addSubCommand(@NotNull Command command) {
			subCommands.add(new BungeeCommand(command));
			return this;
		}

		@Override
		public @NotNull BungeeCommand build() {
			var command = new BungeeCommand(name, description, aliases);
			command.addPermissions(permissions);
			subCommands.forEach(command::addSubCommand);
			command.setDefaultExecutor(defaultExecutor);
			command.syntaxes.addAll(syntaxes);
			command.setExecutionScope(executionScope);
			command.setCondition(condition);

			return command;
		}

		public BungeeCommand register(@NotNull Plugin plugin) {
			final BungeeCommand builtCmd = build();
			BungeeUniform.getInstance(plugin).register(builtCmd);
			return builtCmd;
		}
	}
}