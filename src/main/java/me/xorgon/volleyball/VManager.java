package me.xorgon.volleyball;

import me.xorgon.volleyball.objects.Court;
import me.xorgon.volleyball.schedulers.BallChecker;
import me.xorgon.volleyball.schedulers.MinPlayersChecker;
import me.xorgon.volleyball.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;

import java.util.HashMap;
import java.util.Map;

public class VManager {

    private final VolleyballPlugin plugin = VolleyballPlugin.getInstance();

    private Map<String, Court> courts = new HashMap<>();
    private int ballCheckerID;
    private int minplayersCheckerID;
    private Config config;

    public VManager() {
        ballCheckerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new BallChecker(this), 20L, 4L);
        minplayersCheckerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new MinPlayersChecker(this), 20L, 20L);
        config = new Config(this);
        courts = config.load();
    }

    public Court addCourt(String name) {
        Court court = new Court(name);
        courts.put(name, court);
        return court;
    }

    public void removeCourt(String name) {
        if (courts.containsKey(name)) {
            courts.remove(name);
        }
    }

    public Court getCourt(String name) {
        if (courts.containsKey(name)) {
            return courts.get(name);
        } else {
            return null;
        }
    }

    public Court getCourt(Player player) {
        for (Court court : courts.values()) {
            if (court.isInCourt(player.getLocation())) {
                return court;
            }
        }
        return null;
    }

    public Court getCourt(Slime ball) {
        for (Court court : courts.values()) {
            if (court.getBall() == ball) {
                return court;
            }
        }
        return null;
    }

    public boolean isInCourt(Player player) {
        return getCourt(player) != null;
    }

    public boolean isPlaying(Player player) {
        for (Court court : courts.values()) {
            if (court.getAllPlayers().contains(player)) {
                return true;
            }
        }
        return false;
    }

    public Court getPlayingIn(Player player) {
        for (Court court : courts.values()) {
            if (court.getAllPlayers().contains(player)){
                return court;
            }
        }
        return null;
    }

    public Map<String, Court> getCourts() {
        return courts;
    }

    public void setCourts(Map<String, Court> courts) {
        this.courts = courts;
    }

    public boolean isVolleyball(Entity entity) {
        for (Court court : courts.values()) {
            if (court.isBall(entity)) {
                return true;
            }
        }
        return false;
    }

    public void clearVolleyballs() {
        courts.values().forEach(Court::removeBall);
    }

    public VolleyballPlugin getPlugin() {
        return plugin;
    }

    public Config getConfig() {
        return config;
    }
}
