package com.worldcup.repository.impl;

import com.worldcup.database.DatabaseManager;
import com.worldcup.model.Goal;
import com.worldcup.repository.GoalRepository;
import com.worldcup.repository.PlayerRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation của GoalRepository
 * Xử lý tất cả database operations cho Goal entity
 */
public class GoalRepositoryImpl implements GoalRepository {
    
    private final DatabaseManager dbManager;
    private final PlayerRepository playerRepository;
    
    public GoalRepositoryImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.playerRepository = new PlayerRepositoryImpl(dbManager);
    }
    
    @Override
    public void save(Goal goal) throws SQLException {
        String sql = """
            INSERT INTO goals (match_id, player_id, team_id, minute, goal_type)
            VALUES (?, 
                    (SELECT p.id FROM players p JOIN teams t ON p.team_id = t.id 
                     WHERE p.name = ? AND t.name = ? AND t.tournament_id = ?), 
                    (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                    ?, 'REGULAR')
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, goal.getMatch().getId());
        pstmt.setString(2, goal.getPlayer().getName());
        pstmt.setString(3, goal.getTeam().getName());
        pstmt.setInt(4, goal.getTeam().getTournamentId());
        pstmt.setString(5, goal.getTeam().getName());
        pstmt.setInt(6, goal.getTeam().getTournamentId());
        pstmt.setInt(7, goal.getMinute());
        pstmt.executeUpdate();
        pstmt.close();
        
        int goalId = dbManager.getLastInsertId();
        goal.setId(goalId);
        
        // Cập nhật player goals thông qua repository
        playerRepository.updateGoals(goal.getPlayer(), goal.getTeam().getName(), goal.getTeam().getTournamentId());
    }
    
    @Override
    public void update(Goal goal) throws SQLException {
        String sql = """
            UPDATE goals 
            SET match_id = ?, player_id = ?, team_id = ?, minute = ?, goal_type = ?
            WHERE id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, goal.getMatch().getId());
        pstmt.setInt(2, goal.getPlayer().getId());
        pstmt.setInt(3, goal.getTeam().getId());
        pstmt.setInt(4, goal.getMinute());
        pstmt.setString(5, "REGULAR"); // Default goal type
        pstmt.setInt(6, goal.getId());
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    @Override
    public Optional<Goal> findById(int id) throws SQLException {
        String sql = """
            SELECT g.id, g.match_id, g.player_id, g.team_id, g.minute, g.goal_type,
                   p.name as player_name, t.name as team_name
            FROM goals g
            JOIN players p ON g.player_id = p.id
            JOIN teams t ON g.team_id = t.id
            WHERE g.id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        
        Optional<Goal> result = Optional.empty();
        if (rs.next()) {
            // Note: Để tạo Goal object hoàn chỉnh, cần load Match, Player, Team objects
            // Đây là simplified version
            result = Optional.empty(); // TODO: Implement full object creation
        }
        
        rs.close();
        pstmt.close();
        return result;
    }
    
    @Override
    public List<Goal> findByMatch(int matchId) throws SQLException {
        List<Goal> goals = new ArrayList<>();
        
        String sql = """
            SELECT g.id, g.match_id, g.player_id, g.team_id, g.minute, g.goal_type,
                   p.name as player_name, t.name as team_name
            FROM goals g
            JOIN players p ON g.player_id = p.id
            JOIN teams t ON g.team_id = t.id
            WHERE g.match_id = ?
            ORDER BY g.minute ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, matchId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Goal objects with full relationships
        }
        
        rs.close();
        pstmt.close();
        return goals;
    }
    
    @Override
    public List<Goal> findByPlayer(int playerId) throws SQLException {
        List<Goal> goals = new ArrayList<>();
        
        String sql = """
            SELECT g.id, g.match_id, g.player_id, g.team_id, g.minute, g.goal_type
            FROM goals g
            WHERE g.player_id = ?
            ORDER BY g.match_id ASC, g.minute ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, playerId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Goal objects
        }
        
        rs.close();
        pstmt.close();
        return goals;
    }
    
    @Override
    public List<Goal> findByTeamAndTournament(String teamName, int tournamentId) throws SQLException {
        List<Goal> goals = new ArrayList<>();
        
        String sql = """
            SELECT g.id, g.match_id, g.player_id, g.team_id, g.minute, g.goal_type
            FROM goals g
            JOIN teams t ON g.team_id = t.id
            WHERE t.name = ? AND t.tournament_id = ?
            ORDER BY g.match_id ASC, g.minute ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Goal objects
        }
        
        rs.close();
        pstmt.close();
        return goals;
    }
    
    @Override
    public List<Goal> findByPlayerAndTournament(String playerName, String teamName, int tournamentId) throws SQLException {
        List<Goal> goals = new ArrayList<>();
        
        String sql = """
            SELECT g.id, g.match_id, g.player_id, g.team_id, g.minute, g.goal_type
            FROM goals g
            JOIN players p ON g.player_id = p.id
            JOIN teams t ON g.team_id = t.id
            WHERE p.name = ? AND t.name = ? AND t.tournament_id = ?
            ORDER BY g.match_id ASC, g.minute ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, playerName);
        pstmt.setString(2, teamName);
        pstmt.setInt(3, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Goal objects
        }
        
        rs.close();
        pstmt.close();
        return goals;
    }
    
    @Override
    public int countGoalsByPlayer(String playerName, String teamName, int tournamentId) throws SQLException {
        String sql = """
            SELECT COUNT(*) as goal_count
            FROM goals g
            JOIN players p ON g.player_id = p.id
            JOIN teams t ON p.team_id = t.id
            WHERE p.name = ? AND t.name = ? AND t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, playerName);
        pstmt.setString(2, teamName);
        pstmt.setInt(3, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        int count = 0;
        if (rs.next()) {
            count = rs.getInt("goal_count");
        }
        
        rs.close();
        pstmt.close();
        return count;
    }
    
    @Override
    public int countGoalsByTeam(String teamName, int tournamentId) throws SQLException {
        String sql = """
            SELECT COUNT(*) as goal_count
            FROM goals g
            JOIN teams t ON g.team_id = t.id
            WHERE t.name = ? AND t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        int count = 0;
        if (rs.next()) {
            count = rs.getInt("goal_count");
        }
        
        rs.close();
        pstmt.close();
        return count;
    }
    
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM goals WHERE id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    @Override
    public boolean exists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM goals WHERE id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        
        boolean exists = false;
        if (rs.next()) {
            exists = rs.getInt(1) > 0;
        }
        
        rs.close();
        pstmt.close();
        return exists;
    }
}