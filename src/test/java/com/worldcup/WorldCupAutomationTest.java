package com.worldcup;

import com.worldcup.automation.WorldCupAutomation;
import com.worldcup.database.DatabaseManager;
import com.worldcup.database.TournamentQueries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WorldCupAutomationTest {
    
    private WorldCupAutomation automation;
    private DatabaseManager dbManager;
    private TournamentQueries queries;

    @BeforeEach
    void setUp() {
        automation = new WorldCupAutomation();
        dbManager = new DatabaseManager();
        queries = new TournamentQueries(dbManager);
    }

    @AfterEach
    void tearDown() {
        if (automation != null) {
            automation.close();
        }
        if (dbManager != null) {
            dbManager.close();
        }
    }

    @Test
    void testCompleteWorldCupAutomation() throws SQLException {
        // Run complete World Cup
        automation.runCompleteWorldCup();
        
        // Verify tournament was created
        Map<String, Object> summary = queries.getTournamentSummary();
        assertNotNull(summary);
        assertFalse(summary.isEmpty());
        assertEquals("COMPLETED", summary.get("status"));
        
        // Verify we have a champion
        assertNotNull(summary.get("championName"));
        assertNotNull(summary.get("runnerUpName"));
        
        // Verify we have 32 teams
        List<Map<String, Object>> teams = queries.getAllTeamsWithStats();
        assertEquals(32, teams.size());
        
        // Verify we have matches
        List<Map<String, Object>> matches = queries.getAllMatches();
        assertTrue(matches.size() > 0);
        
        // Verify group stage matches (48 matches: 8 groups * 6 matches per group)
        long groupMatches = matches.stream()
            .filter(m -> "GROUP".equals(m.get("matchType")))
            .count();
        assertEquals(48, groupMatches);
        
        // Verify knockout matches (15 matches: 8 + 4 + 2 + 1 for final)
        long knockoutMatches = matches.stream()
            .filter(m -> !"GROUP".equals(m.get("matchType")))
            .count();
        assertEquals(15, knockoutMatches);
        
        // Verify we have goals
        assertTrue((Integer) summary.get("totalGoals") > 0);
        
        // Verify we have a top scorer
        List<Map<String, Object>> topScorers = queries.getTopScorers(1);
        assertFalse(topScorers.isEmpty());
        assertTrue((Integer) topScorers.get(0).get("goals") > 0);
        
        System.out.println("✅ World Cup automation test completed successfully!");
        System.out.println("🏆 Champion: " + summary.get("championName"));
        System.out.println("📊 Total Goals: " + summary.get("totalGoals"));
        System.out.println("⚽ Total Matches: " + summary.get("totalMatches"));
    }

    @Test
    void testDatabaseTables() throws SQLException {
        // Test that all required tables exist
        assertTrue(dbManager.tableExists("teams"));
        assertTrue(dbManager.tableExists("players"));
        assertTrue(dbManager.tableExists("groups"));
        assertTrue(dbManager.tableExists("matches"));
        assertTrue(dbManager.tableExists("goals"));
        assertTrue(dbManager.tableExists("substitutions"));
        assertTrue(dbManager.tableExists("cards"));
        assertTrue(dbManager.tableExists("tournaments"));
        assertTrue(dbManager.tableExists("tournament_stats"));
        assertTrue(dbManager.tableExists("assistant_coaches"));
        
        System.out.println("✅ All database tables exist!");
    }

    @Test
    void testDataGeneration() {
        // Test that we can generate teams without errors
        assertDoesNotThrow(() -> {
            com.worldcup.generator.DataGenerator.generateRandomTeams(4);
        });
        
        // Test random data generation methods
        assertNotNull(com.worldcup.generator.DataGenerator.getRandomReferee());
        assertNotNull(com.worldcup.generator.DataGenerator.getRandomVenue());
        assertTrue(com.worldcup.generator.DataGenerator.generateRandomGoals() >= 0);
        assertTrue(com.worldcup.generator.DataGenerator.generateRandomMinute() > 0);
        
        int[] score = com.worldcup.generator.DataGenerator.generateMatchScore();
        assertTrue(score[0] >= 0);
        assertTrue(score[1] >= 0);
        
        System.out.println("✅ Data generation test completed successfully!");
    }
}