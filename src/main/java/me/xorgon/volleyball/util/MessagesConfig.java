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

        VMessages messages = new VMessages();
        ConfigurationSection messagesSec = config.getConfigurationSection("messages");

        if (messagesSec == null) {
            for (String key : messages.getMessages().keySet()) {
                messages.setMessage(key, "");
            }
        } else {
            for (String key : messagesSec.getKeys(false)) {
                if (messages.hasMessageKey(key)) {
                    messages.setMessage(key, messagesSec.getString(key));
                }
            }
            for (String key : messages.getMessages().keySet()) {
                if (!messagesSec.contains(key)) {
                    messages.setMessage(key, "");
                }
            }
        }

        return messages;
    }

    public void save() {
        if (!file.exists()) { // Don't overwrite config file.
            config = new YamlConfiguration();

            ConfigurationSection messagesSec = config.createSection("messages");

            Map<String, String> messages = (new VMessages()).getMessages();
            for (String key : messages.keySet()) {
                if (!messages.get(key).isEmpty()) {
                    messagesSec.set(key, messages.get(key));
                }
            }

            try {
                config.save(file);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
