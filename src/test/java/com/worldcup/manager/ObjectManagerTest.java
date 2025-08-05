package com.worldcup.manager;

import com.worldcup.database.DatabaseManager;
import com.worldcup.model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho ObjectManager
 * Kiểm tra OOP approach và Repository pattern
 */
class ObjectManagerTest {
    
    private DatabaseManager dbManager;
    private ObjectManager objectManager;
    private Team testTeamA;
    private Team testTeamB;
    private Player testPlayer;
    private Match testMatch;
    
    @BeforeEach
    void setUp() throws Exception {
        dbManager = new DatabaseManager();
        objectManager = ObjectManager.getInstance(dbManager);
        
        setupTestData();
    }
    
    private void setupTestData() {
        // Create test teams
        testTeamA = new Team("Team A", "Europe", "Coach A", "Medical A");
        testTeamA.setId(1);
        testTeamA.setTournamentId(1);
        
        testTeamB = new Team("Team B", "Asia", "Coach B", "Medical B");
        testTeamB.setId(2);
        testTeamB.setTournamentId(1);
        
        // Create test player
        testPlayer = new Player("Test Player", 10, "Forward");
        testPlayer.setId(1);
        
        // Add player to team
        testTeamA.getStartingPlayers().add(testPlayer);
        
        // Create test match
        testMatch = new Match(testTeamA, testTeamB, null, null, null, null, false);
        testMatch.setId(1);
    }
    
    @Test
    @DisplayName("Should create goal using OOP approach")
    void testCreateGoal() throws Exception {
        // Given
        int minute = 45;
        
        // When
        Goal goal = objectManager.createGoal(testPlayer, testTeamA, minute, testMatch);
        
        // Then
        assertNotNull(goal, "Goal should be created");
        assertEquals(testPlayer, goal.getPlayer(), "Goal should have correct player");
        assertEquals(testTeamA, goal.getTeam(), "Goal should have correct team");
        assertEquals(minute, goal.getMinute(), "Goal should have correct minute");
        assertEquals(testMatch, goal.getMatch(), "Goal should have correct match");
        assertTrue(goal.getId() > 0, "Goal should have ID after creation");
    }
    
    @Test
    @DisplayName("Should create card using OOP approach")
    void testCreateCard() throws Exception {
        // Given
        int minute = 30;
        Card.CardType cardType = Card.CardType.YELLOW;
        
        // When
        Card card = objectManager.createCard(testPlayer, testTeamA, testMatch, minute, cardType);
        
        // Then
        assertNotNull(card, "Card should be created");
        assertEquals(testPlayer, card.getPlayer(), "Card should have correct player");
        assertEquals(testTeamA, card.getTeam(), "Card should have correct team");
        assertEquals(testMatch, card.getMatch(), "Card should have correct match");
        assertEquals(minute, card.getMinutes(), "Card should have correct minute");
        assertEquals(cardType, card.getType(), "Card should have correct type");
    }
    
    @Test
    @DisplayName("Should create substitution using OOP approach")
    void testCreateSubstitution() throws Exception {
        // Given
        Player playerIn = new Player("Player In", 11, "Midfielder");
        Player playerOut = new Player("Player Out", 12, "Defender");
        int minute = 60;
        
        // Add players to team
        testTeamA.getStartingPlayers().add(playerOut);
        testTeamA.getSubstitutePlayers().add(playerIn);
        
        // When
        Substitution substitution = objectManager.createSubstitution(testMatch, testTeamA, playerIn, playerOut, minute);
        
        // Then
        assertNotNull(substitution, "Substitution should be created");
        assertEquals(testMatch, substitution.getMatch(), "Substitution should have correct match");
        assertEquals(testTeamA, substitution.getTeam(), "Substitution should have correct team");
        assertEquals(playerIn, substitution.getInPlayer(), "Substitution should have correct player in");
        assertEquals(playerOut, substitution.getOutPlayer(), "Substitution should have correct player out");
        assertEquals(minute, substitution.getMinute(), "Substitution should have correct minute");
    }
    
    @Test
    @DisplayName("Should handle invalid goal creation")
    void testInvalidGoalCreation() {
        // Given
        Player nullPlayer = null;
        int minute = 45;
        
        // When & Then
        assertThrows(Exception.class, () -> {
            objectManager.createGoal(nullPlayer, testTeamA, minute, testMatch);
        }, "Should throw exception for null player");
    }
    
    @Test
    @DisplayName("Should get repositories correctly")
    void testGetRepositories() {
        // When & Then
        assertNotNull(objectManager.getTeamRepository(), "Team repository should not be null");
        assertNotNull(objectManager.getPlayerRepository(), "Player repository should not be null");
        assertNotNull(objectManager.getMatchRepository(), "Match repository should not be null");
        assertNotNull(objectManager.getGoalRepository(), "Goal repository should not be null");
        assertNotNull(objectManager.getCardRepository(), "Card repository should not be null");
        assertNotNull(objectManager.getSubstitutionRepository(), "Substitution repository should not be null");
    }
    
    @Test
    @DisplayName("Should maintain singleton pattern")
    void testSingletonPattern() {
        // When
        ObjectManager instance1 = ObjectManager.getInstance(dbManager);
        ObjectManager instance2 = ObjectManager.getInstance(dbManager);
        
        // Then
        assertSame(instance1, instance2, "ObjectManager should follow singleton pattern");
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (dbManager != null) {
            dbManager.closeConnection();
        }
    }
}