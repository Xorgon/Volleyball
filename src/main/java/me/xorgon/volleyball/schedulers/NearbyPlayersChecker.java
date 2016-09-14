package me.xorgon.volleyball.schedulers;

import me.xorgon.volleyball.objects.Court;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Elijah on 14/09/2016.
 */
public class NearbyPlayersChecker implements Runnable {

    private Court court;

    public NearbyPlayersChecker(Court court) {
        this.court = court;
    }

    @Override
    public void run() {
        if (court.isStarted()) {
            court.getNearbyPlayers().stream()
                    .filter(p -> !court.hasScoreboard(p))
                    .forEach(player -> court.setScoreboard(player));
        }
        List<Player> toRemove = court.getScoreboards().keySet().stream()
                .filter(p -> !court.getNearbyPlayers().contains(p))
                .filter(p -> !court.getAllPlayers().contains(p)).collect(Collectors.toList());

        toRemove.forEach(p -> court.revertScoreboard(p));

    }
}
