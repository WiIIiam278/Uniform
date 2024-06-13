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

Uniform _currently_ targets the following platforms, (in addition to `uniform-common` which you can compile against in multi-platform plugins):

| Platform      | Artifact                | Minecraft  | Java  |
|---------------|-------------------------|:----------:|:-----:|
| Paper         | `uniform-paper`         | \>`1.20.4` | >`17` |
| Velocity      | `uniform-velocity`      | \>`3.3.0`  | >`17` |
| Fabric 1.20.1 | `uniform-fabric-1.20.1` | =`1.20.1`  | >`17` |
| Fabric 1.20.6 | `uniform-fabric-1.20.6` | =`1.20.6`  | >`21` |

Uniform _plans_ to support the following platforms:

| Platform       | Version    | Java  |
|----------------|------------|:-----:|
| Spigot&dagger; | \>`1.17.1` | >`17` |

&dagger; Brigadier commands are wrapped into non-brigadier Bukkit plugin commands for legacy Spigot support.

## Using
### Platform-specific commands
Extend the platform-specific `PlatformCommand` class and implement the `execute` method.

```java
public class ExampleCommand extends PaperCommand {

    public ExampleCommand() {
        super("example", "platform-specific");
        addSyntax((context) -> {
            context.getSource().getBukkitSender().sendMessage("Woah!!!!");
            String arg = context.getArgument("message", String.class);
            context.getSource().getBukkitSender().sendMessage(MiniMessage.miniMessage().deserialize(arg));
        }, stringArg("message"));
    }

}
```

### Cross-platform commands
Target `uniform-common` and implement the `Command` class.

```java
public class ExampleCrossPlatCommand implements Command {

    @Override
    @NotNull
    public String getNamespace() {
        return "example";
    }

    @Override
    @NotNull
    public List<String> getAliases() {
        return List.of("cross-plat");
    }

    @Override
    public <S> void provide(@NotNull BaseCommand<S> command) {
        command.setCondition(source -> true);
        command.setDefaultExecutor((ctx) -> {
            // Use command.getUser(ctx.getSource()) to get the user
            final Audience user = command.getUser(ctx.getSource()).getAudience();
            user.sendMessage(Component.text("Hello, world!"));
        });
    }

}
```

### Registering
Then, register the command with the platform-specific Uniform instance (e.g. `FabricUniform.getInstance()`, `PaperUniform.getInstance()`, etc...)

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

## Building
To build Uniform, run `clean build` in the root directory. The output JARs will be in `target/`.

## License
Uniform is licensed under GPL v3 as it derives from BrigadierWrapper. See [LICENSE](https://github.com/WiIIiam278/Uniform/raw/master/LICENSE) for more information.