package me.xorgon.volleyball.schedulers;

import me.xorgon.volleyball.VManager;
import me.xorgon.volleyball.objects.Court;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Elijah on 06/09/2016.
 */
public class MinPlayersChecker implements Runnable {

    private VManager manager;
    private Map<Player, Boolean> warnedPlayers; // Player and whether the warning has been sent.

    public MinPlayersChecker(VManager manager) {
        this.manager = manager;
        warnedPlayers = new HashMap<>();
    }

    @Override
    public void run() {
        String ffMessage = ChatColor.RED + "You will leave the game if you don't return to the court!";
        for (Court court : manager.getCourts().values()) {
            if (court.isStarted()) {
                for (Player player : court.getAllPlayers()) {
                    if (!court.isInCourt(player.getLocation())) {
                        if (!warnedPlayers.containsKey(player)) {
                            warnedPlayers.put(player, false);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(manager.getPlugin(), () -> {
                                if (!court.isInCourt(player.getLocation())) {
                                    player.sendMessage(ffMessage);
                                    warnedPlayers.remove(player);
                                    warnedPlayers.put(player, true);
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(manager.getPlugin(), new PlayerLeaveScheduler(court, player), 100);
                                }
                            }, 100);
                        }
                    } else {
                        if (warnedPlayers.containsKey(player)) {
                            if (warnedPlayers.get(player)) {
                                player.sendMessage(ChatColor.YELLOW + "Welcome back.");
                            }
                            warnedPlayers.remove(player);
                        }
                    }
                }
            }
        }
    }

    public void removeWarnedPlayer(Player player){
        if (warnedPlayers.containsKey(player)){
            warnedPlayers.remove(player);
        }
    }


    public class PlayerLeaveScheduler implements Runnable {

        Court court;
        Player player;

        public PlayerLeaveScheduler(Court court, Player player) {
            this.court = court;
            this.player = player;
        }

        @Override
        public void run() {
            if (!court.isInCourt(player.getLocation()) && warnedPlayers.containsKey(player)) {
                Court.Team team = court.getTeam(player);
                court.removePlayer(player);
                player.sendMessage(ChatColor.RED + "You have left the volleyball game.");
                if (!court.hasEnoughPlayers()){
                    int redSize = court.getRedPlayers().size();
                    int blueSize = court.getBluePlayers().size();
                    int minSize = court.getMinTeamSize();

                    Court.Team winner = Court.Team.NONE;

                    if (redSize < minSize && blueSize < minSize){
                        court.sendAllPlayersMessage(ChatColor.YELLOW + "Both teams forfeit.");
                        winner = Court.Team.NONE;
                    } else if (redSize < minSize){
                        court.sendAllPlayersMessage(ChatColor.RED + "Red " + ChatColor.YELLOW + "has too few players and so forfeits.");
                        winner = Court.Team.BLUE;
                    } else if (blueSize < minSize){
                        court.sendAllPlayersMessage(ChatColor.BLUE + "Blue " + ChatColor.YELLOW + "has too few players and so forfeits.");
                        winner = Court.Team.RED;
                    }

                    court.endGame(winner);
                }
            }
        }
    }
}
