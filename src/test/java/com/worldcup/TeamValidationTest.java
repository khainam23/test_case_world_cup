package com.worldcup;

import com.worldcup.generator.DataGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TeamValidationTest {

    @Test
    void testTeamHasMaximum22Players() {
        // Generate a few teams to test
        List<Team> teams = DataGenerator.generateRandomTeams(4);
        
        for (Team team : teams) {
            // Each team should have maximum 22 players total
            assertTrue(team.getPlayers().size() <= 22, 
                "Team " + team.getName() + " should have maximum 22 players, but has " + team.getPlayers().size());
            
            // 11 starting players
            assertEquals(11, team.getStartingPlayers().size(), 
                "Team " + team.getName() + " should have exactly 11 starting players");
            
            // Up to 11 substitute players
            assertTrue(team.getSubstitutePlayers().size() <= 11, 
                "Team " + team.getName() + " should have maximum 11 substitute players");
            
            // All players should have unique jersey numbers
            Set<Integer> jerseyNumbers = new HashSet<>();
            for (Player player : team.getPlayers()) {
                int jerseyNumber = player.getJerseyNumber();
                assertFalse(jerseyNumbers.contains(jerseyNumber), 
                    "Duplicate jersey number " + jerseyNumber + " in team " + team.getName());
                jerseyNumbers.add(jerseyNumber);
                
                // Jersey numbers should be between 1 and 22
                assertTrue(jerseyNumber >= 1 && jerseyNumber <= 22, 
                    "Jersey number " + jerseyNumber + " should be between 1 and 22");
            }
            
            // Should have unique jersey numbers equal to total players
            assertEquals(team.getPlayers().size(), jerseyNumbers.size(), 
                "Team " + team.getName() + " should have unique jersey numbers for all players");
        }
        
        System.out.println("✅ All teams have maximum 22 players with unique jersey numbers 1-22");
    }

    @Test
    void testPlayerMethods() {
        List<Team> teams = DataGenerator.generateRandomTeams(1);
        Team team = teams.get(0);
        Player player = team.getPlayers().get(0);
        
        // Test that getJerseyNumber() and getNumber() return the same value
        assertEquals(player.getNumber(), player.getJerseyNumber(), 
            "getNumber() and getJerseyNumber() should return the same value");
        
        // Test getRedCards() method
        assertEquals(0, player.getRedCards(), "New player should have 0 red cards");
        
        player.receiveRedCard();
        assertEquals(1, player.getRedCards(), "Player with red card should have 1 red card");
        
        // Test other methods
        assertNotNull(player.getName(), "Player should have a name");
        assertNotNull(player.getPosition(), "Player should have a position");
        assertEquals(0, player.getGoals(), "New player should have 0 goals");
        assertEquals(0, player.getYellowCards(), "New player should have 0 yellow cards");
        
        System.out.println("✅ All Player methods work correctly");
    }

    @Test
    void testTeamValidation() {
        // Create a list with too many players (23 players)
        List<Player> tooManyPlayers = DataGenerator.generateRandomTeams(1).get(0).getPlayers();
        // Add one more player to exceed the limit
        tooManyPlayers.add(new Player("Extra Player", 99, "ST"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Team("Test Team", "Europe", "Test Coach", 
                    List.of("Assistant 1"), "Dr. Test", tooManyPlayers, false);
        }, "Should throw exception for team with more than 22 players");
        
        // Test assistant coaches validation
        assertThrows(IllegalArgumentException.class, () -> {
            List<Player> players = DataGenerator.generateRandomTeams(1).get(0).getPlayers();
            new Team("Test Team", "Europe", "Test Coach", 
                    List.of("Assistant 1", "Assistant 2", "Assistant 3", "Assistant 4"), 
                    "Dr. Test", players, false);
        }, "Should throw exception for team with more than 3 assistant coaches");
        
        System.out.println("✅ Team validation works correctly");
    }

    @Test
    void testStartingAndSubstitutePlayersValidation() {
        // Test constructor with separate starting and substitute players
        List<Team> teams = DataGenerator.generateRandomTeams(1);
        Team originalTeam = teams.get(0);
        
        List<Player> starting = originalTeam.getStartingPlayers();
        List<Player> substitutes = originalTeam.getSubstitutePlayers();
        
        // This should work fine
        Team newTeam = new Team("New Team", "Europe", "Coach", 
                               List.of("Assistant"), "Dr. Test", 
                               starting, substitutes, false);
        
        assertEquals(11, newTeam.getStartingPlayers().size());
        assertEquals(11, newTeam.getSubstitutePlayers().size());
        assertEquals(22, newTeam.getPlayers().size());
        
        // Test with wrong number of starting players
        assertThrows(IllegalArgumentException.class, () -> {
            new Team("Test Team", "Europe", "Coach", 
                    List.of("Assistant"), "Dr. Test", 
                    starting.subList(0, 10), substitutes, false);
        }, "Should throw exception for team with wrong number of starting players");
        
        // Test with too many substitute players (12 instead of max 11)
        List<Player> tooManySubstitutes = new ArrayList<>(substitutes);
        tooManySubstitutes.add(new Player("Extra Sub", 99, "ST"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Team("Test Team", "Europe", "Coach", 
                    List.of("Assistant"), "Dr. Test", 
                    starting, tooManySubstitutes, false);
        }, "Should throw exception for team with too many substitute players");
        
        System.out.println("✅ Starting and substitute players validation works correctly");
    }
}