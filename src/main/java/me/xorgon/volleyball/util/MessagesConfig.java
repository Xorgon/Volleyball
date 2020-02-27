package me.xorgon.volleyball.util;

import me.xorgon.volleyball.VManager;
import me.xorgon.volleyball.VolleyballPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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
            this.save();
            return new VMessages();
        }

        config = YamlConfiguration.loadConfiguration(file);

        VMessages messages = new VMessages();
        ConfigurationSection messagesSec = config.getConfigurationSection("messages");

        if (messagesSec == null) {
            messages.setNeedsRepair(true);
        } else {
            for (String key : messagesSec.getKeys(false)) {
                if (messages.hasMessageKey(key)) {  // Ensure no additional messages are loaded.
                    messages.setMessage(key, messagesSec.getString(key));
                } else {
                    messages.setNeedsRepair(true);
                }
            }
            for (String key : messages.getMessages().keySet()) {
                if (!messagesSec.contains(key)) {
                    messages.setNeedsRepair(true);
                }
            }
        }
        return messages;
    }

    public void save() {
        if (!file.exists()) { // Don't overwrite config file.
            config = new YamlConfiguration();

            ConfigurationSection messagesSec = config.createSection("messages");

            VMessages vMessages = new VMessages();
            vMessages.createMapWithDefaults();
            Map<String, String> messages = vMessages.getMessages();
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
        } else if (manager.messages.needsRepair()) {  // Or something was wrong with the config.
            config = new YamlConfiguration();

            ConfigurationSection messagesSec = config.createSection("messages");

            VMessages vMessages = manager.messages;
            Map<String, String> messages = vMessages.getMessages();
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

            vMessages.setNeedsRepair(false);
        }
    }
}
