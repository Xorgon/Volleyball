package me.xorgon.volleyball.events;

import me.xorgon.volleyball.objects.Court;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BallLandEvent extends Event {

    private Court court;
    private Court.Team scoringTeam;

    private static final HandlerList handlerList = new HandlerList();

    public BallLandEvent(Court court) {
        this.court = court;
        Location ballLoc = court.getBall().getLocation();
        if (court.isInCourt(ballLoc)) {
            scoringTeam = court.getSide(ballLoc) == Court.Team.RED ? Court.Team.BLUE : Court.Team.RED;
        } else {
            scoringTeam = court.getLastHitBy() == Court.Team.RED ? Court.Team.BLUE : Court.Team.RED;
        }
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public Court getCourt() {
        return court;
    }

    public Court.Team getScoringTeam() {
        return scoringTeam;
    }
}
