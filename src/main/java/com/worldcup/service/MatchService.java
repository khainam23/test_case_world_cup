package com.worldcup.service;

import com.worldcup.database.DatabaseManager;
import java.sql.*;
import java.util.*;

/**
 * Service class để xử lý logic business liên quan đến Match
 * Tuân theo Single Responsibility Principle
 */
public class MatchService {
    private DatabaseManager dbManager;
    
    public MatchService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    /**
     * Lấy tất cả matches và sắp xếp bằng Java thay vì SQL ORDER BY
     */
    public List<MatchResult> getAllMatchesWithCalculatedResults(int tournamentId) throws SQLException {
        List<MatchResult> matches = new ArrayList<>();
        
        String sql = """
            SELECT m.id, m.team_a_score, m.team_b_score, m.match_type, 
                   m.match_date, m.venue, m.referee, m.status,
                   ta.name as team_a_name, tb.name as team_b_name,
                   g.name as group_name
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            LEFT JOIN (
                SELECT t.id, g.name 
                FROM teams t 
                JOIN groups g ON t.group_id = g.id
            ) g ON m.team_a_id = g.id
            WHERE ta.tournament_id = ? AND tb.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            MatchResult match = new MatchResult();
            match.id = rs.getInt("id");
            match.teamAName = rs.getString("team_a_name");
            match.teamBName = rs.getString("team_b_name");
            match.teamAScore = rs.getInt("team_a_score");
            match.teamBScore = rs.getInt("team_b_score");
            match.matchType = rs.getString("match_type");
            match.matchDate = rs.getString("match_date");
            match.venue = rs.getString("venue");
            match.referee = rs.getString("referee");
            match.status = rs.getString("status");
            match.groupName = rs.getString("group_name");
            
            // Tính toán winner bằng Java thay vì SQL
            match.winnerName = calculateWinner(match.teamAName, match.teamBName, 
                                             match.teamAScore, match.teamBScore);
            
            matches.add(match);
        }
        
        rs.close();
        pstmt.close();
        
        // Sắp xếp matches bằng Java thay vì SQL ORDER BY
        matches.sort(new MatchDateComparator());
        
        return matches;
    }
    
    /**
     * Tính toán winner của trận đấu bằng Java
     */
    private String calculateWinner(String teamAName, String teamBName, int teamAScore, int teamBScore) {
        if (teamAScore > teamBScore) {
            return teamAName;
        } else if (teamBScore > teamAScore) {
            return teamBName;
        } else {
            return "Draw"; // Hòa
        }
    }
    
    /**
     * Lấy matches theo group và sắp xếp bằng Java
     */
    public List<MatchResult> getMatchesByGroupSorted(int tournamentId, String groupName) throws SQLException {
        List<MatchResult> matches = new ArrayList<>();
        
        String sql = """
            SELECT m.id, m.team_a_score, m.team_b_score, m.match_type, 
                   m.match_date, m.venue, m.referee, m.status,
                   ta.name as team_a_name, tb.name as team_b_name,
                   g.name as group_name
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            JOIN (
                SELECT t.id, g.name 
                FROM teams t 
                JOIN groups g ON t.group_id = g.id
            ) g ON m.team_a_id = g.id
            WHERE ta.tournament_id = ? AND tb.tournament_id = ? AND g.name = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        pstmt.setInt(2, tournamentId);
        pstmt.setString(3, groupName);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            MatchResult match = new MatchResult();
            match.id = rs.getInt("id");
            match.teamAName = rs.getString("team_a_name");
            match.teamBName = rs.getString("team_b_name");
            match.teamAScore = rs.getInt("team_a_score");
            match.teamBScore = rs.getInt("team_b_score");
            match.matchType = rs.getString("match_type");
            match.matchDate = rs.getString("match_date");
            match.venue = rs.getString("venue");
            match.referee = rs.getString("referee");
            match.status = rs.getString("status");
            match.groupName = rs.getString("group_name");
            
            // Tính toán winner bằng Java
            match.winnerName = calculateWinner(match.teamAName, match.teamBName, 
                                             match.teamAScore, match.teamBScore);
            
            matches.add(match);
        }
        
        rs.close();
        pstmt.close();
        
        // Sắp xếp bằng Java
        matches.sort(new MatchDateComparator());
        
        return matches;
    }
    
    /**
     * Tính toán thống kê trận đấu cho một team bằng Java
     */
    public TeamMatchStats calculateTeamMatchStats(String teamName, int tournamentId) throws SQLException {
        List<MatchResult> allMatches = getAllMatchesWithCalculatedResults(tournamentId);
        
        TeamMatchStats stats = new TeamMatchStats();
        stats.teamName = teamName;
        
        // Tính toán bằng Java thay vì SQL
        for (MatchResult match : allMatches) {
            if (match.teamAName.equals(teamName) || match.teamBName.equals(teamName)) {
                stats.totalMatches++;
                
                boolean isTeamA = match.teamAName.equals(teamName);
                int teamScore = isTeamA ? match.teamAScore : match.teamBScore;
                int opponentScore = isTeamA ? match.teamBScore : match.teamAScore;
                
                stats.goalsFor += teamScore;
                stats.goalsAgainst += opponentScore;
                
                if (teamScore > opponentScore) {
                    stats.wins++;
                } else if (teamScore == opponentScore) {
                    stats.draws++;
                } else {
                    stats.losses++;
                }
            }
        }
        
        // Tính toán các chỉ số khác
        stats.points = stats.wins * 3 + stats.draws * 1;
        stats.goalDifference = stats.goalsFor - stats.goalsAgainst;
        
        return stats;
    }
    
    /**
     * Find highest scoring match using Java comparison
     */
    public MatchResult findHighestScoringMatch(int tournamentId) throws SQLException {
        List<MatchResult> matches = getAllMatchesWithCalculatedResults(tournamentId);
        
        MatchResult highestScoringMatch = null;
        int maxGoals = -1;
        
        // Tìm kiếm bằng Java thay vì MAX() trong SQL
        for (MatchResult match : matches) {
            int totalGoals = match.teamAScore + match.teamBScore;
            if (totalGoals > maxGoals) {
                maxGoals = totalGoals;
                highestScoringMatch = match;
            }
        }
        
        return highestScoringMatch;
    }
    
    /**
     * Lấy tất cả trận đấu knockout và sắp xếp theo thứ tự vòng đấu
     */
    public List<MatchResult> getKnockoutMatchesSorted(int tournamentId) throws SQLException {
        List<MatchResult> allMatches = getAllMatchesWithCalculatedResults(tournamentId);
        
        // Filter knockout matches using Java
        List<MatchResult> knockoutMatches = new ArrayList<>();
        for (MatchResult match : allMatches) {
            if (!"GROUP".equals(match.matchType)) {
                knockoutMatches.add(match);
            }
        }
        
        // Sắp xếp theo thứ tự vòng đấu bằng Java
        knockoutMatches.sort(new KnockoutStageComparator());
        
        return knockoutMatches;
    }
    
    /**
     * Comparator để sắp xếp matches theo ngày
     */
    private static class MatchDateComparator implements Comparator<MatchResult> {
        @Override
        public int compare(MatchResult m1, MatchResult m2) {
            if (m1.matchDate != null && m2.matchDate != null) {
                int dateComparison = m1.matchDate.compareTo(m2.matchDate);
                if (dateComparison != 0) {
                    return dateComparison;
                }
            }
            
            // Nếu ngày bằng nhau hoặc null, sắp xếp theo ID
            return Integer.compare(m1.id, m2.id);
        }
    }
    
    /**
     * Comparator để sắp xếp matches knockout theo thứ tự vòng đấu
     */
    private static class KnockoutStageComparator implements Comparator<MatchResult> {
        private static final Map<String, Integer> STAGE_ORDER = Map.of(
            "ROUND_16", 1,
            "QUARTER", 2,
            "SEMI", 3,
            "THIRD_PLACE", 4,
            "FINAL", 5
        );
        
        @Override
        public int compare(MatchResult m1, MatchResult m2) {
            int stage1 = STAGE_ORDER.getOrDefault(m1.matchType, 0);
            int stage2 = STAGE_ORDER.getOrDefault(m2.matchType, 0);
            
            int stageComparison = Integer.compare(stage1, stage2);
            if (stageComparison != 0) {
                return stageComparison;
            }
            
            // Nếu cùng vòng đấu, sắp xếp theo ngày
            if (m1.matchDate != null && m2.matchDate != null) {
                return m1.matchDate.compareTo(m2.matchDate);
            }
            
            return Integer.compare(m1.id, m2.id);
        }
    }
    
    /**
     * Inner class để lưu kết quả trận đấu
     */
    public static class MatchResult {
        public int id;
        public String teamAName;
        public String teamBName;
        public int teamAScore;
        public int teamBScore;
        public String matchType;
        public String matchDate;
        public String venue;
        public String referee;
        public String status;
        public String groupName;
        public String winnerName;
        
        @Override
        public String toString() {
            return String.format("%s %d - %d %s (%s)", 
                teamAName, teamAScore, teamBScore, teamBName, 
                winnerName != null ? "Winner: " + winnerName : "Draw");
        }
    }
    
    /**
     * Inner class để lưu thống kê trận đấu của team
     */
    public static class TeamMatchStats {
        public String teamName;
        public int totalMatches;
        public int wins;
        public int draws;
        public int losses;
        public int goalsFor;
        public int goalsAgainst;
        public int goalDifference;
        public int points;
        
        @Override
        public String toString() {
            return String.format("%s: %d matches, %d wins, %d draws, %d losses, %d points, GD: %d", 
                teamName, totalMatches, wins, draws, losses, points, goalDifference);
        }
    }
}