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

public class CourtsConfig {

    private VolleyballPlugin plugin = VolleyballPlugin.getInstance();
    private File file;
    private YamlConfiguration config;
    private VManager manager;

    public CourtsConfig(VManager manager) {
        this.manager = manager;
    }

    public Map<String, Court> load() {
        Map<String, Court> courts = new HashMap<>();

        file = new File(plugin.getDataFolder(), "courts.yml");

        if (!file.exists()) {
            return new HashMap<>();
        }

        config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection courtsSec = config.getConfigurationSection("courts");
        if (courtsSec == null) {
            return new HashMap<>();
        }

        for (String courtName : courtsSec.getKeys(false)) {
            ConfigurationSection courtSec = courtsSec.getConfigurationSection(courtName);
            Court court = manager.addCourt(courtName);
            court.setRed(deserializeVector(courtSec.get("redMin")), deserializeVector(courtSec.get("redMax")));
            court.setBlue(deserializeVector(courtSec.get("blueMin")), deserializeVector(courtSec.get("blueMax")));
            court.setWorldName(courtSec.getString("world"));

            if (courtSec.contains("ballSize")) {
                court.setBallSize(courtSec.getInt("ballSize"));
            }

            if (courtSec.contains("power")) {
                court.setPowerFactor(courtSec.getDouble("power"));
            }

            if (courtSec.contains("minTeamSize")) {
                court.setMinTeamSize(courtSec.getInt("minTeamSize"));
            }
            if (courtSec.contains("maxTeamSize")) {
                court.setMaxTeamSize(courtSec.getInt("maxTeamSize"));
            }
            if (courtSec.contains("inviteRange")) {
                court.setInviteRange(courtSec.getInt("inviteRange"));
            }

            if (courtSec.contains("displayName")) {
                court.setDisplayName(courtSec.getString("displayName"));
            }

            courts.put(courtName, court);
            System.out.println("Loaded " + courtName);
        }

        return courts;
    }

    public void save() {
        config = new YamlConfiguration();

        ConfigurationSection courtsSec = config.createSection("courts");

        for (String courtName : manager.getCourts().keySet()) {
            Court court = manager.getCourt(courtName);
            ConfigurationSection courtSec = courtsSec.createSection(courtName);
            courtSec.set("redMin", serializeVector(court.getRedMin()));
            courtSec.set("redMax", serializeVector(court.getRedMax()));
            courtSec.set("blueMin", serializeVector(court.getBlueMin()));
            courtSec.set("blueMax", serializeVector(court.getBlueMax()));
            courtSec.set("world", court.getWorldName());

            courtSec.set("ballSize", court.getBallSize());
            courtSec.set("power", court.getPowerFactor());

            courtSec.set("minTeamSize", court.getMinTeamSize());
            courtSec.set("maxTeamSize", court.getMaxTeamSize());
            courtSec.set("inviteRange", court.getInviteRange());

            if (court.getDisplayName() != null) {
                courtSec.set("displayName", court.getDisplayName());
            }

            System.out.println("Saved " + courtName);
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
