package net.greenfieldmc.greenbot;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Config {

    private final String botToken;
    private final long guildId;
    private final long testingChannelId;
    private final long testingRankId;
    private boolean hasBeenAddedToServer;

    private final long failureMessagesCategory;
    private final List<Long> failureMessageCategoryAllowedRanks;
    private final int autoDeleteFailureDays;

    public Config(Plugin plugin) throws IOException {
        plugin.getDataFolder().mkdirs();
        File file = new File(plugin.getDataFolder() + File.separator + "config.yml");
        if (file.exists()) {
            var conf = YamlConfiguration.loadConfiguration(file);
            if (conf.get("botToken") == null ||
                    conf.get("guildId") == null ||
                    conf.get("testingChannelId") == null ||
                    conf.get("testingRankId") == null ||
                    conf.get("hasBeenAddedToServer") == null ||
                    conf.get("failureMessagesCategory") == null ||
                    conf.get("failureMessageCategoryAllowedRanks") == null ||
                    conf.get("autoDeleteFailureDays") == null) {
                throw new RuntimeException("Configuration file does not have all necessary keys to load. Please delete the configuration and let the program regenerate it.");
            }
            else {
                this.hasBeenAddedToServer = conf.getBoolean("hasBeenAddedToServer");
                conf.set("hasBeenAddedToServer", true);
                this.botToken = conf.getString("botToken");
                this.guildId = conf.getLong("guildId");
                this.testingChannelId = conf.getLong("testingChannelId");
                this.testingRankId = conf.getLong("testingRankId");
                this.failureMessagesCategory = conf.getLong("failureMessagesCategory");
                this.failureMessageCategoryAllowedRanks = conf.getLongList("failureMessageCategoryAllowedRanks");
                this.autoDeleteFailureDays = conf.getInt("autoDeleteFailureDays");
                conf.save(file);
            }

        } else {
            plugin.getLogger().warning("No configuration file found for the bot. Creating a default configuration.");
            if (!file.createNewFile()) {
                throw new RuntimeException("Failed to create a new file. Exiting...");
            }
            var conf = YamlConfiguration.loadConfiguration(file);
            conf.set("hasBeenAddedToServer", true);
            conf.set("botToken", "put bot token here");
            conf.set("guildId", "put guild id here");
            conf.set("testingChannelId", "put testing channel id here");
            conf.set("testingRankId", "put the testing rank ID here");
            conf.set("failureMessagesCategory", "put the category ID here");
            conf.set("failureMessageCategoryAllowedRanks", List.of(-1L, -2L));
            conf.set("autoDeleteFailureDays", 14);
            conf.save(file);
            throw new RuntimeException("Set valid configuration values");
        }

        if (!hasBeenAddedToServer) plugin.getLogger().warning("The bot has not yet been added to your server, add it by following the link: https://discord.com/oauth2/authorize?client_id=1081436720618283058&scope=bot&permissions=8");
    }

    public String getBotToken() {
        return botToken;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getTestingChannelId() {
        return testingChannelId;
    }

    public long getTestingRankId() {
        return testingRankId;
    }

    public long getFailureMessagesCategory() {
        return failureMessagesCategory;
    }

    public List<Long> getFailureMessageAllowedRoles() {
        return failureMessageCategoryAllowedRanks;
    }

    public int getAutoDeleteFailureChannelDays() {
        return autoDeleteFailureDays;
    }
}
