package com.worldcup.repository.impl;

import com.worldcup.database.DatabaseManager;
import com.worldcup.model.Match;
import com.worldcup.repository.MatchRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation của MatchRepository
 * Xử lý tất cả database operations cho Match entity
 */
public class MatchRepositoryImpl implements MatchRepository {
    
    private final DatabaseManager dbManager;
    
    public MatchRepositoryImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    @Override
    public int save(Match match, String venue, String referee) throws SQLException {
        String sql = """
            INSERT INTO matches (team_a_id, team_b_id, team_a_score, team_b_score, match_type, 
                               match_date, venue, referee, winner_id)
            VALUES ((SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                    (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                    ?, ?, ?, ?, ?, ?, ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, match.getTeamA().getName());
        pstmt.setInt(2, match.getTeamA().getTournamentId());
        pstmt.setString(3, match.getTeamB().getName());
        pstmt.setInt(4, match.getTeamB().getTournamentId());
        pstmt.setInt(5, match.getTeamAScore());
        pstmt.setInt(6, match.getTeamBScore());
        pstmt.setString(7, match.getMatchType());
        pstmt.setString(8, match.getMatchDate());
        pstmt.setString(9, venue);
        pstmt.setString(10, referee);
        
        // Set winner_id
        if (match.getWinnerId() != null) {
            pstmt.setInt(11, match.getWinnerId());
        } else {
            pstmt.setNull(11, java.sql.Types.INTEGER);
        }
        
        pstmt.executeUpdate();
        pstmt.close();
        
        int matchId = dbManager.getLastInsertId();
        match.setId(matchId);
        
        return matchId;
    }
    
    @Override
    public void update(Match match) throws SQLException {
        String sql = """
            UPDATE matches 
            SET team_a_score = ?, team_b_score = ?, match_type = ?, 
                match_date = ?, winner_id = ?
            WHERE id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, match.getTeamAScore());
        pstmt.setInt(2, match.getTeamBScore());
        pstmt.setString(3, match.getMatchType());
        pstmt.setString(4, match.getMatchDate());
        
        if (match.getWinnerId() != null) {
            pstmt.setInt(5, match.getWinnerId());
        } else {
            pstmt.setNull(5, java.sql.Types.INTEGER);
        }
        
        pstmt.setInt(6, match.getId());
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    @Override
    public Optional<Match> findById(int id) throws SQLException {
        String sql = """
            SELECT m.id, m.team_a_id, m.team_b_id, m.team_a_score, m.team_b_score,
                   m.match_type, m.match_date, m.venue, m.referee, m.winner_id,
                   ta.name as team_a_name, tb.name as team_b_name
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            WHERE m.id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        
        Optional<Match> result = Optional.empty();
        if (rs.next()) {
            // TODO: Create Match object with full Team objects
            // This requires loading teams with players
            result = Optional.empty(); // Simplified for now
        }
        
        rs.close();
        pstmt.close();
        return result;
    }
    
    @Override
    public List<Match> findByTournament(int tournamentId) throws SQLException {
        List<Match> matches = new ArrayList<>();
        
        String sql = """
            SELECT m.id, m.team_a_id, m.team_b_id, m.team_a_score, m.team_b_score,
                   m.match_type, m.match_date, m.venue, m.referee, m.winner_id,
                   ta.name as team_a_name, tb.name as team_b_name
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            WHERE ta.tournament_id = ? AND tb.tournament_id = ?
            ORDER BY m.match_date ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Match objects
        }
        
        rs.close();
        pstmt.close();
        return matches;
    }
    
    @Override
    public List<Match> findByTournamentAndType(int tournamentId, String matchType) throws SQLException {
        List<Match> matches = new ArrayList<>();
        
        String sql = """
            SELECT m.id, m.team_a_id, m.team_b_id, m.team_a_score, m.team_b_score,
                   m.match_type, m.match_date, m.venue, m.referee, m.winner_id,
                   ta.name as team_a_name, tb.name as team_b_name
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            WHERE ta.tournament_id = ? AND tb.tournament_id = ? AND m.match_type = ?
            ORDER BY m.match_date ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        pstmt.setInt(2, tournamentId);
        pstmt.setString(3, matchType);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Match objects
        }
        
        rs.close();
        pstmt.close();
        return matches;
    }
    
    @Override
    public List<Match> findByTeam(String teamName, int tournamentId) throws SQLException {
        List<Match> matches = new ArrayList<>();
        
        String sql = """
            SELECT m.id, m.team_a_id, m.team_b_id, m.team_a_score, m.team_b_score,
                   m.match_type, m.match_date, m.venue, m.referee, m.winner_id,
                   ta.name as team_a_name, tb.name as team_b_name
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            WHERE (ta.name = ? OR tb.name = ?) 
            AND ta.tournament_id = ? AND tb.tournament_id = ?
            ORDER BY m.match_date ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setString(2, teamName);
        pstmt.setInt(3, tournamentId);
        pstmt.setInt(4, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Match objects
        }
        
        rs.close();
        pstmt.close();
        return matches;
    }
    
    @Override
    public List<Match> findGroupStageMatchesByTeam(String teamName, int tournamentId) throws SQLException {
        List<Match> matches = new ArrayList<>();
        
        String sql = """
            SELECT m.id, m.team_a_id, m.team_b_id, m.team_a_score, m.team_b_score,
                   m.match_type, m.match_date, m.venue, m.referee, m.winner_id,
                   ta.name as team_a_name, tb.name as team_b_name
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            WHERE (ta.name = ? OR tb.name = ?) 
            AND ta.tournament_id = ? AND tb.tournament_id = ?
            AND m.match_type = 'GROUP'
            ORDER BY m.match_date ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setString(2, teamName);
        pstmt.setInt(3, tournamentId);
        pstmt.setInt(4, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Match objects
        }
        
        rs.close();
        pstmt.close();
        return matches;
    }
    
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM matches WHERE id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    @Override
    public boolean exists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM matches WHERE id = ?";
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