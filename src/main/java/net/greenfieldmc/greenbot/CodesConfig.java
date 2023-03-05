package net.greenfieldmc.greenbot;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class CodesConfig {

    private final List<String> codes;

    public CodesConfig(Plugin plugin, Config config) {
        if (!config.getCodesFile().exists()) throw new RuntimeException("Unable to find the codes file. [" + config.getCodesFile().getAbsolutePath() + "]");
        else {
            var codesFile = YamlConfiguration.loadConfiguration(config.getCodesFile());

            if (codesFile.get(config.getCodeListKey()) == null) {
                this.codes = new ArrayList<>();
                plugin.getLogger().warning("No codes were specified in the codes file with the key [" + config.getCodeListKey() + "]");
            } else {
                this.codes = codesFile.getStringList(config.getCodeListKey());
            }
        }
    }

    public List<String> getCodes() {
        return codes;
    }

}
