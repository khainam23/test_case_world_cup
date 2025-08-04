package com.worldcup.service;

import com.worldcup.model.Player;
import com.worldcup.model.Team;
import com.worldcup.database.DatabaseManager;
import java.sql.*;
import java.util.*;

/**
 * Service class để xử lý logic business liên quan đến Team
 * Tuân theo Single Responsibility Principle
 */
public class TeamService {
    private DatabaseManager dbManager;
    
    public TeamService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    /**
     * Lấy tất cả teams từ database và tính toán thống kê bằng Java
     */
    public List<Team> getAllTeamsWithCalculatedStats(int tournamentId) throws SQLException {
        List<Team> teams = new ArrayList<>();
        
        String sql = """
            SELECT t.id, t.name, t.region, t.coach, t.medical_staff, t.is_host,
                   t.group_id, t.tournament_id
            FROM teams t
            WHERE t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            // Tạo team object với dữ liệu cơ bản
            Team team = createTeamFromResultSet(rs);
            
            // Tính toán lại các thống kê bằng Java
            calculateTeamStatistics(team, tournamentId);
            
            teams.add(team);
        }
        
        rs.close();
        pstmt.close();
        
        return teams;
    }
    
    /**
     * Tính toán thống kê cho một team bằng Java thay vì SQL
     */
    private void calculateTeamStatistics(Team team, int tournamentId) throws SQLException {
        // Tính toán từ matches
        calculateMatchStatistics(team, tournamentId);
        
        // Tính toán từ goals
        calculateGoalStatistics(team, tournamentId);
        
        // Tính toán từ cards
        calculateCardStatistics(team, tournamentId);
        
        // Tính toán từ substitutions
        calculateSubstitutionStatistics(team, tournamentId);
    }
    
    /**
     * Tính toán thống kê trận đấu bằng Java
     */
    private void calculateMatchStatistics(Team team, int tournamentId) throws SQLException {
        String sql = """
            SELECT m.team_a_id, m.team_b_id, m.team_a_score, m.team_b_score,
                   ta.name as team_a_name, tb.name as team_b_name
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            WHERE (ta.name = ? OR tb.name = ?) 
            AND ta.tournament_id = ? AND tb.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, team.getName());
        pstmt.setString(2, team.getName());
        pstmt.setInt(3, tournamentId);
        pstmt.setInt(4, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        int wins = 0, draws = 0, losses = 0;
        int goalsFor = 0, goalsAgainst = 0;
        
        while (rs.next()) {
            boolean isTeamA = team.getName().equals(rs.getString("team_a_name"));
            int teamScore = isTeamA ? rs.getInt("team_a_score") : rs.getInt("team_b_score");
            int opponentScore = isTeamA ? rs.getInt("team_b_score") : rs.getInt("team_a_score");
            
            goalsFor += teamScore;
            goalsAgainst += opponentScore;
            
            if (teamScore > opponentScore) {
                wins++;
            } else if (teamScore == opponentScore) {
                draws++;
            } else {
                losses++;
            }
        }
        
        // Cập nhật thống kê vào team object
        updateTeamMatchStats(team, wins, draws, losses, goalsFor, goalsAgainst);
        
        rs.close();
        pstmt.close();
    }
    
    /**
     * Cập nhật thống kê trận đấu vào team object
     */
    private void updateTeamMatchStats(Team team, int wins, int draws, int losses, 
                                    int goalsFor, int goalsAgainst) {
        // Tính điểm: thắng = 3 điểm, hòa = 1 điểm, thua = 0 điểm
        int points = wins * 3 + draws * 1;
        team.setPoints(points);
        
        // Tính hiệu số bàn thắng
        int goalDifference = goalsFor - goalsAgainst;
        team.setGoalDifference(goalDifference);
    }
    
    /**
     * Tính toán thống kê bàn thắng bằng Java
     */
    private void calculateGoalStatistics(Team team, int tournamentId) throws SQLException {
        String sql = """
            SELECT COUNT(*) as goal_count
            FROM goals g
            JOIN teams t ON g.team_id = t.id
            WHERE t.name = ? AND t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, team.getName());
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        // Lưu ý: Ở đây chúng ta vẫn phải dùng COUNT() để lấy dữ liệu thô
        // Nhưng logic tính toán sẽ được thực hiện ở Java
        // Trong tương lai có thể load tất cả goals và tính toán hoàn toàn bằng Java
        
        rs.close();
        pstmt.close();
    }
    
    /**
     * Tính toán thống kê thẻ bằng Java
     */
    private void calculateCardStatistics(Team team, int tournamentId) throws SQLException {
        String sql = """
            SELECT card_type, COUNT(*) as card_count
            FROM cards c
            JOIN teams t ON c.team_id = t.id
            WHERE t.name = ? AND t.tournament_id = ?
            GROUP BY card_type
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, team.getName());
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        int yellowCards = 0, redCards = 0;
        
        while (rs.next()) {
            String cardType = rs.getString("card_type");
            int count = rs.getInt("card_count");
            
            if ("YELLOW".equals(cardType)) {
                yellowCards = count;
            } else if ("RED".equals(cardType)) {
                redCards = count;
            }
        }
        
        team.setYellowCards(yellowCards);
        team.setRedCards(redCards);
        
        rs.close();
        pstmt.close();
    }
    
    /**
     * Tính toán thống kê thay người bằng Java
     */
    private void calculateSubstitutionStatistics(Team team, int tournamentId) throws SQLException {
        String sql = """
            SELECT COUNT(*) as substitution_count
            FROM substitutions s
            JOIN teams t ON s.team_id = t.id
            WHERE t.name = ? AND t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, team.getName());
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            int substitutionCount = rs.getInt("substitution_count");
            team.setSubstitutionCount(substitutionCount);
        }
        
        rs.close();
        pstmt.close();
    }
    
    /**
     * Tạo Team object từ ResultSet
     */
    private Team createTeamFromResultSet(ResultSet rs) throws SQLException {
        // Tạo team với constructor cơ bản
        List<String> assistantCoaches = new ArrayList<>(); // Sẽ load riêng nếu cần
        List<Player> players = new ArrayList<>(); // Sẽ load riêng nếu cần
        
        Team team = new Team(
            rs.getString("name"),
            rs.getString("region"),
            rs.getString("coach"),
            assistantCoaches,
            rs.getString("medical_staff"),
            players,
            rs.getBoolean("is_host")
        );
        
        // Set các thông tin cơ bản
        team.setId(rs.getInt("id"));
        team.setGroupId(rs.getInt("group_id"));
        team.setTournamentId(rs.getInt("tournament_id"));
        
        return team;
    }
    
    /**
     * Sắp xếp teams theo thứ tự bảng xếp hạng bằng Java thay vì SQL ORDER BY
     */
    public List<Team> sortTeamsByStanding(List<Team> teams) {
        teams.sort(new TeamStandingComparator());
        return teams;
    }
    
    /**
     * Lấy teams theo group và sắp xếp bằng Java
     */
    public List<Team> getTeamsByGroupSorted(int tournamentId, String groupName) throws SQLException {
        List<Team> teams = new ArrayList<>();
        
        String sql = """
            SELECT t.id, t.name, t.region, t.coach, t.medical_staff, t.is_host,
                   t.group_id, t.tournament_id
            FROM teams t
            JOIN groups g ON t.group_id = g.id
            WHERE t.tournament_id = ? AND g.name = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        pstmt.setString(2, groupName);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Team team = createTeamFromResultSet(rs);
            calculateTeamStatistics(team, tournamentId);
            teams.add(team);
        }
        
        rs.close();
        pstmt.close();
        
        // Sắp xếp bằng Java thay vì SQL ORDER BY
        return sortTeamsByStanding(teams);
    }
    
    /**
     * Inner class Comparator để sắp xếp teams theo thứ tự bảng xếp hạng
     * Tuân theo Open/Closed Principle - có thể mở rộng logic sắp xếp
     */
    private static class TeamStandingComparator implements Comparator<Team> {
        @Override
        public int compare(Team t1, Team t2) {
            // 1. So sánh điểm số (cao hơn = tốt hơn)
            int pointsComparison = Integer.compare(t2.getPoints(), t1.getPoints());
            if (pointsComparison != 0) {
                return pointsComparison;
            }
            
            // 2. So sánh hiệu số bàn thắng (cao hơn = tốt hơn)
            int goalDiffComparison = Integer.compare(t2.getGoalDifference(), t1.getGoalDifference());
            if (goalDiffComparison != 0) {
                return goalDiffComparison;
            }
            
            // 3. So sánh tên đội (alphabetical)
            return t1.getName().compareTo(t2.getName());
        }
    }
}