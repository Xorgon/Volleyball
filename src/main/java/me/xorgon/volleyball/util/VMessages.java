package me.xorgon.volleyball.util;

import me.xorgon.volleyball.objects.Court;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class VMessages {
    private Map<String, String> messages;

    private Map<String, Integer> globalPlaceholders = new HashMap<>();

    private String helpDefault = "§dHow to play volleyball:\n"
            + "§eSprinting and jumping both increase the power of your shot.\n"
            + "§eUse one, or both, to hit the ball as far as you want.\n"
            + "§eIt is recommended to just sprint for a serve.\n\n"
            + "§eTeams take turns serving.\n"
            + "§eEach time the ball goes over the net, a team has "
            + "§d" + "{court.maxhits}" + "§e shots to hit it back over.\n"
            + "§eThe first team to score "
            + "§d" + "{court.maxscore}" + "§e points wins!\n";

    private String fullGameDefault = "§eThat game is full, but you can watch!";

    private String winMessageDefault = "{teamname} team wins! Congratulations.";

    private String drawMessageDefault = "§eIt's a draw!";

    private String gameLeaveBeforeStartDefault = "§eYou left the court before the game started.";

    private String notEnoughPlayersDefault = "§eNot enough players to start.";

    private String gameStartDefault = "§eGame started, you're on {teamname} §eteam.";

    private String scoredDefault = "{teamname} §eteam scored!";

    private String matchPointDefault = "{teamname} §ematch point!";

    private String leaveGameThreatDefault = "§cYou will leave the game if you don't return to the court!";

    private String returnToCourtDefault = "§eWelcome back.";

    private String leftGameDefault = "§cYou have left the volleyball game.";

    private String teamForfeitDefault = "{teamname} §ehas too few players and so forfeits.";

    private String doubleForfeitDefault = "§eBoth teams forfeit.";

    private String wrongSideDefault = "§eYou can't hit the ball while it's on the opponents' side!";

    private String tooManyHitsDefault = "§eYour team has already hit it {court.maxhits} times!";

    private String clickForHelpDefault = "§dClick here to learn how to play volleyball!";

    private String noPermissionsDefault = "§cYou do not have permission to play volleyball.";

    private String matchStartedDefault = "§cThe match has already started!";

    private String joinedTeamDefault = "§eYou have joined {teamname} §eteam!";

    private String fullTeamDefault = "{teamname} §cteam is full.";

    private String matchStartingWithNameDefault = "§eVolleyball game starting at the {court.name} court in {court.startdelay} seconds!";

    private String matchStartingWithoutNameDefault = "§eVolleyball game starting in {court.startdelay} seconds!";

    private String clickToJoinDefault = "§dClick here to join the game!";

    public VMessages() {
        globalPlaceholders.put("court.maxscore", Court.MAX_SCORE);
        globalPlaceholders.put("court.maxhits", Court.MAX_HITS);
        globalPlaceholders.put("court.startdelay", Court.START_DELAY_SECS);

        createMapWithDefaults();
        replaceAllPlaceholders();
    }

    public void createMapWithDefaults() {
        messages = new HashMap<>();
        messages.put("help", helpDefault);
        messages.put("full-game", fullGameDefault);
        messages.put("win-message", winMessageDefault);
        messages.put("draw-message", drawMessageDefault);
        messages.put("game-leave-before-start", gameLeaveBeforeStartDefault);
        messages.put("not-enough-players", notEnoughPlayersDefault);
        messages.put("game-start", gameStartDefault);
        messages.put("scored", scoredDefault);
        messages.put("match-point", matchPointDefault);

        // MinPlayersChecker
        messages.put("leave-game-threat", leaveGameThreatDefault);
        messages.put("return-to-court", returnToCourtDefault);
        messages.put("left-game", leftGameDefault);
        messages.put("team-forfeit", teamForfeitDefault);
        messages.put("double-forfeit", doubleForfeitDefault);

        // VListener
        messages.put("wrong-side", wrongSideDefault);
        messages.put("too-many-hits", tooManyHitsDefault);
        messages.put("click-for-help", clickForHelpDefault);
        messages.put("no-permissions", noPermissionsDefault);
        messages.put("match-started", matchStartedDefault);
        messages.put("joined-team", joinedTeamDefault);
        messages.put("full-team", fullTeamDefault);
        messages.put("match-starting-with-name", matchStartingWithNameDefault);
        messages.put("match-starting-without-name", matchStartingWithoutNameDefault);
        messages.put("click-to-join", clickToJoinDefault);
    }

    public boolean hasMessageKey(String key) {
        return messages.containsKey(key);
    }

    public void setMessage(String key, String message) {
        messages.replace(key, replacePlaceholders(message));
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    private void replaceAllPlaceholders() {
        for (String messageKey : messages.keySet()) {
            messages.replace(messageKey, replacePlaceholders(messages.get(messageKey)));
        }
    }

    private String replacePlaceholders(String message) {
        for (String replacement : globalPlaceholders.keySet()) {
            message = message.replaceAll("\\{" + replacement + "}", globalPlaceholders.get(replacement).toString());
        }
        return message;
    }

    private String getTeamName(Court.Team team) {
        String teamName = "";
        if (team == Court.Team.RED) {
            teamName = ChatColor.RED + "Red";
        } else if (team == Court.Team.BLUE) {
            teamName = ChatColor.BLUE + "Blue";
        }
        return teamName;
    }


    // Message getters.

    public String getHelpMessage() {
        return messages.get("help");
    }

    public String getFullGameMessage() {
        return messages.get("full-game");
    }

    public String getWinMessage(Court.Team team) {
        String teamName = getTeamName(team);
        if (team == Court.Team.NONE) {
            return messages.get("draw-message");
        } else {
            return messages.get("win-message").replaceAll("\\{teamname}", teamName);
        }
    }

    public String getGameLeaveBeforeStartMessage() {
        return messages.get("game-leave-before-start");
    }

    public String getNotEnoughPlayersMessage() {
        return messages.get("not-enough-players");
    }

    public String getGameStartMessage(Court.Team team) {
        return messages.get("game-start").replaceAll("\\{teamname}", getTeamName(team));
    }

    public String getScoredMessage(Court.Team team) {
        return messages.get("scored").replaceAll("\\{teamname}", getTeamName(team));
    }

    public String getMatchPointMessage(Court.Team team) {
        String teamName = getTeamName(team);
        if (teamName.equals("")) {
            teamName = "§eDouble";
        }
        return messages.get("match-point").replaceAll("\\{teamname}", teamName);
    }

    public String getLeaveGameThreatMessage() {
        return messages.get("leave-game-threat");
    }

    public String getReturnToCourtMessage() {
        return messages.get("return-to-court");
    }

    public String getLeftGameMessage() {
        return messages.get("left-game");
    }

    public String getForfeitMessage(Court.Team team) {
        if (team == Court.Team.NONE) {
            return messages.get("double-forfeit");
        } else {
            return messages.get("team-forfeit").replaceAll("\\{teamname}", getTeamName(team));
        }
    }

    public String getWrongSideMessage() {
        return messages.get("wrong-side");
    }

    public String getTooManyHitsMessage() {
        return messages.get("too-many-hits");
    }

    public String getClickForHelpMessage() {
        return messages.get("click-for-help");
    }

    public String getNoPermissionsMessage() {
        return messages.get("no-permissions");
    }

    public String getMatchStartedMessage() {
        return messages.get("match-started");
    }

    public String getJoinedTeamMessage(Court.Team team) {
        return messages.get("joined-team").replaceAll("\\{teamname}", getTeamName(team));
    }

    public String getFullTeamMessage(Court.Team team) {
        return messages.get("full-team").replaceAll("\\{teamname}", getTeamName(team));
    }

    public String getMatchStartingWithNameMessage(Court court) {
        return messages.get("match-starting-with-name").replaceAll("\\{court.name}", court.getDisplayName());
    }

    public String getMatchStartingWithoutNameMessage() {
        return messages.get("match-starting-without-name");
    }

    public String getClickToJoinMessage() {
        return messages.get("click-to-join");
    }
}
