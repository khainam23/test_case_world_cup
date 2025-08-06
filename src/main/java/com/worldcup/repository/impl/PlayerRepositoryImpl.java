package com.worldcup.repository.impl;

import com.worldcup.database.DatabaseManager;
import com.worldcup.model.Player;
import com.worldcup.repository.PlayerRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation của PlayerRepository
 * Xử lý tất cả database operations cho Player entity
 */
public class PlayerRepositoryImpl implements PlayerRepository {
    
    private final DatabaseManager dbManager;
    
    public PlayerRepositoryImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    @Override
    public void save(Player player, int teamId) throws SQLException {
        // Check if player with same jersey number already exists for this team
        String checkSql = "SELECT COUNT(*) FROM players WHERE team_id = ? AND jersey_number = ?";
        PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkSql);
        checkStmt.setInt(1, teamId);
        checkStmt.setInt(2, player.getJerseyNumber());
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next() && rs.getInt(1) > 0) {
            rs.close();
            checkStmt.close();
            throw new SQLException("Player with jersey number " + player.getJerseyNumber() + 
                                 " already exists for team ID " + teamId);
        }
        rs.close();
        checkStmt.close();
        
        String sql = """
            INSERT INTO players (name, jersey_number, position, team_id, is_starting, 
                               yellow_cards, red_cards, is_eligible, goals)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, player.getName());
        pstmt.setInt(2, player.getJerseyNumber());
        pstmt.setString(3, player.getPosition());
        pstmt.setInt(4, teamId);
        pstmt.setBoolean(5, player.isStarting());
        pstmt.setInt(6, player.getYellowCards());
        pstmt.setInt(7, player.getRedCards());
        pstmt.setBoolean(8, player.isEligible());
        pstmt.setInt(9, player.getGoals());
        pstmt.executeUpdate();
        pstmt.close();
        
        int playerId = dbManager.getLastInsertId();
        player.setId(playerId);
    }
    
    @Override
    public void update(Player player) throws SQLException {
        String sql = """
            UPDATE players 
            SET name = ?, jersey_number = ?, position = ?, is_starting = ?, 
                yellow_cards = ?, red_cards = ?, is_eligible = ?, goals = ?
            WHERE id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, player.getName());
        pstmt.setInt(2, player.getJerseyNumber());
        pstmt.setString(3, player.getPosition());
        pstmt.setBoolean(4, player.isStarting());
        pstmt.setInt(5, player.getYellowCards());
        pstmt.setInt(6, player.getRedCards());
        pstmt.setBoolean(7, player.isEligible());
        pstmt.setInt(8, player.getGoals());
        pstmt.setInt(9, player.getId());
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    @Override
    public Optional<Player> findById(int id) throws SQLException {
        String sql = """
            SELECT id, name, jersey_number, position, is_starting, 
                   yellow_cards, red_cards, is_eligible, goals
            FROM players WHERE id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        
        Optional<Player> result = Optional.empty();
        if (rs.next()) {
            Player player = createPlayerFromResultSet(rs);
            result = Optional.of(player);
        }
        
        rs.close();
        pstmt.close();
        return result;
    }
    
    @Override
    public Optional<Player> findByNameAndTeam(String playerName, String teamName, int tournamentId) throws SQLException {
        String sql = """
            SELECT p.id, p.name, p.jersey_number, p.position, p.is_starting,
                   p.yellow_cards, p.red_cards, p.is_eligible, p.goals
            FROM players p
            JOIN teams t ON p.team_id = t.id
            WHERE p.name = ? AND t.name = ? AND t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, playerName);
        pstmt.setString(2, teamName);
        pstmt.setInt(3, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        Optional<Player> result = Optional.empty();
        if (rs.next()) {
            Player player = createPlayerFromResultSet(rs);
            result = Optional.of(player);
        }
        
        rs.close();
        pstmt.close();
        return result;
    }
    
    @Override
    public List<Player> findByTeam(int teamId) throws SQLException {
        List<Player> players = new ArrayList<>();
        
        String sql = """
            SELECT id, name, jersey_number, position, is_starting,
                   yellow_cards, red_cards, is_eligible, goals
            FROM players WHERE team_id = ?
            ORDER BY is_starting DESC, jersey_number ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, teamId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Player player = createPlayerFromResultSet(rs);
            players.add(player);
        }
        
        rs.close();
        pstmt.close();
        return players;
    }
    
    @Override
    public List<Player> findByTeamNameAndTournament(String teamName, int tournamentId) throws SQLException {
        List<Player> players = new ArrayList<>();
        
        String sql = """
            SELECT p.id, p.name, p.jersey_number, p.position, p.is_starting,
                   p.yellow_cards, p.red_cards, p.is_eligible, p.goals
            FROM players p
            JOIN teams t ON p.team_id = t.id
            WHERE t.name = ? AND t.tournament_id = ?
            ORDER BY p.is_starting DESC, p.jersey_number ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Player player = createPlayerFromResultSet(rs);
            players.add(player);
        }
        
        rs.close();
        pstmt.close();
        return players;
    }
    
    @Override
    public List<Player> findStartingPlayersByTeam(int teamId) throws SQLException {
        List<Player> players = new ArrayList<>();
        
        String sql = """
            SELECT id, name, jersey_number, position, is_starting,
                   yellow_cards, red_cards, is_eligible, goals
            FROM players WHERE team_id = ? AND is_starting = true
            ORDER BY jersey_number ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, teamId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Player player = createPlayerFromResultSet(rs);
            players.add(player);
        }
        
        rs.close();
        pstmt.close();
        return players;
    }
    
    @Override
    public List<Player> findSubstitutePlayersByTeam(int teamId) throws SQLException {
        List<Player> players = new ArrayList<>();
        
        String sql = """
            SELECT id, name, jersey_number, position, is_starting,
                   yellow_cards, red_cards, is_eligible, goals
            FROM players WHERE team_id = ? AND is_starting = false
            ORDER BY jersey_number ASC
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, teamId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Player player = createPlayerFromResultSet(rs);
            players.add(player);
        }
        
        rs.close();
        pstmt.close();
        return players;
    }
    
    @Override
    public void updateGoals(Player player, String teamName, int tournamentId) throws SQLException {
        String sql = """
            UPDATE players 
            SET goals = goals + 1 
            WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, player.getName());
        pstmt.setString(2, teamName);
        pstmt.setInt(3, tournamentId);
        pstmt.executeUpdate();
        pstmt.close();
        
        // Cập nhật object
        player.scoreGoal();
    }
    
    @Override
    public void updateCards(Player player, String teamName, int tournamentId, String cardType) throws SQLException {
        String sql;
        if ("YELLOW".equals(cardType)) {
            sql = """
                UPDATE players 
                SET yellow_cards = yellow_cards + 1 
                WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)
            """;
        } else {
            sql = """
                UPDATE players 
                SET red_cards = red_cards + 1 
                WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)
            """;
        }
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, player.getName());
        pstmt.setString(2, teamName);
        pstmt.setInt(3, tournamentId);
        pstmt.executeUpdate();
        pstmt.close();
        
        // Cập nhật object
        if ("YELLOW".equals(cardType)) {
            player.receiveYellowCard();
        } else {
            player.receiveRedCard();
        }
    }
    
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM players WHERE id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    @Override
    public boolean exists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM players WHERE id = ?";
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
    
    @Override
    public Optional<Integer> getPlayerId(String playerName, String teamName, int tournamentId) throws SQLException {
        String sql = """
            SELECT p.id 
            FROM players p
            JOIN teams t ON p.team_id = t.id
            WHERE p.name = ? AND t.name = ? AND t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, playerName);
        pstmt.setString(2, teamName);
        pstmt.setInt(3, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        Optional<Integer> result = Optional.empty();
        if (rs.next()) {
            result = Optional.of(rs.getInt("id"));
        }
        
        rs.close();
        pstmt.close();
        return result;
    }
    
    @Override
    public void updateStartingStatus(int playerId, boolean isStarting) throws SQLException {
        String sql = "UPDATE players SET is_starting = ? WHERE id = ?";
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setBoolean(1, isStarting);
        pstmt.setInt(2, playerId);
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    /**
     * Tạo Player object từ ResultSet
     */
    private Player createPlayerFromResultSet(ResultSet rs) throws SQLException {
        Player player = new Player(
            rs.getString("name"),
            rs.getInt("jersey_number"),
            rs.getString("position")
        );
        
        player.setId(rs.getInt("id"));
        player.setYellowCards(rs.getInt("yellow_cards"));
        player.setRedCard(rs.getInt("red_cards") > 0);
        player.setEligible(rs.getBoolean("is_eligible"));
        player.setGoals(rs.getInt("goals"));
        player.setStarting(rs.getBoolean("is_starting"));
        
        return player;
    }
}