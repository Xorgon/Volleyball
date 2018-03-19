package me.xorgon.volleyball.objects;

import de.slikey.effectlib.EffectManager;
import me.xorgon.volleyball.VManager;
import me.xorgon.volleyball.VolleyballPlugin;
import me.xorgon.volleyball.effects.BallLandEffect;
import me.xorgon.volleyball.effects.BallTrailEffect;
import me.xorgon.volleyball.effects.RomanCandleEffect;
import me.xorgon.volleyball.schedulers.NearbyPlayersChecker;
import me.xorgon.volleyball.util.TitleUtil;
import net.minecraft.server.v1_12_R1.EntitySlime;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftSlime;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.*;

public class Court {

    private VManager manager;

    private String name;
    private String displayName;

    private Vector blueMin;
    private Vector blueMax;

    private Vector redMin;
    private Vector redMax;

    private double y;

    private World world;

    private Slime ball;
    private int ballSize = 3;
    private Team lastHitBy;
    private int hitCount;
    public static int MAX_HITS = 3;
    private long lastHitMS;
    public static int HIT_PERIOD_MS = 250;

    private List<Player> redPlayers = new ArrayList<>();
    private List<Player> bluePlayers = new ArrayList<>();
    private Map<Player, Scoreboard> scoreboards = new HashMap<>();

    private int minTeamSize = 1;
    private int maxTeamSize = 6;

    private int redScore = 0;
    private int blueScore = 0;
    public static int MAX_SCORE = 21;

    private Team turn;
    private boolean started;

    public static int START_DELAY_SECS = 15;
    private boolean starting;

    private boolean initialized;

    private BallTrailEffect trailEffect;

    private Scoreboard scoreboard;

    private NearbyPlayersChecker nearbyChecker;

    public Court(String name, VManager manager) {
        this.name = name;
        started = false;
        initialized = false;

        this.manager = manager;

        scoreboard = VolleyballPlugin.getInstance().getServer().getScoreboardManager().getNewScoreboard();
        scoreboard.registerNewTeam("red").setPrefix(ChatColor.RED + "");
        scoreboard.registerNewTeam("blue").setPrefix(ChatColor.BLUE + "");
        Objective obj = scoreboard.registerNewObjective("vbscore", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Score");
        obj.getScore(ChatColor.RED + "Red").setScore(0);
        obj.getScore(ChatColor.BLUE + "Blue").setScore(0);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(VolleyballPlugin.getInstance(), new NearbyPlayersChecker(this),
                20, 10);
    }

    public boolean isInCourt(Location location) {
        if (!initialized) {
            return false;
        }
        if (location.getWorld() == world && location.getY() >= y) {
            location.setY(y);
            if (location.toVector().isInAABB(redMin, redMax) || location.toVector().isInAABB(blueMin, blueMax)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public Team getSide(Location location) {
        if (location.toVector().setY(y).isInAABB(redMin, redMax)) {
            return Team.RED;
        } else if (location.toVector().setY(y).isInAABB(blueMin, blueMax)) {
            return Team.BLUE;
        } else {
            return Team.NONE;
        }
    }

    public Team getTeam(Player player) {
        if (redPlayers.contains(player)) {
            return Team.RED;
        } else if (bluePlayers.contains(player)) {
            return Team.BLUE;
        } else {
            return Team.NONE;
        }
    }

    public double getY() {
        return y;
    }

    public void setRed(Vector point1, Vector point2) {
        redMin = Vector.getMinimum(point1, point2);
        redMax = Vector.getMaximum(point1, point2);
        y = redMax.getY();
        isInitialized();
    }

    public void setRed(Location point1, Location point2) {
        redMin = Vector.getMinimum(point1.toVector(), point2.toVector());
        redMax = Vector.getMaximum(point1.toVector(), point2.toVector());
        y = redMax.getY();
        setWorld(point1.getWorld());
        isInitialized();
    }

    public void setBlue(Vector point1, Vector point2) {
        blueMin = Vector.getMinimum(point1, point2);
        blueMax = Vector.getMaximum(point1, point2);
        isInitialized();
    }

    public void setBlue(Location point1, Location point2) {
        blueMin = Vector.getMinimum(point1.toVector(), point2.toVector());
        blueMax = Vector.getMaximum(point1.toVector(), point2.toVector());
        setWorld(point1.getWorld());
        isInitialized();
    }

    public void setWorld(World world) {
        this.world = world;
        isInitialized();
    }

    public Slime getBall() {
        return ball;
    }

    public double getPower() {
        // Method devised by James 'Octobox' Griffin.
        Vector unitV = getCenter(Team.RED).toVector().subtract(getCenter(Team.BLUE).toVector()).normalize();
        double L1 = Math.abs(unitV.dot(redMax.clone().subtract(blueMin)));
        double L2 = Math.abs(unitV.dot(blueMax.clone().subtract(redMin)));
        double length = Math.max(L1, L2);
        return Math.pow(length, 0.75) / 16.792; // Values adjusted for best experience.
    }

    public void spawnBall(Location loc) {
        if (this.ball != null) {
            removeBall();
        }
        Slime ball = (Slime) loc.getWorld().spawnEntity(loc.setDirection(new Vector(0, 1, 0)), EntityType.SLIME);
        fixStupidMinecraftNoAI(ball);
        ball.setSize(ballSize);
        ball.setGravity(false);
        this.ball = ball;
        ball.setGlowing(true);
        setBallColour(turn);
        trailEffect = new BallTrailEffect(VolleyballPlugin.getInstance().getEffectManager(), this);
        trailEffect.start();
    }

    public void fixStupidMinecraftNoAI(Slime ball) {
        EntitySlime handle = ((CraftSlime) ball).getHandle();
        try {
            Field b = handle.goalSelector.getClass().getDeclaredField("b");
            b.setAccessible(true);
            ((Set) b.get(handle.goalSelector)).clear();
            ((Set) b.get(handle.targetSelector)).clear();
            handle.yaw = 0;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setBallColour(Team team) {
        if (team == Team.RED) {
            scoreboard.getTeam("red").addEntry(ball.getUniqueId().toString());
        } else if (team == Team.BLUE) {
            scoreboard.getTeam("blue").addEntry(ball.getUniqueId().toString());
        } else {
        }
    }

    public void removeBall() {
        if (ball != null) {
            this.ball.remove();
            this.ball = null;
            trailEffect.cancel();
        }
    }

    public boolean isBall(Entity entity) {
        if (entity instanceof Slime) {
            return entity == ball;
        } else {
            return false;
        }
    }

    public List<Player> getRedPlayers() {
        return redPlayers;
    }

    public List<Player> getInRedBox() {
        List<Player> players = new ArrayList<>();
        world.getPlayers().forEach(p -> {
            if (getSide(p.getLocation()) == Team.RED) {
                players.add(p);
            }
        });
        return players;
    }

    public List<Player> getBluePlayers() {
        return bluePlayers;
    }

    public List<Player> getInBlueBox() {
        List<Player> players = new ArrayList<>();
        world.getPlayers().forEach(p -> {
            if (getSide(p.getLocation()) == Team.BLUE) {
                players.add(p);
            }
        });
        return players;
    }

    public void addPoint(Team team) {
        if (team == Team.RED) {
            setRedScore(redScore + 1);
        } else if (team == Team.BLUE) {
            setBlueScore(blueScore + 1);
        }
    }

    public Team getLastHitBy() {
        return lastHitBy;
    }

    public void setLastHitBy(Team lastHitBy) {
        setBallColour(lastHitBy);
        this.lastHitBy = lastHitBy;
    }

    public int getRedScore() {
        return redScore;
    }

    public void setRedScore(int redScore) {
        this.redScore = redScore;
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.RED + "Red").setScore(redScore);
    }

    public int getBlueScore() {
        return blueScore;
    }

    public void setBlueScore(int blueScore) {
        this.blueScore = blueScore;
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.BLUE + "Blue").setScore(blueScore);
    }

    public void serve() {
        spawnBall(getCenter(turn));
        setLastHitBy(turn);
        if (turn == Team.RED) {
            turn = Team.BLUE;
        } else {
            turn = Team.RED;
        }
    }

    public Location getCenter(Team team) {
        if (team == Team.RED) {
            return redMin.getMidpoint(redMax).add(new Vector(0, 2.25, 0)).toLocation(world);
        } else if (team == Team.BLUE) {
            return blueMin.getMidpoint(blueMax).add(new Vector(0, 2.25, 0)).toLocation(world);
        } else {
            return null;
        }
    }

    public Location getCenter() {
        Location redCenter = redMin.getMidpoint(redMax).add(new Vector(0, 2.25, 0)).toLocation(world);
        Location blueCenter = blueMin.getMidpoint(blueMax).add(new Vector(0, 2.25, 0)).toLocation(world);
        return redCenter.toVector().getMidpoint(blueCenter.toVector()).toLocation(world);
    }

    public boolean isFinished() {
        return (redScore >= MAX_SCORE || blueScore >= MAX_SCORE);
    }

    public Team getWinning() {
        if (redScore > blueScore) {
            return Team.RED;
        } else if (blueScore > redScore) {
            return Team.BLUE;
        } else {
            return Team.NONE;
        }
    }

    public void endGame(boolean serverShutdown) {
        removeBall();
        String winMessage = manager.messages.getWinMessage(getWinning());
        if (!winMessage.isEmpty()) {
            sendNearbyPlayersMessage(winMessage);
        }

        revertScoreboards();

        if (!serverShutdown) {
            fireworks(getWinning());
        }

        redPlayers = new ArrayList<>();
        bluePlayers = new ArrayList<>();
        setRedScore(0);
        setBlueScore(0);
        if (started) {
            started = false;
        }
    }

    public void endGame() {
        endGame(false);
    }

    // Only use when messages and fireworks are otherwise handled (e.g. forfeits).
    public void endGame(Team winner) {
        removeBall();
        fireworks(winner);
        redPlayers = new ArrayList<>();
        bluePlayers = new ArrayList<>();
        setRedScore(0);
        setBlueScore(0);
        if (started) {
            started = false;
        }
    }

    public void ballLanded() {
        if (isFinished()) {
            endGame();
        } else {
            serve();
        }
    }

    public void sendRedPlayersMessage(String message) {
        redPlayers.forEach(player -> player.sendMessage(message));
    }

    public void sendBluePlayersMessage(String message) {
        bluePlayers.forEach(player -> player.sendMessage(message));
    }

    public void sendAllPlayersMessage(String message) {
        List<Player> players = getAllPlayers();
        players.forEach(p -> p.sendMessage(message));
    }

    public void sendNearbyPlayersMessage(String message) {
        getNearbyPlayers().forEach(p -> p.sendMessage(message));
    }

    public List<Player> getNearbyPlayers() {
        Vector redC = getCenter(Team.RED).toVector();
        Vector blueC = getCenter(Team.BLUE).toVector();
        double length = 2 * redC.distance(blueC);
        Location loc = redC.midpoint(blueC).toLocation(world);
        Collection<Entity> nearby = world.getNearbyEntities(loc, length, length / 2, length);
        List<Player> players = new ArrayList<>();
        nearby.stream().filter(e -> e instanceof Player).forEach(p -> players.add((Player) p));
        return players;
    }

    public void startGame(boolean force) {
        if (!force && started) {
            return;
        }

        getAllPlayers().stream().filter(p -> !isInCourt(p.getLocation())).forEach(p -> {
            removePlayer(p);
            String gameLeaveBeforeStartMessage = manager.messages.getGameLeaveBeforeStartMessage();
            if (!gameLeaveBeforeStartMessage.isEmpty()) {
                p.sendMessage(gameLeaveBeforeStartMessage);
            }
        });

        if (!hasEnoughPlayers() && !force) {
            String notEnoughPlayersMessage = manager.messages.getNotEnoughPlayersMessage();
            if (!notEnoughPlayersMessage.isEmpty()) {
                sendAllPlayersMessage(notEnoughPlayersMessage);
            }
            starting = false;
            return;
        }

        String gameStartMessageRed = manager.messages.getGameStartMessage(Team.RED);
        if (!gameStartMessageRed.isEmpty()) {
            sendRedPlayersMessage(gameStartMessageRed);
        }
        String gameStartMessageBlue = manager.messages.getGameStartMessage(Team.BLUE);
        if (!gameStartMessageBlue.isEmpty()) {
            sendBluePlayersMessage(gameStartMessageBlue);
        }

        setScoreboards(getNearbyPlayers());

        turn = Team.RED;
        setRedScore(0);
        setBlueScore(0);
        started = true;
        starting = false;

        serve();
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isInitialized() {
        boolean b = redMin != null && redMax != null && blueMin != null && blueMax != null && world != null;
        initialized = b;
        return b;
    }

    public Vector getBlueMin() {
        return blueMin;
    }

    public Vector getBlueMax() {
        return blueMax;
    }

    public Vector getRedMin() {
        return redMin;
    }

    public Vector getRedMax() {
        return redMax;
    }

    public World getWorld() {
        return world;
    }

    public int getMinTeamSize() {
        return minTeamSize;
    }

    public void setMinTeamSize(int minTeamSize) {
        this.minTeamSize = minTeamSize;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(int maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isStarting() {
        return starting;
    }

    public void setStarting(boolean starting) {
        this.starting = starting;
    }

    public void incHitCount() {
        hitCount++;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void resetHitCount() {
        hitCount = 0;
    }

    public boolean canHit() {
        long now = new Date().getTime();
        if (now - lastHitMS < HIT_PERIOD_MS) {
            return false;
        } else {
            lastHitMS = now;
            return true;
        }
    }

    public void score(Team scoringTeam) {

        addPoint(scoringTeam);

        hitCount = 0;

        String message = manager.messages.getScoredMessage(scoringTeam);

        if (!message.isEmpty()) {
            getAllPlayers().forEach(p -> TitleUtil.sendTitle(p, "", message));
        }

        boolean redMP = getRedScore() == Court.MAX_SCORE - 1 && getBlueScore() < MAX_SCORE;
        boolean blueMP = getBlueScore() == Court.MAX_SCORE - 1 && getRedScore() < MAX_SCORE;
        if (!manager.messages.getMatchPointMessage(Team.NONE).isEmpty()) { // If this is empty, all will be empty.
            if (redMP && blueMP) {
                sendNearbyPlayersMessage(manager.messages.getMatchPointMessage(Team.NONE));
            } else if (redMP) {
                sendNearbyPlayersMessage(manager.messages.getMatchPointMessage(Team.RED));
            } else if (blueMP) {
                sendNearbyPlayersMessage(manager.messages.getMatchPointMessage(Team.BLUE));
            }
        }

        BallLandEffect effect = new BallLandEffect(VolleyballPlugin.getInstance().getEffectManager(), this, scoringTeam);
        effect.callback = this::ballLanded;
        effect.start();

        removeBall();
    }

    public boolean hasEnoughPlayers() {
        int redSize = getInRedBox().size();
        int blueSize = getInBlueBox().size();
        return (redSize >= minTeamSize && redSize <= maxTeamSize && blueSize >= minTeamSize && blueSize <= maxTeamSize);
    }

    public int getBallSize() {
        return ballSize;
    }

    public void setBallSize(int ballSize) {
        this.ballSize = ballSize;
    }

    public String getName() {
        return name;
    }

    public void fireworks(Team team) {
        Vector c1;
        Vector c2;
        Vector c3;
        Vector c4;
        Color color;
        if (team == Team.RED) {
            c1 = redMax;
            c2 = new Vector(redMax.getX(), y, redMin.getZ());
            c3 = redMin;
            c4 = new Vector(redMin.getX(), y, redMax.getZ());
            color = Color.RED;
        } else if (team == Team.BLUE) {
            c1 = blueMax;
            c2 = new Vector(blueMax.getX(), y, blueMin.getZ());
            c3 = blueMin;
            c4 = new Vector(blueMin.getX(), y, blueMax.getZ());
            color = Color.BLUE;
        } else {
            if (redMax.distance(blueMin) > blueMax.distance(redMin)) {
                c1 = redMax;
                c2 = new Vector(redMax.getX(), y, blueMin.getZ());
                c3 = blueMin;
                c4 = new Vector(blueMin.getX(), y, redMax.getZ());
            } else {
                c1 = blueMax;
                c2 = new Vector(blueMax.getX(), y, redMin.getZ());
                c3 = redMin;
                c4 = new Vector(redMin.getX(), y, blueMax.getZ());
            }
            color = Color.PURPLE;
        }

        EffectManager eM = VolleyballPlugin.getInstance().getEffectManager();

        int fPerSide = 5;
        double height = 8.0;
        Location loc;
        for (int i = 0; i < fPerSide; i++) {
            double dist = ((double) i) / (fPerSide - 1); // Distance along side
            loc = c1.clone().add(c2.clone().subtract(c1).multiply(dist)).toLocation(world);
            new RomanCandleEffect(eM, loc, color, height).start();
            loc = c2.clone().add(c3.clone().subtract(c2).multiply(dist)).toLocation(world);
            new RomanCandleEffect(eM, loc, color, height).start();
            loc = c3.clone().add(c4.clone().subtract(c3).multiply(dist)).toLocation(world);
            new RomanCandleEffect(eM, loc, color, height).start();
            loc = c4.clone().add(c1.clone().subtract(c4).multiply(dist)).toLocation(world);
            new RomanCandleEffect(eM, loc, color, height).start();
        }
    }

    public void removePlayer(Player player) {
        revertScoreboard(player);
        if (redPlayers.contains(player)) {
            redPlayers.remove(player);
        } else if (bluePlayers.contains(player)) {
            bluePlayers.remove(player);
        }
    }

    public void addPlayer(Player player, Team team) {
        if (!scoreboards.containsKey(player)) {
            scoreboards.put(player, player.getScoreboard());
        }
        player.setScoreboard(scoreboard);
        if (team == Team.RED) {
            redPlayers.add(player);
        } else if (team == Team.BLUE) {
            bluePlayers.add(player);
        }
    }

    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        players.addAll(redPlayers);
        players.addAll(bluePlayers);
        return players;
    }

    /**
     * Sets the scoreboard for given players to be the court's scoreboard and saves their previous scoreboard.
     *
     * @param players players for whom to set the scoreboard.
     */
    public void setScoreboards(List<Player> players) {
        players.forEach(p -> {
            if (!scoreboards.containsKey(p))
                scoreboards.put(p, p.getScoreboard());
            p.setScoreboard(scoreboard);
        });
    }

    public void setScoreboard(Player player) {
        if (!scoreboards.containsKey(player)) {
            scoreboards.put(player, player.getScoreboard());
            player.setScoreboard(scoreboard);
        }
    }

    /**
     * Reverts all the players' scoreboards to those stored in 'scoreboards' and clears 'scoreboards'.
     */
    public void revertScoreboards() {
        List<Player> reverted = new ArrayList<>();
        scoreboards.forEach((p, sb) -> {
            p.setScoreboard(sb);
            reverted.add(p);
        });
        reverted.forEach(p -> scoreboards.remove(p));
    }

    public void revertScoreboard(Player player) {
        if (scoreboards.containsKey(player)) {
            player.setScoreboard(scoreboards.get(player));
            scoreboards.remove(player);
        }
    }

    public boolean hasScoreboard(Player player) {
        return scoreboards.containsKey(player);
    }

    public Map<Player, Scoreboard> getScoreboards() {
        return scoreboards;
    }

    public enum Team {
        RED, BLUE, NONE
    }

    public void resetCourt() {
        if (isStarted()) {
            endGame(true);
        } else {
            removeBall();
        }
    }
}