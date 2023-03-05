package net.greenfieldmc.greenbot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;

public class Start extends JavaPlugin {

    private BukkitTask botTask;
    private Greenbot bot;

    @Override
    public void onEnable() {
        getLogger().info("Starting Greenbot discord bot...");
        this.botTask = Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                bot = new Greenbot(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void onDisable() {
        if (botTask != null) botTask.cancel();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("greenbot.reload")) sender.sendMessage(ChatColor.RED + "You do not have permission to reload Greenbot's configuration.");
        else {
            bot.reload();
            sender.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Reloaded Greenbot's configuration.");
            return true;
        }
        return false;
    }
}
