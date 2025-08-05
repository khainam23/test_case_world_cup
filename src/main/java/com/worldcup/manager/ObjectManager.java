package com.worldcup.manager;

import com.worldcup.database.DatabaseManager;
import com.worldcup.model.*;
import com.worldcup.repository.*;
import com.worldcup.repository.impl.*;

import java.sql.PreparedStatement;

/**
 * Object Manager để quản lý tất cả repositories và cung cấp OOP interface
 * Tuân theo Singleton Pattern và Dependency Injection
 */
public class ObjectManager {
    
    private static ObjectManager instance;
    
    private final DatabaseManager dbManager;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final GoalRepository goalRepository;
    private final CardRepository cardRepository;
    private final SubstitutionRepository substitutionRepository;
    
    private ObjectManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        
        // Initialize repositories
        this.teamRepository = new TeamRepositoryImpl(dbManager);
        this.playerRepository = new PlayerRepositoryImpl(dbManager);
        this.matchRepository = new MatchRepositoryImpl(dbManager);
        this.goalRepository = new GoalRepositoryImpl(dbManager);
        this.cardRepository = new CardRepositoryImpl(dbManager);
        this.substitutionRepository = new SubstitutionRepositoryImpl(dbManager);
        
        // Set repositories to model classes for persistence operations
        initializeModelRepositories();
    }
    
    /**
     * Get singleton instance
     */
    public static ObjectManager getInstance(DatabaseManager dbManager) {
        if (instance == null) {
            instance = new ObjectManager(dbManager);
        }
        return instance;
    }
    
    /**
     * Initialize repositories in model classes
     */
    private void initializeModelRepositories() {
        Team.setTeamRepository(teamRepository);
        Goal.setGoalRepository(goalRepository);
        // Card.setCardRepository(cardRepository);
        // Substitution.setSubstitutionRepository(substitutionRepository);
        // Player.setPlayerRepository(playerRepository);
        // Match.setMatchRepository(matchRepository);
    }
    
    // Repository getters
    public TeamRepository getTeamRepository() {
        return teamRepository;
    }
    
    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }
    
    public MatchRepository getMatchRepository() {
        return matchRepository;
    }
    
    public GoalRepository getGoalRepository() {
        return goalRepository;
    }
    
    public CardRepository getCardRepository() {
        return cardRepository;
    }
    
    public SubstitutionRepository getSubstitutionRepository() {
        return substitutionRepository;
    }
    
    /**
     * OOP-style methods for common operations
     */
    
    /**
     * Create and save a new goal using OOP approach
     */
    public Goal createGoal(Player player, Team team, int minute, Match match) throws Exception {
        Goal goal = new Goal(player, team, minute, match);
        goal.save(); // Uses repository internally
        return goal;
    }
    
    /**
     * Create and save a new card using OOP approach
     */
    public Card createCard(Player player, Team team, Match match, int minute, Card.CardType type) throws Exception {
        Card card = new Card(player, team, match, minute, type);
        cardRepository.save(card);
        
        // Update player cards through repository
        String cardTypeStr = type == Card.CardType.YELLOW ? "YELLOW" : "RED";
        playerRepository.updateCards(player, team.getName(), team.getTournamentId(), cardTypeStr);
        
        return card;
    }
    
    /**
     * Create and save a new substitution using OOP approach
     */
    public Substitution createSubstitution(Match match, Team team, Player playerIn, Player playerOut, int minute) throws Exception {
        // Create substitution with correct parameter order: playerIn, playerOut, minute, team, match
        
        Substitution substitution = new Substitution(playerIn, playerOut, minute, team, match);
        substitutionRepository.save(substitution);
        return substitution;
    }
    
    /**
     * Save team with all related entities
     */
    public void saveTeamComplete(Team team) throws Exception {
        
        
        // Save team using repository - this will automatically save players and assistant coaches
        teamRepository.save(team);
       
    }
    
    /**
     * Load team with all players
     */
    public Team loadTeamWithPlayers(String teamName, int tournamentId) throws Exception {
        return teamRepository.findByNameAndTournament(teamName, tournamentId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamName));
    }
    
    /**
     * Update match result using OOP approach
     */
    public void updateMatchResult(Match match, int teamAScore, int teamBScore) throws Exception {
        match.updateMatchResult(teamAScore, teamBScore);
        matchRepository.update(match);
    }
    
    /**
     * Get database manager for direct access if needed
     */
    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }
}