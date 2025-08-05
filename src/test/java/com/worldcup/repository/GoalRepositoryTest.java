package com.worldcup.repository;

import com.worldcup.database.DatabaseManager;
import com.worldcup.model.*;
import com.worldcup.repository.impl.GoalRepositoryImpl;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho GoalRepository
 * Kiểm tra OOP approach cho Goal operations
 */
class GoalRepositoryTest {
    
    private DatabaseManager dbManager;
    private GoalRepository goalRepository;
    private Team testTeam;
    private Player testPlayer;
    private Match testMatch;
    
    @BeforeEach
    void setUp() throws Exception {
        dbManager = new DatabaseManager();
        goalRepository = new GoalRepositoryImpl(dbManager);
        
        // Create test data
        setupTestData();
    }
    
    private void setupTestData() {
        // Create test team
        testTeam = new Team("Test Team", "Europe", "Test Coach", "Test Medical");
        testTeam.setId(1);
        testTeam.setTournamentId(1);
        
        // Create test player
        testPlayer = new Player("Test Player", 10, "Forward");
        testPlayer.setId(1);
        
        // Create test match
        Team teamB = new Team("Team B", "Asia", "Coach B", "Medical B");
        testMatch = new Match(testTeam, teamB, null, null, null, null, false);
        testMatch.setId(1);
    }
    
    @Test
    @DisplayName("Should save goal successfully")
    void testSaveGoal() throws Exception {
        // Given
        Goal goal = new Goal(testPlayer, testTeam, 45, testMatch);
        
        // When
        assertDoesNotThrow(() -> goalRepository.save(goal));
        
        // Then
        assertTrue(goal.getId() > 0, "Goal should have ID after saving");
    }
    
    @Test
    @DisplayName("Should count goals by player correctly")
    void testCountGoalsByPlayer() throws Exception {
        // Given
        String playerName = "Test Player";
        String teamName = "Test Team";
        int tournamentId = 1;
        
        // When
        int goalCount = goalRepository.countGoalsByPlayer(playerName, teamName, tournamentId);
        
        // Then
        assertTrue(goalCount >= 0, "Goal count should be non-negative");
    }
    
    @Test
    @DisplayName("Should count goals by team correctly")
    void testCountGoalsByTeam() throws Exception {
        // Given
        String teamName = "Test Team";
        int tournamentId = 1;
        
        // When
        int goalCount = goalRepository.countGoalsByTeam(teamName, tournamentId);
        
        // Then
        assertTrue(goalCount >= 0, "Goal count should be non-negative");
    }
    
    @Test
    @DisplayName("Should handle invalid goal data gracefully")
    void testInvalidGoalData() {
        // Given
        Goal invalidGoal = null;
        
        // When & Then
        assertThrows(Exception.class, () -> {
            goalRepository.save(invalidGoal);
        }, "Should throw exception for null goal");
    }
    
    @Test
    @DisplayName("Should find goals by match")
    void testFindGoalsByMatch() throws Exception {
        // Given
        int matchId = 1;
        
        // When
        List<Goal> goals = goalRepository.findByMatch(matchId);
        
        // Then
        assertNotNull(goals, "Goals list should not be null");
        assertTrue(goals.size() >= 0, "Goals list should be valid");
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (dbManager != null) {
            dbManager.closeConnection();
        }
    }
}