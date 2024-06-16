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

Uniform _currently_ targets the following platforms:

<p align="center">

| Platform      | Artifact           | Minecraft  |  Java  |
|---------------|--------------------|:----------:|:------:|
| Common        | `uniform-common`   |     -      | \>`17` |
| Bukkit        | `uniform-bukkit`   | \>`1.17.1` | \>`17` |
| Paper         | `uniform-paper`    | \>`1.17.1` | \>`21` |
| Velocity      | `uniform-velocity` | \>`3.3.0`  | \>`17` |
| Fabric 1.20.1 | `uniform-fabric`   | =`1.20.1`  | \>`17` |
| Fabric 1.20.6 | `uniform-fabric`   | =`1.20.6`  | \>`21` |

</p>

* **Fabric:** Please note Uniform on Fabric requires [adventure-platform-fabric](https://docs.advntr.dev/platform/fabric.html) and the [Fabric API](https://fabricmc.net/) as dependencies. To target Fabric, use `uniform-fabric` as the artifact and `<uniform_version>+<minecraft_version>` as the version (e.g. `net.william278.uniform:uniform-fabric:1.0+1.20.1`).
* **Sponge**: Support for Sponge 8 is also planned in a future version.

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

Then, add the dependency itself. Replace `VERSION` with the latest release version. (e.g., `1.0`) and `PLATFORM` with the platform you are targeting (e.g., `paper`). If you want to target pre-release "snapshot" versions (not recommended), you should use the `/snapshots` repository instead.

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

### Platform-specific commands
Extend the platform-specific `PlatformCommand` class and add your Brigadier syntax.

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
            context.getSource().getBukkitSender().sendMessage(MiniMessage.miniMessage().deserialize(arg));
        }, stringArg("message"));
    }
}
```

### Cross-platform commands
Target `uniform-common` and extend the `Command` class. You'll want to use `BaseCommand#getUser` to get a platform-agnostic User from which you can acquire the adventure `Audience` to send messages to.

```java
import java.awt.*;

public class ExampleCrossPlatCommand extends Command {
    public ExampleCrossPlatCommand() {
        super("example", "cross-platform");
    }

    @Override
    public <S> void provide(@NotNull BaseCommand<S> command) {
        // What gets executed when no args are passed. For tidiness, feel free to delegate this stuff to methods!
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

### Registering
Then, register the command with the platform-specific Uniform instance (e.g. `FabricUniform.getInstance()`, `PaperUniform.getInstance()`, etc...)

## Building
To build Uniform, run `clean build` in the root directory. The output JARs will be in `target/`.

## License
Uniform is licensed under GPL v3 as it derives from BrigadierWrapper. See [LICENSE](https://github.com/WiIIiam278/Uniform/raw/master/LICENSE) for more information.