package com.worldcup.repository.impl;

import com.worldcup.database.DatabaseManager;
import com.worldcup.model.Player;
import com.worldcup.model.Team;
import com.worldcup.repository.PlayerRepository;
import com.worldcup.repository.TeamRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation của TeamRepository
 * Xử lý tất cả database operations cho Team entity
 */
public class TeamRepositoryImpl implements TeamRepository {
    
    private final DatabaseManager dbManager;
    private final PlayerRepository playerRepository;
    
    public TeamRepositoryImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.playerRepository = new PlayerRepositoryImpl(dbManager);
    }
    
    @Override
    public void save(Team team) throws SQLException {
        // Lưu team
        String teamSql = """
            INSERT INTO teams (name, region, coach, medical_staff, is_host, tournament_id, group_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(teamSql);
        pstmt.setString(1, team.getName());
        pstmt.setString(2, team.getRegion());
        pstmt.setString(3, team.getCoach());
        pstmt.setString(4, team.getMedicalStaff());
        pstmt.setBoolean(5, team.isHost());
        pstmt.setInt(6, team.getTournamentId());
        pstmt.setInt(7, team.getGroupId());
        pstmt.executeUpdate();
        pstmt.close();
        
        int teamId = dbManager.getLastInsertId();
        team.setId(teamId);
        
        // Lưu assistant coaches
        saveAssistantCoaches(team);
        
        // Lưu players
        savePlayers(team);
    }
    
    @Override
    public void update(Team team) throws SQLException {
        String sql = """
            UPDATE teams 
            SET name = ?, region = ?, coach = ?, medical_staff = ?, is_host = ?, 
                tournament_id = ?, group_id = ?
            WHERE id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, team.getName());
        pstmt.setString(2, team.getRegion());
        pstmt.setString(3, team.getCoach());
        pstmt.setString(4, team.getMedicalStaff());
        pstmt.setBoolean(5, team.isHost());
        pstmt.setInt(6, team.getTournamentId());
        pstmt.setInt(7, team.getGroupId());
        pstmt.setInt(8, team.getId());
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    @Override
    public Optional<Team> findById(int id) throws SQLException {
        String sql = """
            SELECT id, name, region, coach, medical_staff, is_host, tournament_id, group_id
            FROM teams WHERE id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        
        Optional<Team> result = Optional.empty();
        if (rs.next()) {
            Team team = createTeamFromResultSet(rs);
            result = Optional.of(team);
        }
        
        rs.close();
        pstmt.close();
        return result;
    }
    
    @Override
    public Optional<Team> findByNameAndTournament(String name, int tournamentId) throws SQLException {
        String sql = """
            SELECT id, name, region, coach, medical_staff, is_host, tournament_id, group_id
            FROM teams WHERE name = ? AND tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, name);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        Optional<Team> result = Optional.empty();
        if (rs.next()) {
            Team team = createTeamFromResultSet(rs);
            result = Optional.of(team);
        }
        
        rs.close();
        pstmt.close();
        return result;
    }
    
    @Override
    public List<Team> findByTournament(int tournamentId) throws SQLException {
        List<Team> teams = new ArrayList<>();
        
        String sql = """
            SELECT id, name, region, coach, medical_staff, is_host, tournament_id, group_id
            FROM teams WHERE tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Team team = createTeamFromResultSet(rs);
            teams.add(team);
        }
        
        rs.close();
        pstmt.close();
        return teams;
    }
    
    @Override
    public List<Team> findByGroupAndTournament(String groupName, int tournamentId) throws SQLException {
        List<Team> teams = new ArrayList<>();
        
        String sql = """
            SELECT t.id, t.name, t.region, t.coach, t.medical_staff, t.is_host, 
                   t.tournament_id, t.group_id
            FROM teams t
            JOIN groups g ON t.group_id = g.id
            WHERE g.name = ? AND t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, groupName);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Team team = createTeamFromResultSet(rs);
            teams.add(team);
        }
        
        rs.close();
        pstmt.close();
        return teams;
    }
    
    @Override
    public List<Team> findByTournamentWithPlayers(int tournamentId) throws SQLException {
        List<Team> teams = findByTournament(tournamentId);
        
        // Load players cho mỗi team
        for (Team team : teams) {
            loadPlayersForTeam(team);
        }
        
        return teams;
    }
    
    @Override
    public List<Team> findByGroupAndTournamentWithPlayers(String groupName, int tournamentId) throws SQLException {
        List<Team> teams = findByGroupAndTournament(groupName, tournamentId);
        
        // Load players cho mỗi team
        for (Team team : teams) {
            loadPlayersForTeam(team);
        }
        
        return teams;
    }
    
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM teams WHERE id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    @Override
    public boolean exists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM teams WHERE id = ?";
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
    public Optional<Integer> getTeamId(String teamName, int tournamentId) throws SQLException {
        String sql = "SELECT id FROM teams WHERE name = ? AND tournament_id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        Optional<Integer> result = Optional.empty();
        if (rs.next()) {
            result = Optional.of(rs.getInt("id"));
        }
        
        rs.close();
        pstmt.close();
        return result;
    }
    
    /**
     * Tạo Team object từ ResultSet
     */
    private Team createTeamFromResultSet(ResultSet rs) throws SQLException {
        List<String> assistantCoaches = loadAssistantCoaches(rs.getInt("id"));
        List<Player> players = new ArrayList<>(); // Sẽ load riêng nếu cần
        
        Team team = new Team(
            rs.getString("name"),
            rs.getString("region"),
            rs.getString("coach"),
            assistantCoaches,
            rs.getString("medical_staff"),
            rs.getBoolean("is_host")
        );
        
        team.setId(rs.getInt("id"));
        team.setGroupId(rs.getInt("group_id"));
        team.setTournamentId(rs.getInt("tournament_id"));
        
        return team;
    }
    
    /**
     * Load assistant coaches cho team
     */
    private List<String> loadAssistantCoaches(int teamId) throws SQLException {
        List<String> assistants = new ArrayList<>();
        
        String sql = "SELECT name FROM assistant_coaches WHERE team_id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, teamId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            assistants.add(rs.getString("name"));
        }
        
        rs.close();
        pstmt.close();
        return assistants;
    }
    
    /**
     * Lưu assistant coaches
     */
    private void saveAssistantCoaches(Team team) throws SQLException {
        for (String assistant : team.getAssistantCoaches()) {
            String sql = "INSERT INTO assistant_coaches (name, team_id) VALUES (?, ?)";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, assistant);
            pstmt.setInt(2, team.getId());
            pstmt.executeUpdate();
            pstmt.close();
        }
    }
    
    /**
     * Lưu players
     */
    private void savePlayers(Team team) throws SQLException {
        // Lưu starting players
        for (Player player : team.getStartingPlayers()) {
            player.setStarting(true); // Đảm bảo starting players có isStarting = true
            playerRepository.save(player, team.getId());
        }
        
        // Lưu substitute players
        for (Player player : team.getSubstitutePlayers()) {
            player.setStarting(false); // Đảm bảo substitute players có isStarting = false
            playerRepository.save(player, team.getId());
        }
    }
    
    /**
     * Load players cho team
     */
    private void loadPlayersForTeam(Team team) throws SQLException {
        List<Player> startingPlayers = playerRepository.findStartingPlayersByTeam(team.getId());
        List<Player> substitutePlayers = playerRepository.findSubstitutePlayersByTeam(team.getId());
        
        team.setStartingPlayers(startingPlayers);
        team.setSubstitutePlayers(substitutePlayers);
    }
}