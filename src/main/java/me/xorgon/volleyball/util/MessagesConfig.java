package me.xorgon.volleyball.util;

import me.xorgon.volleyball.VManager;
import me.xorgon.volleyball.VolleyballPlugin;
import me.xorgon.volleyball.objects.Court;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static me.xorgon.volleyball.util.ConfigUtil.deserializeVector;
import static me.xorgon.volleyball.util.ConfigUtil.serializeVector;

public class MessagesConfig {

    private VolleyballPlugin plugin = VolleyballPlugin.getInstance();
    private File file;
    private YamlConfiguration config;
    private VManager manager;

    public MessagesConfig(VManager manager) {
        this.manager = manager;
    }

    public VMessages load() {
        file = new File(plugin.getDataFolder(), "messages.yml");

        if (!file.exists()) {
            return new VMessages();
        }

        config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection messagesSec = config.getConfigurationSection("messages");
        if (messagesSec == null) {
            return new VMessages();
        }

        VMessages messages = new VMessages();
        return messages;
    }

    public void save() {
        config = new YamlConfiguration();

        ConfigurationSection messagesSec = config.createSection("messages");

        try {
            config.save(file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
