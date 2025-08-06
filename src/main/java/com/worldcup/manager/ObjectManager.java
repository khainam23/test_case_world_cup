package com.worldcup.manager;

import com.worldcup.database.DatabaseManager;
import com.worldcup.model.*;
import com.worldcup.repository.*;
import com.worldcup.repository.impl.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    public static ObjectManager getInstance(DatabaseManager dbManager) {
        if (instance == null) {
            instance = new ObjectManager(dbManager);
        }
        return instance;
    }

    private void initializeModelRepositories() {
        Team.setTeamRepository(teamRepository);
        Goal.setGoalRepository(goalRepository);
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
     * tạo và lưu goal
     */
    public Goal createGoal(Player player, Team team, int minute, Match match) throws Exception {
        Goal goal = new Goal(player, team, minute, match);
        goal.save(); // Uses repository internally
        return goal;
    }

    /**
     * tạo và lưu card
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
     * tạo và lưu thay thế cầu thủ
     */
    public Substitution createSubstitution(Match match, Team team, Player playerIn, Player playerOut, int minute) throws Exception {
        Substitution substitution = new Substitution(playerIn, playerOut, minute, team, match);
        substitutionRepository.save(substitution);
        match.addSubstitution(substitution);
        return substitution;
    }

    public void saveTeam(Team team) throws Exception {
        teamRepository.save(team);
    }

    public void savePlayer(Player player, int teamId, boolean isStarting) throws SQLException {
        String sql = """
                    INSERT INTO players (name, jersey_number, position, team_id, is_starting, yellow_cards, red_cards, is_eligible)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, player.getName());
        pstmt.setInt(2, player.getJerseyNumber());
        pstmt.setString(3, player.getPosition());
        pstmt.setInt(4, teamId);
        pstmt.setBoolean(5, isStarting);
        pstmt.setInt(6, player.getYellowCards());
        pstmt.setInt(7, player.getRedCards());
        pstmt.setBoolean(8, player.isEligible());
        pstmt.executeUpdate();
        pstmt.close();

        // Set the player ID from database
        int playerId = dbManager.getLastInsertId();
        player.setId(playerId);
    }

    /**
     * cập nhật kết quả trận đấu
     */
    public void updateMatchResult(Match match, int teamAScore, int teamBScore) throws Exception {
        match.updateMatchResult(teamAScore, teamBScore);
        matchRepository.update(match);
    }
}