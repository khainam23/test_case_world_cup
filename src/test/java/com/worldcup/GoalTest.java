package com.worldcup;

import com.worldcup.model.Goal;
import com.worldcup.model.Match;
import com.worldcup.model.Player;
import com.worldcup.model.Team;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class GoalTest {

    public static void main(String[] args) {
        runAllTests();
        System.out.println("All GoalTest tests passed!");
    }

    public static void runAllTests() {
        // ========== GOAL CONSTRUCTOR TESTS ==========
        GoalConstructor_ValidParameters_CreateSuccessfully();
        GoalConstructor_NullPlayer_ThrowException();
        GoalConstructor_NullMatch_ThrowException();
        GoalConstructor_NegativeMinute_ThrowException();
        GoalConstructor_ZeroMinute_CreateSuccessfully();
        GoalConstructor_MinuteAtBoundary_CreateSuccessfully();
        GoalConstructor_LargeMinute_CreateSuccessfully();
        
        // ========== GETTER TESTS ==========
        Goal_GetPlayer_ReturnCorrectPlayer();
        Goal_GetMatch_ReturnCorrectMatch();
        Goal_GetMinute_ReturnCorrectMinute();
        
        // ========== BOUNDARY VALUE TESTS ==========
        GoalConstructor_MinuteMinValue_CreateSuccessfully();
        GoalConstructor_MinuteMaxValue_CreateSuccessfully();
        
        // ========== EQUIVALENCE PARTITIONING TESTS ==========
        GoalConstructor_ValidMinuteRange_CreateSuccessfully();
        GoalConstructor_InvalidNegativeMinute_ThrowException();
    }
    
    // Helper method to create players
    private static List<Player> generatePlayers(int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            players.add(new Player("Player" + i, i, "Position" + (i % 4 + 1)));
        }
        return players;
    }
    
    // Helper method to create teams and match
    private static Object[] setupTestData() {
        List<Player> playersA = generatePlayers(22);
        List<Player> playersB = generatePlayers(22);
        
        Team teamA = new Team("France", "Europe", "Coach A", Arrays.asList("A1", "A2"), "Doctor A", playersA, false);
        Team teamB = new Team("Brazil", "South America", "Coach B", Arrays.asList("B1", "B2"), "Doctor B", playersB, false);
        
        List<Player> startingPlayersA = playersA.subList(0, 11);
        List<Player> substitutePlayersA = playersA.subList(11, 22);
        List<Player> startingPlayersB = playersB.subList(0, 11);
        List<Player> substitutePlayersB = playersB.subList(11, 22);
        
        Match match = new Match(teamA, teamB, startingPlayersA, substitutePlayersA, startingPlayersB, substitutePlayersB);
        
        return new Object[]{teamA, teamB, match, playersA.get(0)};
    }
    
    public static void GoalConstructor_ValidParameters_CreateSuccessfully() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        Player player = (Player) data[3];
        
        Goal goal = new Goal(player, match, 45);
        assert goal.getPlayer() == player : "Goal should have correct player";
        assert goal.getMatch() == match : "Goal should have correct match";
        assert goal.getMinute() == 45 : "Goal should have correct minute";
    }
    
    public static void GoalConstructor_NullPlayer_ThrowException() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        
        try {
            new Goal(null, match, 45);
            assert false : "Goal constructor should throw exception for null player";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public static void GoalConstructor_NullMatch_ThrowException() {
        Object[] data = setupTestData();
        Player player = (Player) data[3];
        
        try {
            new Goal(player, null, 45);
            assert false : "Goal constructor should throw exception for null match";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public static void GoalConstructor_NegativeMinute_ThrowException() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        Player player = (Player) data[3];
        
        try {
            new Goal(player, match, -1);
            assert false : "Goal constructor should throw exception for negative minute";
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    public static void GoalConstructor_ZeroMinute_CreateSuccessfully() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        Player player = (Player) data[3];
        
        Goal goal = new Goal(player, match, 0);
        assert goal.getMinute() == 0 : "Goal should accept minute 0";
    }
    
    public static void GoalConstructor_MinuteAtBoundary_CreateSuccessfully() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        Player player = (Player) data[3];
        
        // Test boundary values
        Goal goal1 = new Goal(player, match, 1);
        assert goal1.getMinute() == 1 : "Goal should accept minute 1";
        
        Goal goal90 = new Goal(player, match, 90);
        assert goal90.getMinute() == 90 : "Goal should accept minute 90";
        
        Goal goal120 = new Goal(player, match, 120);
        assert goal120.getMinute() == 120 : "Goal should accept minute 120";
    }
    
    public static void GoalConstructor_LargeMinute_CreateSuccessfully() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        Player player = (Player) data[3];
        
        Goal goal = new Goal(player, match, 999);
        assert goal.getMinute() == 999 : "Goal should accept large minute values";
    }
    
    public static void Goal_GetPlayer_ReturnCorrectPlayer() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        Player player = (Player) data[3];
        
        Goal goal = new Goal(player, match, 45);
        assert goal.getPlayer() == player : "getPlayer should return the correct player";
        assert goal.getPlayer().getName().equals(player.getName()) : "Player name should match";
        assert goal.getPlayer().getJerseyNumber() == player.getJerseyNumber() : "Player jersey number should match";
    }
    
    public static void Goal_GetMatch_ReturnCorrectMatch() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        Player player = (Player) data[3];
        
        Goal goal = new Goal(player, match, 45);
        assert goal.getMatch() == match : "getMatch should return the correct match";
    }
    
    public static void Goal_GetMinute_ReturnCorrectMinute() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        Player player = (Player) data[3];
        
        Goal goal = new Goal(player, match, 67);
        assert goal.getMinute() == 67 : "getMinute should return the correct minute";
    }
    
    public static void GoalConstructor_MinuteMinValue_CreateSuccessfully() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        Player player = (Player) data[3];
        
        Goal goal = new Goal(player, match, 0);
        assert goal.getMinute() == 0 : "Goal should accept minimum minute value (0)";
    }
    
    public static void GoalConstructor_MinuteMaxValue_CreateSuccessfully() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        Player player = (Player) data[3];
        
        Goal goal = new Goal(player, match, Integer.MAX_VALUE);
        assert goal.getMinute() == Integer.MAX_VALUE : "Goal should accept maximum minute value";
    }
    
    public static void GoalConstructor_ValidMinuteRange_CreateSuccessfully() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        Player player = (Player) data[3];
        
        // Test various valid minute values
        int[] validMinutes = {1, 15, 30, 45, 60, 75, 90, 105, 120};
        for (int minute : validMinutes) {
            Goal goal = new Goal(player, match, minute);
            assert goal.getMinute() == minute : "Goal should accept minute " + minute;
        }
    }
    
    public static void GoalConstructor_InvalidNegativeMinute_ThrowException() {
        Object[] data = setupTestData();
        Match match = (Match) data[2];
        Player player = (Player) data[3];
        
        // Test various negative minute values
        int[] invalidMinutes = {-1, -10, -100, Integer.MIN_VALUE};
        for (int minute : invalidMinutes) {
            try {
                new Goal(player, match, minute);
                assert false : "Goal constructor should throw exception for negative minute " + minute;
            } catch (IllegalArgumentException e) {
                // Expected
            }
        }
    }
}