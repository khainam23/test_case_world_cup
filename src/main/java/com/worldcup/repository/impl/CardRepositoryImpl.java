package com.worldcup.repository.impl;

import com.worldcup.database.DatabaseManager;
import com.worldcup.model.Card;
import com.worldcup.repository.CardRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation của CardRepository
 * Xử lý tất cả database operations cho Card entity
 */
public class CardRepositoryImpl implements CardRepository {
    
    private final DatabaseManager dbManager;
    
    public CardRepositoryImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    @Override
    public void save(Card card) throws SQLException {
        String sql = """
            INSERT INTO cards (match_id, player_id, team_id, card_type, minute)
            VALUES (?, 
                    ?, 
                    ?, 
                    ?, 
                    ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, card.getMatch().getId());
        pstmt.setInt(2, card.getPlayer().getId());
        pstmt.setInt(3, card.getTeam().getId());
        pstmt.setString(4, card.getType().getLabel().toUpperCase());
        pstmt.setInt(5, card.getMinutes());
        pstmt.executeUpdate();
        pstmt.close();
        
        int cardId = dbManager.getLastInsertId();
        card.setId(cardId);
    }
    
    @Override
    public void update(Card card) throws SQLException {
        String sql = """
            UPDATE cards 
            SET match_id = ?, player_id = ?, team_id = ?, card_type = ?, minute = ?
            WHERE id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, card.getMatch().getId());
        pstmt.setInt(2, card.getPlayer().getId());
        pstmt.setInt(3, card.getTeam().getId());
        pstmt.setString(4, card.getType().getLabel().toUpperCase());
        pstmt.setInt(5, card.getMinutes());
        pstmt.setInt(6, card.getId());
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    @Override
    public Optional<Card> findById(int id) throws SQLException {
        String sql = """
            SELECT c.id, c.match_id, c.player_id, c.team_id, c.card_type, c.minute,
                   p.name as player_name, t.name as team_name
            FROM cards c
            JOIN players p ON c.player_id = p.id
            JOIN teams t ON c.team_id = t.id
            WHERE c.id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        
        Optional<Card> result = Optional.empty();
        if (rs.next()) {
            // TODO: Create Card object with full relationships
            result = Optional.empty(); // Simplified for now
        }
        
        rs.close();
        pstmt.close();
        return result;
    }
    
    @Override
    public List<Card> findByMatch(int matchId) throws SQLException {
        List<Card> cards = new ArrayList<>();
        
        String sql = """
            SELECT c.id, c.match_id, c.player_id, c.team_id, c.card_type, c.minute,
                   p.name as player_name, t.name as team_name
            FROM cards c
            JOIN players p ON c.player_id = p.id
            JOIN teams t ON c.team_id = t.id
            WHERE c.match_id = ?
            ORDER BY c.minute ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, matchId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Card objects
        }
        
        rs.close();
        pstmt.close();
        return cards;
    }
    
    @Override
    public List<Card> findByPlayer(int playerId) throws SQLException {
        List<Card> cards = new ArrayList<>();
        
        String sql = """
            SELECT c.id, c.match_id, c.player_id, c.team_id, c.card_type, c.minute
            FROM cards c
            WHERE c.player_id = ?
            ORDER BY c.match_id ASC, c.minute ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, playerId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Card objects
        }
        
        rs.close();
        pstmt.close();
        return cards;
    }
    
    @Override
    public List<Card> findByTeamAndTournament(String teamName, int tournamentId) throws SQLException {
        List<Card> cards = new ArrayList<>();
        
        String sql = """
            SELECT c.id, c.match_id, c.player_id, c.team_id, c.card_type, c.minute
            FROM cards c
            JOIN teams t ON c.team_id = t.id
            WHERE t.name = ? AND t.tournament_id = ?
            ORDER BY c.match_id ASC, c.minute ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Card objects
        }
        
        rs.close();
        pstmt.close();
        return cards;
    }
    
    @Override
    public List<Card> findByTeamAndTournamentAndType(String teamName, int tournamentId, String cardType) throws SQLException {
        List<Card> cards = new ArrayList<>();
        
        String sql = """
            SELECT c.id, c.match_id, c.player_id, c.team_id, c.card_type, c.minute
            FROM cards c
            JOIN teams t ON c.team_id = t.id
            WHERE t.name = ? AND t.tournament_id = ? AND c.card_type = ?
            ORDER BY c.match_id ASC, c.minute ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        pstmt.setString(3, cardType);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // TODO: Create Card objects
        }
        
        rs.close();
        pstmt.close();
        return cards;
    }
    
    @Override
    public int countCardsByTeamAndType(String teamName, int tournamentId, String cardType) throws SQLException {
        String sql = """
            SELECT COUNT(*) as card_count
            FROM cards c
            JOIN teams t ON c.team_id = t.id
            JOIN matches m ON c.match_id = m.id
            WHERE t.name = ? AND t.tournament_id = ? AND c.card_type = ? AND m.match_type = 'GROUP'
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        pstmt.setString(3, cardType);
        ResultSet rs = pstmt.executeQuery();
        
        int count = 0;
        if (rs.next()) {
            count = rs.getInt("card_count");
        }
        
        rs.close();
        pstmt.close();
        return count;
    }
    
    @Override
    public int countCardsByPlayerAndType(String playerName, String teamName, int tournamentId, String cardType) throws SQLException {
        String sql = """
            SELECT COUNT(*) as card_count
            FROM cards c
            JOIN players p ON c.player_id = p.id
            JOIN teams t ON c.team_id = t.id
            WHERE p.name = ? AND t.name = ? AND t.tournament_id = ? AND c.card_type = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, playerName);
        pstmt.setString(2, teamName);
        pstmt.setInt(3, tournamentId);
        pstmt.setString(4, cardType);
        ResultSet rs = pstmt.executeQuery();
        
        int count = 0;
        if (rs.next()) {
            count = rs.getInt("card_count");
        }
        
        rs.close();
        pstmt.close();
        return count;
    }
    
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM cards WHERE id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    @Override
    public boolean exists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cards WHERE id = ?";
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