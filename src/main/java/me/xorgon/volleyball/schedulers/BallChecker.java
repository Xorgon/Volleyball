package me.xorgon.volleyball.schedulers;

import me.xorgon.volleyball.VManager;
import me.xorgon.volleyball.events.BallLandEvent;
import me.xorgon.volleyball.objects.Court;
import org.bukkit.Bukkit;

public class BallChecker implements Runnable {

    private VManager manager;

    public BallChecker(VManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        for (Court court : manager.getCourts().values()) {
            if (court.getBall() != null) {
                if (court.getBall().isOnGround()
                        || court.getBall().getLocation().getY() < court.getY()
                        || court.hasLanded()
                        || court.getBall().getLocation().getBlock().isLiquid()
                        || court.getBall().getLocation().distance(court.getCenter()) > 10 * court.getCenter(Court.Team.RED).distance(court.getCenter(Court.Team.BLUE))) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(manager.getPlugin(), new BallRemovalScheduler(court), 2L);
                }
            }
        }
    }

    public class BallRemovalScheduler implements Runnable {

        private Court court;

        public BallRemovalScheduler(Court court) {
            this.court = court;
        }

        @Override
        public void run() {
            if (court.getBall().isOnGround()
                    || court.getBall().getLocation().getY() < court.getY()
                    || court.hasLanded()
                    || court.getBall().getLocation().getBlock().isLiquid()
                    || court.getBall().getLocation().distance(court.getCenter()) > 10 * court.getCenter(Court.Team.RED).distance(court.getCenter(Court.Team.BLUE))) {
                Bukkit.getPluginManager().callEvent(new BallLandEvent(court));
            }
        }
    }
}

