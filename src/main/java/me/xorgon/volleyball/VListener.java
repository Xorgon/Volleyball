package me.xorgon.volleyball;

import com.supaham.commons.bukkit.text.FancyMessage;
import me.xorgon.volleyball.events.BallLandEvent;
import me.xorgon.volleyball.objects.Court;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class VListener implements Listener {

    VolleyballPlugin plugin = VolleyballPlugin.getInstance();
    VManager manager = plugin.getManager();

    @EventHandler
    public void onSlimeHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && manager.isVolleyball(event.getEntity())) {
            event.setCancelled(true);
            Slime ball = (Slime) event.getEntity();
            Player player = (Player) event.getDamager();
            Court court = manager.getCourt(ball);
            if (court.getTeam(player) != Court.Team.NONE) {
                if (court.canHit()) {
                    Court.Team ballSide = court.getSide(ball.getLocation());
                    if (ballSide != court.getTeam(player) && ballSide != Court.Team.NONE) {
                        player.sendMessage(ChatColor.YELLOW + "You can't hit the ball while it's on the opponents' side!");
                        return;
                    }
                    if (court.getLastHitBy() == court.getTeam(player)) {
                        if (court.getHitCount() >= Court.MAX_HITS) {
                            player.sendMessage(ChatColor.YELLOW + "Your team has already hit it " + court.getHitCount() + " times!");
                            return;
                        }
                    } else {
                        court.resetHitCount();
                    }
                    Vector dir = player.getLocation().getDirection();
                    if (!ball.hasGravity()) {
                        ball.setGravity(true);
                    }
                    double factor = 0.5;
                    if (player.isSprinting()) {
                        factor += 0.5;
                    }
                    if (!player.isOnGround()) {
                        factor += 0.5;
                    }
                    factor *= court.getPower();
                    ball.setVelocity(dir.clone().multiply(2.7).setY(dir.getY() + 1.1).multiply(factor));
                    court.setLastHitBy(court.getTeam(player));
                    court.incHitCount();
                }
            }
        }
    }

    @EventHandler
    public void onSlimeDamage(EntityDamageEvent event) {
        if (manager.isVolleyball(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBallLand(BallLandEvent event) {
        event.getCourt().score(event.getScoringTeam());
    }


    private List<Player> teamFullSent = new ArrayList<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (manager.isInCourt(player)) {
            Court court = manager.getCourt(player);
            if (court.isStarted()) {
                return;
            }
            FancyMessage helpMsg = new FancyMessage();
            String helpTooltip = ChatColor.YELLOW + "Click here to learn how to play volleyball!";
            helpMsg.color(ChatColor.LIGHT_PURPLE)
                    .text("Click here")
                    .command("/vb help")
                    .tooltip(helpTooltip)
                    .then()
                    .color(ChatColor.YELLOW)
                    .text(" to learn how to play volleyball!");
            if (court.getSide(player.getLocation()) == Court.Team.RED) {
                if (court.getRedPlayers().size() < court.getMaxTeamSize()) {
                    if (!court.getRedPlayers().contains(player)) {
                        if (court.getBluePlayers().contains(player)) {
                            court.removePlayer(player);
                        }
                        court.addPlayer(player, Court.Team.RED);
                        player.sendMessage(ChatColor.YELLOW + "You have joined " + ChatColor.RED + "red " + ChatColor.YELLOW + "team!");
                        helpMsg.send(player);
                    }
                } else {
                    if (!teamFullSent.contains(player)) {
                        player.sendMessage(ChatColor.RED + "Red " + ChatColor.YELLOW + "team is full.");
                        teamFullSent.add(player);
                    }
                }
            } else {
                if (court.getBluePlayers().size() < court.getMaxTeamSize()) {
                    if (!court.getBluePlayers().contains(player)) {
                        if (court.getRedPlayers().contains(player)) {
                            court.removePlayer(player);
                        }
                        court.addPlayer(player, Court.Team.BLUE);
                        player.sendMessage(ChatColor.YELLOW + "You have joined " + ChatColor.BLUE + "blue " + ChatColor.YELLOW + "team!");
                        helpMsg.send(player);
                    }
                } else {
                    if (!teamFullSent.contains(player)) {
                        player.sendMessage(ChatColor.BLUE + "Blue " + ChatColor.YELLOW + "team is full.");
                        teamFullSent.add(player);
                    }
                }
            }
            if (court.hasEnoughPlayers() && !court.isStarting()) {
                if (court.getDisplayName() != null) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "Volleyball game starting at the " + court.getDisplayName() +
                            " court in " + Court.START_DELAY_SECS + " seconds!");
                } else {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "Volleyball game starting in " + Court.START_DELAY_SECS + " seconds!");
                }
                FancyMessage joinMsg = new FancyMessage()
                        .color(ChatColor.LIGHT_PURPLE)
                        .text("Click here to join!")
                        .command("/vb join " + court.getName())
                        .tooltip(ChatColor.YELLOW + "Join the volleyball game.");
                Bukkit.getOnlinePlayers().forEach(p -> joinMsg.send(p));
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> court.startGame(false), Court.START_DELAY_SECS * 20);
                court.setStarting(true);
            }
        } else if (manager.isPlaying(player)) {
            Court court = manager.getPlayingIn(player);
            if (!court.isStarted()) {
                player.sendMessage(ChatColor.YELLOW + "You have left the game.");
                court.removePlayer(player);
            }
        } else if (teamFullSent.contains(player)) {
            teamFullSent.remove(player);
        }
    }

    @EventHandler
    public void onPlayerDamagedByBall(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Slime) {
            if (manager.isVolleyball(event.getDamager())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        // Cancel the event if player is playing.
        event.setCancelled(manager.isPlaying(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (manager.isPlaying(event.getPlayer())) {
            manager.getPlayingIn(event.getPlayer()).removePlayer(event.getPlayer());
        }
    }

}
