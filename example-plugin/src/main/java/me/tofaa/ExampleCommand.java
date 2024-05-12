package me.tofaa;

import me.tofaa.brigadierwrapper.paper.PaperCommand;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ExampleCommand extends PaperCommand {

    public ExampleCommand() {
        super("example", "silly-command");
        addSyntax((context) -> {
            context.getSource().getBukkitSender().sendMessage("Woah!!!!");
            String arg = context.getArgument("message", String.class);
            context.getSource().getBukkitSender().sendMessage(MiniMessage.miniMessage().deserialize(arg));
        }, stringArg("message"));
    }

}
