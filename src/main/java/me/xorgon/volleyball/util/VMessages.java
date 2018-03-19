package me.xorgon.volleyball.util;

import me.xorgon.volleyball.objects.Court;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class VMessages {
    public String help;
    public String fullGame;
    private String winMessage;
    public String gameLeaveBeforeStart;
    public String notEnoughPlayers;
    private String gameStart;
    private String scored;
    private String matchPoint;

    // MinPlayersChecker
    public String leaveGameThreat;
    public String returnToCourt;
    public String leftGame;
    private String teamForfeit;
    public String doubleForfeit;

    // VListener
    public String wrongSide;
    public String tooManyHits;
    public String clickForHelp;
    public String noPermissions;
    public String matchStarted;
    private String joinedTeam;
    private String fullTeam;
    private String matchStartingWithName;
    public String matchStartingWithoutName;
    public String clickToJoin;

    private Map<String, Integer> replacements = new HashMap<>();

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
        replacements.put("court.maxscore", Court.MAX_SCORE);
        replacements.put("court.maxhits", Court.MAX_HITS);
        replacements.put("court.startdelay", Court.START_DELAY_SECS);

        loadMessages();
    }

    private void loadMessages() {
        help = replacePlaceholders(helpDefault);
        fullGame = replacePlaceholders(fullGameDefault);
        tooManyHits = replacePlaceholders(tooManyHitsDefault);
        winMessage = winMessageDefault;
        gameLeaveBeforeStart = gameLeaveBeforeStartDefault;
        notEnoughPlayers = notEnoughPlayersDefault;
        gameStart = gameStartDefault;
        scored = scoredDefault;
        matchPoint = matchPointDefault;
        leaveGameThreat = leaveGameThreatDefault;
        returnToCourt = returnToCourtDefault;
        leftGame = leftGameDefault;
        teamForfeit = teamForfeitDefault;
        doubleForfeit = doubleForfeitDefault;
        wrongSide = wrongSideDefault;
        clickForHelp = clickForHelpDefault;
        noPermissions = noPermissionsDefault;
        matchStarted = matchStartedDefault;
        joinedTeam = joinedTeamDefault;
        fullTeam = fullTeamDefault;
        matchStartingWithName = replacePlaceholders(matchStartingWithNameDefault);
        matchStartingWithoutName = replacePlaceholders(matchStartingWithoutNameDefault);
        clickToJoin = clickToJoinDefault;
    }

    private String replacePlaceholders(String message) {
        for (String replacement : replacements.keySet()) {
            message = message.replaceAll("\\{" + replacement + "}", replacements.get(replacement).toString());
        }
        return message;
    }

    public String getWinMessage(Court.Team team) {
        String teamName = getTeamName(team);
        if (team == Court.Team.NONE) {
            return ChatColor.YELLOW + "It's a draw!";
        } else {
            return winMessage.replaceAll("\\{teamname}", teamName);
        }
    }

    public String getGameStartMessage(Court.Team team) {
        return gameStart.replaceAll("\\{teamname}", getTeamName(team));
    }

    public String getScoredMessage(Court.Team team) {
        return scored.replaceAll("\\{teamname}", getTeamName(team));
    }

    public String getMatchPointMessage(Court.Team team) {
        String teamName = getTeamName(team);
        if (teamName.equals("")) {
            teamName = "§eDouble";
        }
        return matchPoint.replaceAll("\\{teamname}", teamName);
    }

    public String getForfeitMessage(Court.Team team) {
        if (team == Court.Team.NONE) {
            return doubleForfeit;
        } else {
            return teamForfeit.replaceAll("\\{teamname}", getTeamName(team));
        }
    }

    public String getJoinedTeamMessage(Court.Team team) {
        return joinedTeam.replaceAll("\\{teamname}", getTeamName(team));
    }

    public String getFullTeamMessage(Court.Team team) {
        return fullTeam.replaceAll("\\{teamname}", getTeamName(team));
    }

    public String getMatchStartingWithNameMessage(Court court) {
        return matchStartingWithName.replaceAll("\\{court.name}", court.getDisplayName());
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
}
