package me.tofaa;

import me.tofaa.brigadierwrapper.paper.PaperBrigadierWrapper;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {


    @Override
    public void onEnable() {
        PaperBrigadierWrapper.init(this);
        PaperBrigadierWrapper.register(new ExampleCommand());
    }
}