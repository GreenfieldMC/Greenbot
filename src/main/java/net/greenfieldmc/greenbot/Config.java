package net.greenfieldmc.greenbot;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private final String botToken;
    private final long guildId;
    private final long testingChannelId;
    private final long testingRankId;

    private final long failureMessagesCategory;
    private final List<Long> failureMessageCategoryAllowedRanks;
    private final List<Long> ranksAllowedRunForPermission;
    private final int autoDeleteFailureDays;
    private final YamlConfiguration conf;
    private final File codesFile;
    private final String codeListKey;
    private final List<MapVersion> mapVersions;
    private final String mapVersionCommandFooter;
    private final List<ResourcepackVersion> resourcepackVersions;
    private final String resourcePackCommandFooter;

    private final List<String> betapackOneTimeDownloadSteps;
    private final List<String> betapackGitSetupSteps;
    private final List<String> betapackGitUpdateSteps;

    private final String macInstallLink;
    private final String windowsInstallLink;

    private final long archiveCategoryId;

    public Config(Plugin plugin) throws IOException {
        plugin.getDataFolder().mkdirs();
        File file = new File(plugin.getDataFolder() + File.separator + "config.yml");
        boolean hasBeenAddedToServer;
        long clientId = -1;
        if (file.exists()) {
            this.conf = YamlConfiguration.loadConfiguration(file);

            hasBeenAddedToServer = getOrThrow(Boolean.class, "hasBeenAddedToServer");
            conf.set("hasBeenAddedToServer", true);
            this.botToken = getOrThrow(String.class, "botToken");
            clientId = getOrThrow(Long.class, "clientId");
            this.guildId = getOrThrow(Long.class, "guildId");
            this.ranksAllowedRunForPermission = getOrThrow(List.class, "ranksAllowedRunForPermission");

            this.testingChannelId = getOrThrow(Long.class, "applications.testingChannelId");
            this.testingRankId = getOrThrow(Long.class, "applications.testingRankId");
            this.failureMessagesCategory = getOrThrow(Long.class, "applications.failureMessagesCategory");
            this.failureMessageCategoryAllowedRanks = getOrThrow(List.class, "applications.failureMessageCategoryAllowedRanks");
            this.autoDeleteFailureDays = getOrThrow(Integer.class,"applications.autoDeleteFailureDays");

            this.codesFile = new File(plugin.getDataFolder().getParentFile() + getOrThrow(String.class, "codes.codesYmlFile"));
            this.codeListKey = getOrThrow(String.class, "codes.codeListKey");


            if (conf.getConfigurationSection("mapVersion.versions") == null) throw new RuntimeException("Missing mapVersion.versions section in config.yml");
            var mapVerKeys = conf.getConfigurationSection("mapVersion.versions").getKeys(false);
            this.mapVersions = new ArrayList<>();
            mapVerKeys.forEach(key -> {
                var name = getOrThrow(String.class, "mapVersion.versions." + key + ".name");
                var mcVersion = getOrThrow(String.class, "mapVersion.versions." + key + ".mcVersion");
                mapVersions.add(new MapVersion(name, mcVersion));
            });
            this.mapVersionCommandFooter = getOrThrow(String.class, "mapVersion.footer");

            if (conf.getConfigurationSection("resourcePacks.versions") == null) throw new RuntimeException("Missing resourcePacks.versions section in config.yml");
            var rpVerKeys = conf.getConfigurationSection("resourcePacks.versions").getKeys(false);
            this.resourcepackVersions = new ArrayList<>();
            rpVerKeys.forEach(key -> {
                var mcVersion = getOrThrow(String.class, "resourcePacks.versions." + key + ".mcVersion");
                var download = getOrThrow(String.class, "resourcePacks.versions." + key + ".download");
                resourcepackVersions.add(new ResourcepackVersion(mcVersion, download));
            });
            this.resourcePackCommandFooter = getOrThrow(String.class, "resourcePacks.footer");

            this.betapackOneTimeDownloadSteps = getOrThrow(List.class, "betaPack.oneTimeSteps");
            this.betapackGitSetupSteps = getOrThrow(List.class, "betaPack.gitSetupSteps");
            this.betapackGitUpdateSteps = getOrThrow(List.class, "betaPack.gitUpdateSteps");

            this.windowsInstallLink = getOrThrow(String.class, "windowsInstall.link");
            this.macInstallLink = getOrThrow(String.class, "macInstall.link");

            this.archiveCategoryId = getOrThrow(Long.class, "archive.archiveCategoryId");

            conf.save(file);
        } else {
            plugin.getLogger().warning("No configuration file found for the bot. Creating a default configuration.");
            if (!file.createNewFile()) {
                throw new RuntimeException("Failed to create a new file. Exiting...");
            }
            var conf = YamlConfiguration.loadConfiguration(file);
            conf.set("hasBeenAddedToServer", true);
            conf.set("botToken", "put bot token here");
            conf.set("clientId", "put application client id here");
            conf.set("guildId", "put guild id here");
            conf.set("ranksAllowedRunForPermission", List.of(1L,2L));
            conf.set("applications.testingChannelId", "put testing channel id here");
            conf.set("applications.testingRankId", "put the testing rank ID here");
            conf.set("applications.failureMessagesCategory", "put the category ID here");
            conf.set("applications.failureMessageCategoryAllowedRanks", List.of(-1L, -2L));
            conf.set("applications.autoDeleteFailureDays", 14);
            conf.set("codes.codeYmlFile", "plugins/GreenfieldCore/codes.yml");
            conf.set("codes.codeListKey", "codes");
            conf.set("mapVersions.versions.v055.name", "v0.5.5");
            conf.set("mapVersions.versions.v055.mcVersion", "MC 1.xx");
            conf.set("mapVersions.footer", "this is a footer message");
            conf.set("resourcePacks.versions.v115.mcVersion", "1.15-1.16");
            conf.set("resourcePacks.versions.v115.download", "download link");
            conf.set("resourcePacks.footer", "this is a footer message");
            conf.set("betaPack.oneTimeSteps", List.of("First step", "second step"));
            conf.set("betaPack.gitSetupSteps", List.of("First step", "second step"));
            conf.set("betaPack.gitUpdateSteps", List.of("First step", "second step"));
            conf.set("macInstall.link", "video link");
            conf.set("windowsInstall.link", "video link");
            conf.set("archive.archiveCategoryId", "archive category ID here");
            conf.save(file);
            throw new RuntimeException("Set valid configuration values");
        }

        if (!hasBeenAddedToServer) plugin.getLogger().warning("The bot has not yet been added to your server, add it by following the link: https://discord.com/oauth2/authorize?client_id=" + clientId +  "&scope=bot&permissions=8");
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

    public List<Long> getRanksAllowedRunForPermission() {
        return ranksAllowedRunForPermission;
    }

    public List<MapVersion> getMapVersions() {
        return mapVersions;
    }

    public String getMapVersionCommandFooter() {
        return mapVersionCommandFooter;
    }

    public List<ResourcepackVersion> getResourcepackVersions() {
        return resourcepackVersions;
    }

    public String getResourcePackCommandFooter() {
        return resourcePackCommandFooter;
    }

    public List<String> getBetapackOneTimeDownloadSteps() {
        return betapackOneTimeDownloadSteps;
    }

    public List<String> getBetapackGitSetupSteps() {
        return betapackGitSetupSteps;
    }

    public List<String> getBetapackGitUpdateSteps() {
        return betapackGitUpdateSteps;
    }

    public String getMacInstallLink() {
        return macInstallLink;
    }

    public String getWindowsInstallLink() {
        return windowsInstallLink;
    }

    public long getArchiveCategoryId() {
        return archiveCategoryId;
    }

    public record MapVersion(String version, String minecraftVersion) {}

    public record ResourcepackVersion(String minecraftVersion, String link) {}

}
