package com.worldcup.service;

import com.worldcup.model.Player;
import com.worldcup.database.DatabaseManager;
import java.sql.*;
import java.util.*;

/**
 * Service class để xử lý logic business liên quan đến Player
 * Tuân theo Single Responsibility Principle
 */
public class PlayerService {
    private DatabaseManager dbManager;
    
    public PlayerService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    /**
     * Lấy top scorers và sắp xếp bằng Java thay vì SQL ORDER BY
     */
    public List<PlayerGoalStats> getTopScorersCalculatedInJava(int tournamentId, int limit) throws SQLException {
        // Lấy tất cả players và goals
        Map<String, PlayerGoalStats> playerStatsMap = new HashMap<>();
        
        String sql = """
            SELECT p.id, p.name, p.position, t.name as team_name,
                   g.id as goal_id
            FROM players p
            JOIN teams t ON p.team_id = t.id
            LEFT JOIN goals g ON p.id = g.player_id
            WHERE t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            int playerId = rs.getInt("id");
            String playerName = rs.getString("name");
            String teamName = rs.getString("team_name");
            String position = rs.getString("position");
            boolean hasGoal = rs.getObject("goal_id") != null;
            
            // Tạo hoặc cập nhật player stats
            PlayerGoalStats stats = playerStatsMap.computeIfAbsent(playerName, 
                k -> new PlayerGoalStats(playerId, playerName, teamName, position, 0));
            
            if (hasGoal) {
                stats.incrementGoals();
            }
        }
        
        rs.close();
        pstmt.close();
        
        // Chuyển đổi map thành list và sắp xếp bằng Java
        List<PlayerGoalStats> playerStatsList = new ArrayList<>(playerStatsMap.values());
        
        // Lọc chỉ những cầu thủ có bàn thắng
        playerStatsList = playerStatsList.stream()
            .filter(stats -> stats.getGoals() > 0)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        // Sắp xếp bằng Java thay vì SQL ORDER BY
        playerStatsList.sort(new PlayerGoalComparator());
        
        // Giới hạn số lượng kết quả
        if (limit > 0 && playerStatsList.size() > limit) {
            playerStatsList = playerStatsList.subList(0, limit);
        }
        
        return playerStatsList;
    }

    /**
     * Comparator để sắp xếp players theo số bàn thắng
     */
    private static class PlayerGoalComparator implements Comparator<PlayerGoalStats> {
        @Override
        public int compare(PlayerGoalStats p1, PlayerGoalStats p2) {
            // Sắp xếp theo số bàn thắng giảm dần
            int goalComparison = Integer.compare(p2.getGoals(), p1.getGoals());
            if (goalComparison != 0) {
                return goalComparison;
            }
            
            // Nếu bằng nhau thì sắp xếp theo tên
            return p1.getPlayerName().compareTo(p2.getPlayerName());
        }
    }
    
    /**
     * Inner class để lưu thống kê bàn thắng của cầu thủ
     */
    public static class PlayerGoalStats {
        private int playerId;
        private String playerName;
        private String teamName;
        private String position;
        private int goals;
        
        public PlayerGoalStats(int playerId, String playerName, String teamName, String position, int goals) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.teamName = teamName;
            this.position = position;
            this.goals = goals;
        }
        
        public void incrementGoals() {
            this.goals++;
        }
        
        // Getters
        public int getPlayerId() { return playerId; }
        public String getPlayerName() { return playerName; }
        public String getTeamName() { return teamName; }
        public String getPosition() { return position; }
        public int getGoals() { return goals; }
        
        @Override
        public String toString() {
            return String.format("%s (%s) - %s: %d bàn", 
                playerName, teamName, position, goals);
        }
    }
    
    /**
     * Inner class để lưu thống kê thẻ của cầu thủ
     */
    public static class PlayerCardStats {
        private String playerName;
        private String teamName;
        private String position;
        private int yellowCards;
        private int redCards;
        
        public PlayerCardStats(String playerName, String teamName, String position, 
                             int yellowCards, int redCards) {
            this.playerName = playerName;
            this.teamName = teamName;
            this.position = position;
            this.yellowCards = yellowCards;
            this.redCards = redCards;
        }
        
        public void incrementYellowCards() {
            this.yellowCards++;
        }
        
        public void incrementRedCards() {
            this.redCards++;
        }
        
        // Getters
        public String getPlayerName() { return playerName; }
        public String getTeamName() { return teamName; }
        public String getPosition() { return position; }
        public int getYellowCards() { return yellowCards; }
        public int getRedCards() { return redCards; }
        
        @Override
        public String toString() {
            return String.format("%s (%s) - %s: %d thẻ vàng, %d thẻ đỏ", 
                playerName, teamName, position, yellowCards, redCards);
        }
    }
}