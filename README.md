<!--suppress ALL -->
<p align="center">
    <img src="images/banner.png" alt="Claim Operations Library" />
    <a href="https://github.com/WiIIiam278/Uniform/actions/workflows/ci.yml">
        <img src="https://img.shields.io/github/actions/workflow/status/WiIIiam278/Uniform/ci.yml?branch=master&logo=github"/>
    </a> 
    <a href="https://repo.william278.net/#/releases/net/william278/uniform/">
        <img src="https://repo.william278.net/api/badge/latest/releases/net/william278/uniform/uniform-common?color=00fb9a&name=Maven&prefix=v"/>
    </a> 
    <a href="https://discord.gg/tVYhJfyDWG">
        <img src="https://img.shields.io/discord/818135932103557162.svg?label=&logo=discord&logoColor=fff&color=7389D8&labelColor=6A7EC2" />
    </a> 
</p>
<br/>

**Uniform** is cross-platform wrapper for making Brigadier commands, based on [`BrigadierWrapper` by Tofaa2](https://github.com/Tofaa2/BrigadierWrapper/), which itself was inspired by [EmortalMC's `command-system`](https://github.com/emortalmc/command-system).

## Compatibility

Versions are available on maven in the format `net.william278.uniform:ARTIFACT:VERSION`. See below for a table of supported platforms.

Note that Uniform versions omit the `v` prefix. Fabric versions are suffixed with the target Minecraft version (e.g. `1.2.1+1.21`) and also require Fabric API installed on the server. Sponge versions are suffixed with the target Sponge API version (e.g. `1.2.1+11`).

<table align="center">
    <thead>
        <tr>
            <th colspan="5">Uniform Version Table</th>
        </tr>
        <tr>
            <th>Platform</th>
            <th>Artifact</th>
            <th>Minecraft ver.</th>
            <th>Java ver.</th>
            <th>Uniform ver.</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Common</td>
            <td><code>uniform-common</code></td>
            <td><code>N/A</code></td>
            <td align="center">≥<code>17</code></td>
            <td align="center"><img src="https://img.shields.io/github/v/tag/WiIIiam278/Uniform?color=000000&label=%20&style=flat"/></td>
        </tr>
        <tr>
            <th colspan="5">Supported Platforms</th>
        </tr>
        <tr>
            <td>Bukkit / Spigot</td>
            <td><code>uniform-bukkit</code></td>
            <td rowspan="2">≥<code>1.17.1</code></td>
            <td rowspan="3" align="center">≥<code>17</code></td>
            <td rowspan="7" align="center"><img src="https://img.shields.io/github/v/tag/WiIIiam278/Uniform?color=000000&label=%20&style=flat"/></td>
        </tr>
        <tr>
            <td>Paper</td>
            <td><code>uniform-paper</code></td>
        </tr>
        <tr>
            <td>Velocity <small>(3.3.0)</small></td>
            <td><code>uniform-velocity</code></td>
            <td>≥<code>1.8.9</code></td>
        </tr>
        <tr>
            <td>Sponge <small>(api 11)</small></td>
            <td><code>uniform-sponge</code></td>
            <td>=<code>1.20.6</code></td>
            <td rowspan="4" align="center">≥<code>21</code></td>
        </tr>
        <tr>
            <td>Fabric <small>(1.20.1)</small></td>
            <td rowspan="3"><code>uniform-fabric</code></td>
            <td>=<code>1.20.1</code></td>
        <tr>
            <td>Fabric <small>(1.21.1)</small></td>
            <td>=<code>1.21.1</code></td>
        </tr>
        <tr>
            <td>Fabric <small>(1.21.4)</small></td>
            <td>=<code>1.21.3</code></td>
        </tr>
        <tr>
            <th colspan="5">Formerly Supported Platforms</th>
        </tr>
        <tr>
            <td>Fabric <small>(1.20.6)</small></td>
            <td><code>uniform-fabric</code></td>
            <td>=<code>1.20.6</code></td>
            <td align="center" rowspan="3">≥<code>21</code></td>
            <td align="center"><code>v1.1.8</code></td>
        </tr>
        <tr>
            <td>Fabric <small>(1.21)</small></td>
            <td><code>uniform-fabric</code></td>
            <td>=<code>1.21</code></td>
            <td align="center"><code>v1.2.1</code></td>
        </tr>
        <tr>
            <td>Fabric <small>(1.21.3)</small></td>
            <td><code>uniform-fabric</code></td>
            <td>=<code>1.21.3</code></td>
            <td align="center"><code>v1.2.2</code></td>
        </tr>
    </tbody>
</table>

Example: To target Uniform on Bukkit, the artifact is `net.william278.uniform:uniform-bukkit:1.2.1` (check that this version is up-to-date &ndash; make sure you target the latest available!).

## Setup
Uniform is available [on Maven](https://repo.william278.net/#/releases/net/william278/uniform/). You can browse the Javadocs [here](https://repo.william278.net/javadoc/releases/net/william278/uniform/latest).

<details>
<summary>Gradle setup instructions</summary> 

First, add the Maven repository to your `build.gradle` file:
```groovy
repositories {
    maven { url "https://repo.william278.net/releases" }
}
```

Then, add the dependency itself. Replace `VERSION` with the latest release version. (e.g., `1.2.1`) and `PLATFORM` with the platform you are targeting (e.g., `paper`). If you want to target pre-release "snapshot" versions (not recommended), you should use the `/snapshots` repository instead.

```groovy
dependencies {
    implementation "net.william278.uniform:uniform-PLATFORM:VERSION"
}
```
</details>

Using Maven/something else? There's instructions on how to include Uniform on [the repo browser](https://repo.william278.net/#/releases/net/william278/uniform).

## Basic use
Uniform lets you create commands either natively per-platform, or cross-platform (by compiling against `uniform-common` in a common module, then implementing `uniform-PLATFORM` in each platform, getting the platform specific Uniform manager instance and registering your commands).

Check `example-plugin` for a full example of a cross-platform command being registered on Paper.

### Cross-platform commands
Cross-platform commands can be created by registering `Command` objects; you can create these from `@CommandNode` annotated objects, or by extending `Command` and providing these yourself.

#### Using annotations
You can use the `@CommandNode` annotations to easily create cross-platform Brigadier commands (since: v1.2). This is the recommended way to create commands.

```java
@CommandNode(
        value = "helloworld",
        aliases = {"hello", "hi"},
        description = "A simple hello world command",
        permission = @PermissionNode(
                value = "example.command.helloworld",
                defaultValue = Permission.Default.TRUE
        )
)
public class AnnotatedCommand {

    @Syntax
    public void execute(CommandUser user) {
        user.getAudience().sendMessage(Component.text("Hello, world!"));
    }

    @Syntax
    public void pongMessage(
            CommandUser user,
            @Argument(name = "message", parser = Argument.StringArg.class) String message
    ) {
        user.getAudience().sendMessage(Component.text("Hello, " + message, NamedTextColor.GREEN));
    }
    
    @CommandNode(
            value = "subcommand",
            aliases = {"sub", "hi"}
    )
    static class SubCommand {
        @Syntax
        public void execute(CommandUser user) {
            user.getAudience().sendMessage(Component.text("Subcommand executed!"));
        }
    }

}
```

#### By extending the Command class.
You can also extend the `Command` class to create a Command object you can register. You'll want to use `BaseCommand#getUser` to get a platform-agnostic User from which you can acquire the adventure `Audience` to send messages to.

```java
public class ExampleCrossPlatCommand extends Command {
    public ExampleCrossPlatCommand() {
        super("example", "cross-platform");
    }

    @Override
    public <S> void provide(@NotNull BaseCommand<S> command) {
        // What gets executed when no args are passed. 
        // For tidiness, feel free to delegate this stuff to methods!
        command.setDefaultExecutor((context) -> {
            // Use command.getUser(context.getSource()) to get the user
            final Audience user = command.getUser(context.getSource()).getAudience();
            user.sendMessage(Component.text("Hello, world!"));
        });

        // Add syntax to the command
        command.addSyntax((context) -> {
            final Audience user = command.getUser(ctx.getSource()).getAudience();
            user.sendMessage(Component.text("Woah!!!!"));
            String arg = context.getArgument("message", String.class);
            user.sendMessage(MiniMessage.miniMessage().deserialize(arg));
        }, stringArg("message"));

        // Sub-commands, too
        command.addSubCommand("subcommand", (sub) -> {
            sub.setDefaultExecutor((context) -> {
                final Audience user = sub.getUser(context.getSource()).getAudience();
                user.sendMessage(Component.text("Subcommand executed!"));
            });
        });
    }
}
```

### Platform-specific commands
If you need platform-specific features, extend the platform-specific `PlatformCommand` class and add your Brigadier syntax.

```java
public class ExampleCommand extends PaperCommand {
    public ExampleCommand() {
        super("example", "platform-specific");
        command.setDefaultExecutor((context) -> {
            context.getSource().getBukkitSender().sendMessage("Hello, world!");
        });
        addSyntax((context) -> {
            context.getSource().getBukkitSender().sendMessage("Woah!!!!");
            String arg = context.getArgument("message", String.class);
            context.getSource().getBukkitSender()
                .sendMessage(MiniMessage.miniMessage().deserialize(arg));
        }, stringArg("message"));
    }
}
```


### Registering
Then, register the command with the platform-specific Uniform instance (e.g. `FabricUniform.getInstance()`, `PaperUniform.getInstance()`, etc...)

## Building
To build Uniform, run `clean build` in the root directory. The output JARs will be in `target/`.

## License
Uniform is licensed under GPL v3 as it derives from BrigadierWrapper. See [LICENSE](https://github.com/WiIIiam278/Uniform/raw/master/LICENSE) for more information.