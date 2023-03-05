package net.greenfieldmc.greenbot;

import org.bukkit.Bukkit;
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
    private final List<Long> allowCodesOtherRanks;
    private final int autoDeleteFailureDays;
    private final YamlConfiguration conf;
    private final File codesFile;
    private final String codeListKey;

    public Config(Plugin plugin) throws IOException {
        plugin.getDataFolder().mkdirs();
        File file = new File(plugin.getDataFolder() + File.separator + "config.yml");
        if (file.exists()) {
            this.conf = YamlConfiguration.loadConfiguration(file);

            this.hasBeenAddedToServer = getOrThrow(Boolean.class, "hasBeenAddedToServer");
            conf.set("hasBeenAddedToServer", true);
            this.botToken = getOrThrow(String.class, "botToken");

            this.guildId = getOrThrow(Long.class, "guildId");
            this.testingChannelId = getOrThrow(Long.class, "testingChannelId");
            this.testingRankId = getOrThrow(Long.class, "testingRankId");
            this.failureMessagesCategory = getOrThrow(Long.class, "failureMessagesCategory");
            this.failureMessageCategoryAllowedRanks = getOrThrow(List.class, "failureMessageCategoryAllowedRanks");
            this.autoDeleteFailureDays = getOrThrow(Integer.class,"autoDeleteFailureDays");

            this.codesFile = new File(plugin.getDataFolder().getParentFile() + getOrThrow(String.class, "codes.codesYmlFile"));
            this.codeListKey = getOrThrow(String.class, "codes.codeListKey");
            this.allowCodesOtherRanks = getOrThrow(List.class, "codes.allowCodesOtherRanks");

            conf.save(file);
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
            conf.set("codes.codeYmlFile", "plugins/GreenfieldCore/codes.yml");
            conf.set("codes.codeListKey", "codes");
            conf.set("codes.allowCodesOtherRanks", List.of(1L,2L));
            conf.save(file);
            throw new RuntimeException("Set valid configuration values");
        }

        if (!hasBeenAddedToServer) plugin.getLogger().warning("The bot has not yet been added to your server, add it by following the link: https://discord.com/oauth2/authorize?client_id=1081436720618283058&scope=bot&permissions=8");
    }

    private <T> T getOrThrow(Class<T> type, String key) {
        if (conf.get(key) == null) throw new RuntimeException("Configuration file does not have all necessary keys to load. Please delete the configuration and let the program regenerate it.");
        else return type.cast(conf.get(key));
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

    public File getCodesFile() {
        return codesFile;
    }

    public String getCodeListKey() {
        return codeListKey;
    }

    public List<Long> getAllowCodesOtherRanks() {
        return allowCodesOtherRanks;
    }
}
