package com.worldcup.repository.impl;

import com.worldcup.database.DatabaseManager;
import com.worldcup.model.Substitution;
import com.worldcup.repository.SubstitutionRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation của SubstitutionRepository
 * Xử lý tất cả database operations cho Substitution entity
 */
public class SubstitutionRepositoryImpl implements SubstitutionRepository {
    
    private final DatabaseManager dbManager;
    
    public SubstitutionRepositoryImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    @Override
    public void save(Substitution substitution) throws SQLException {
        // Validate players exist in the team first
        validatePlayersExist(substitution);
        
        String sql = """
            INSERT INTO substitutions (match_id, team_id, player_in_id, player_out_id, minute)
            VALUES (?, 
                    (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                    (SELECT p.id FROM players p JOIN teams t ON p.team_id = t.id 
                     WHERE p.name = ? AND t.name = ? AND t.tournament_id = ?),
                    (SELECT p.id FROM players p JOIN teams t ON p.team_id = t.id 
                     WHERE p.name = ? AND t.name = ? AND t.tournament_id = ?), 
                    ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, substitution.getMatch().getId());
        pstmt.setString(2, substitution.getTeam().getName());
        pstmt.setInt(3, substitution.getTeam().getTournamentId());
        pstmt.setString(4, substitution.getInPlayer().getName());
        pstmt.setString(5, substitution.getTeam().getName());
        pstmt.setInt(6, substitution.getTeam().getTournamentId());
        pstmt.setString(7, substitution.getOutPlayer().getName());
        pstmt.setString(8, substitution.getTeam().getName());
        pstmt.setInt(9, substitution.getTeam().getTournamentId());
        pstmt.setInt(10, substitution.getMinute());
        pstmt.executeUpdate();
        pstmt.close();
        
        int substitutionId = dbManager.getLastInsertId();
        substitution.setId(substitutionId);
    }
    
    /**
     * Validate that both players exist in the team before creating substitution
     */
    private void validatePlayersExist(Substitution substitution) throws SQLException {
        String teamName = substitution.getTeam().getName();
        int tournamentId = substitution.getTeam().getTournamentId();
        String playerInName = substitution.getInPlayer().getName();
        String playerOutName = substitution.getOutPlayer().getName();
        
        // Check if player_in exists in the team
        String checkPlayerInSql = """
            SELECT COUNT(*) FROM players p 
            JOIN teams t ON p.team_id = t.id 
            WHERE p.name = ? AND t.name = ? AND t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(checkPlayerInSql);
        pstmt.setString(1, playerInName);
        pstmt.setString(2, teamName);
        pstmt.setInt(3, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        boolean playerInExists = false;
        if (rs.next()) {
            playerInExists = rs.getInt(1) > 0;
        }
        rs.close();
        pstmt.close();
        
        if (!playerInExists) {
            throw new SQLException("Cầu thủ vào sân không thuộc đội: " + playerInName + " (Team: " + teamName + ")");
        }
        
        // Check if player_out exists in the team
        String checkPlayerOutSql = """
            SELECT COUNT(*) FROM players p 
            JOIN teams t ON p.team_id = t.id 
            WHERE p.name = ? AND t.name = ? AND t.tournament_id = ?
        """;
        
        pstmt = dbManager.getConnection().prepareStatement(checkPlayerOutSql);
        pstmt.setString(1, playerOutName);
        pstmt.setString(2, teamName);
        pstmt.setInt(3, tournamentId);
        rs = pstmt.executeQuery();
        
        boolean playerOutExists = false;
        if (rs.next()) {
            playerOutExists = rs.getInt(1) > 0;
        }
        rs.close();
        pstmt.close();
        
        if (!playerOutExists) {
            throw new SQLException("Cầu thủ bị thay ra không thuộc trận đấu: " + playerOutName);
        }
    }
    
    @Override
    public void update(Substitution substitution) throws SQLException {
        String sql = """
            UPDATE substitutions 
            SET match_id = ?, team_id = ?, player_in_id = ?, player_out_id = ?, minute = ?
            WHERE id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, substitution.getMatch().getId());
        pstmt.setInt(2, substitution.getTeam().getId());
        pstmt.setInt(3, substitution.getInPlayer().getId());
        pstmt.setInt(4, substitution.getOutPlayer().getId());
        pstmt.setInt(5, substitution.getMinute());
        pstmt.setInt(6, substitution.getId());
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    @Override
    public Optional<Substitution> findById(int id) throws SQLException {
        String sql = """
            SELECT s.id, s.match_id, s.team_id, s.player_in_id, s.player_out_id, s.minute,
                   t.name as team_name, pin.name as player_in_name, pout.name as player_out_name
            FROM substitutions s
            JOIN teams t ON s.team_id = t.id
            JOIN players pin ON s.player_in_id = pin.id
            JOIN players pout ON s.player_out_id = pout.id
            WHERE s.id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        
        Optional<Substitution> result = Optional.empty();
        if (rs.next()) {
            // TODO: Create Substitution object with full relationships
            result = Optional.empty(); // Simplified for now
        }
        
        rs.close();
        pstmt.close();
        return result;
    }
    
    @Override
    public List<Substitution> findByMatch(int matchId) throws SQLException {
        List<Substitution> substitutions = new ArrayList<>();
        
        String sql = """
            SELECT s.id, s.match_id, s.team_id, s.player_in_id, s.player_out_id, s.minute,
                   t.name as team_name, pin.name as player_in_name, pout.name as player_out_name
            FROM substitutions s
            JOIN teams t ON s.team_id = t.id
            JOIN players pin ON s.player_in_id = pin.id
            JOIN players pout ON s.player_out_id = pout.id
            WHERE s.match_id = ?
            ORDER BY s.minute ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, matchId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Substitution objects
        }
        
        rs.close();
        pstmt.close();
        return substitutions;
    }
    
    @Override
    public List<Substitution> findByTeamAndTournament(String teamName, int tournamentId) throws SQLException {
        List<Substitution> substitutions = new ArrayList<>();
        
        String sql = """
            SELECT s.id, s.match_id, s.team_id, s.player_in_id, s.player_out_id, s.minute
            FROM substitutions s
            JOIN teams t ON s.team_id = t.id
            WHERE t.name = ? AND t.tournament_id = ?
            ORDER BY s.match_id ASC, s.minute ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Substitution objects
        }
        
        rs.close();
        pstmt.close();
        return substitutions;
    }
    
    @Override
    public int countSubstitutionsByTeam(String teamName, int tournamentId) throws SQLException {
        String sql = """
            SELECT COUNT(*) as substitution_count
            FROM substitutions s
            JOIN teams t ON s.team_id = t.id
            WHERE t.name = ? AND t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        int count = 0;
        if (rs.next()) {
            count = rs.getInt("substitution_count");
        }
        
        rs.close();
        pstmt.close();
        return count;
    }
    
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM substitutions WHERE id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    @Override
    public boolean exists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM substitutions WHERE id = ?";
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