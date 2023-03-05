package net.greenfieldmc.greenbot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;

public class Start extends JavaPlugin {

    private BukkitTask botTask;

    @Override
    public void onEnable() {
        getLogger().info("Starting Greenbot discord bot...");
        this.botTask = Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                new Greenbot(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void onDisable() {
        if (botTask != null) botTask.cancel();
    }
}
