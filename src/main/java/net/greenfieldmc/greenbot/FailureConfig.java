package net.greenfieldmc.greenbot;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FailureConfig {

    //a map of discord user snowflakes to channel snowflakes
    private final Map<Long, Failure> failureMap;
    private final YamlConfiguration conf;
    private final File file;

    public FailureConfig(Plugin plugin) throws IOException {
        this.failureMap = new HashMap<>();
        plugin.getDataFolder().mkdirs();
        this.file = new File(plugin.getDataFolder() + File.separator + "failuremap.yml");
        if (file.exists()) {
            this.conf = YamlConfiguration.loadConfiguration(file);

            if (conf.getConfigurationSection("failures") == null) return;

            var userList =  conf.getConfigurationSection("failures").getKeys(false);
            userList.forEach(u -> {
                var uid = Long.parseLong(u);
                var cid = conf.getLong("failures." + u + ".cid");
                var time = conf.getLong("features." + u + ".time");

                failureMap.put(uid, new Failure(cid, time));
            });



        } else {
            plugin.getLogger().warning("No failuremap file found for the bot. Creating it");
            if (!file.createNewFile()) {
                throw new RuntimeException("Failed to create a new file. Exiting...");
            }
            this.conf = YamlConfiguration.loadConfiguration(file);
        }
    }

    public boolean hasFailureChannel(long discordId) {
        return failureMap.containsKey(discordId);
    }

    public Failure getFailureChannel(long discordId) {
        return failureMap.get(discordId);
    }

    public void addFailureChannel(long discordId, long channelId) {
        long time = System.currentTimeMillis();
        failureMap.put(discordId, new Failure(channelId, time));
        conf.set("failures." + discordId + ".cid", channelId);
        conf.set("failures." + discordId + ".time", time);
        save();
    }

    public void removeFailureChannelForUser(long discordId) {
        Bukkit.getLogger().info("Deleted failure channel for user: " + discordId);
        conf.set("failures." + discordId, null);
        failureMap.remove(discordId);
        save();
    }

    public long findDiscordIdByChannel(long channelId) {
        for (var e : failureMap.entrySet()) {
            if (e.getValue().channelId == channelId) return e.getKey();
        }
        return -1;
    }

    public Map<Long, Failure> getFailures() {
        return failureMap;
    }

    private void save() {
        try {
            conf.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public record Failure(long channelId, long creationTime) {}

}
