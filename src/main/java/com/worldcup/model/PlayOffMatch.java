package com.worldcup.model;
import java.util.List;

public class PlayOffMatch extends Match{
    private boolean isFirstLeg;
    private boolean isSecondLeg;

    public PlayOffMatch(Team teamA, Team teamB, List<Player> startingPlayersTeamA, List<Player> substitutePlayersTeamA, List<Player> startingPlayersTeamB, List<Player> substitutePlayersTeamB, boolean isKnockout) {
        super(teamA, teamB, isKnockout);
    }
}
