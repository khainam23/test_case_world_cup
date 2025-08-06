package com.worldcup.model;
import java.util.List;

public class PlayOffMatch extends Match{
    private boolean isFirstLeg;
    private boolean isSecondLeg;

    public PlayOffMatch(Team teamA, Team teamB, String venue, String referee, boolean isKnockout) {
        super(teamA, teamB, venue, referee, isKnockout);
    }
}
