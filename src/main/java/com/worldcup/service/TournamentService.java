package com.worldcup.service;

import com.worldcup.model.Group;
import com.worldcup.model.Team;
import com.worldcup.database.DatabaseManager;
import com.worldcup.service.PlayerService.PlayerGoalStats;
import java.sql.*;
import java.util.*;

/**
 * Service class để xử lý logic business liên quan đến Tournament
 * Tuân theo Single Responsibility Principle
 */
public class TournamentService {
    private DatabaseManager dbManager;
    private TeamService teamService;
    private PlayerService playerService;
    
    public TournamentService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.teamService = new TeamService(dbManager);
        this.playerService = new PlayerService(dbManager);
    }
    
    /**
     * Tính toán tất cả thống kê tournament bằng Java thay vì SQL
     */
    public TournamentStats calculateTournamentStats(int tournamentId) throws SQLException {
        TournamentStats stats = new TournamentStats();
        
        // 1. Tính tổng số trận đấu bằng Java
        stats.totalMatches = calculateTotalMatches(tournamentId);
        
        // 2. Tính tổng số bàn thắng bằng Java
        stats.totalGoals = calculateTotalGoals(tournamentId);
        
        // 3. Tính tổng số thẻ bằng Java
        CardStats cardStats = calculateTotalCards(tournamentId);
        stats.totalYellowCards = cardStats.yellowCards;
        stats.totalRedCards = cardStats.redCards;
        
        // 4. tỉnh tổng thay thế người
        stats.totalSubstitutions = calculateTotalSubstitutions(tournamentId);
        
        // 5. Tìm vua phá lưới bằng Java
        List<PlayerGoalStats> topScorers = playerService.getTopScorersCalculatedInJava(tournamentId, 1);
        if (!topScorers.isEmpty()) {
            PlayerGoalStats topScorer = topScorers.get(0);
            stats.topScorerId = topScorer.getPlayerId();
            stats.topScorerName = topScorer.getPlayerName();
            stats.topScorerGoals = topScorer.getGoals();
            
            
        } else {
            // Không có cầu thủ nào ghi bàn
            stats.topScorerId = 0;
            stats.topScorerName = null;
            stats.topScorerGoals = 0;
            System.out.printf("ℹ️ Tournament %d: Chưa có cầu thủ nào ghi bàn\n", tournamentId);
        }
        
        return stats;
    }
    
    /**
     * Tính tổng số trận đấu bằng cách đếm trong Java
     */
    private int calculateTotalMatches(int tournamentId) throws SQLException {
        String sql = """
            SELECT m.id
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            WHERE ta.tournament_id = ? AND tb.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        int count = 0;
        while (rs.next()) {
            count++; // Đếm bằng Java thay vì COUNT() trong SQL
        }
        
        rs.close();
        pstmt.close();
        
        return count;
    }
    
    /**
     * Tính tổng số bàn thắng bằng cách cộng dồn trong Java
     */
    private int calculateTotalGoals(int tournamentId) throws SQLException {
        String sql = """
            SELECT g.id
            FROM goals g
            JOIN teams t ON g.team_id = t.id
            WHERE t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        int totalGoals = 0;
        while (rs.next()) {
            totalGoals++; // Cộng dồn bằng Java thay vì SUM() trong SQL
        }
        
        rs.close();
        pstmt.close();
        
        return totalGoals;
    }
    
    /**
     * Tính tổng số thẻ bằng cách phân loại và đếm trong Java
     */
    private CardStats calculateTotalCards(int tournamentId) throws SQLException {
        String sql = """
            SELECT c.card_type
            FROM cards c
            JOIN teams t ON c.team_id = t.id
            WHERE t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        int yellowCards = 0;
        int redCards = 0;
        
        // Phân loại và đếm bằng Java thay vì GROUP BY và COUNT() trong SQL
        while (rs.next()) {
            String cardType = rs.getString("card_type");
            if ("YELLOW".equals(cardType)) {
                yellowCards++;
            } else if ("RED".equals(cardType)) {
                redCards++;
            }
        }
        
        rs.close();
        pstmt.close();
        
        return new CardStats(yellowCards, redCards);
    }
    
    /**
     * Tính tổng số thay người bằng cách đếm trong Java
     */
    private int calculateTotalSubstitutions(int tournamentId) throws SQLException {
        String sql = """
            SELECT s.id
            FROM substitutions s
            JOIN teams t ON s.team_id = t.id
            WHERE t.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        int count = 0;
        while (rs.next()) {
            count++; // Đếm bằng Java thay vì COUNT() trong SQL
        }
        
        rs.close();
        pstmt.close();
        
        return count;
    }
    
    /**
     * Lấy bảng xếp hạng của tất cả các bảng đấu, sắp xếp bằng Java
     */
    public Map<String, List<Team>> getAllGroupStandingsCalculatedInJava(int tournamentId) throws SQLException {
        Map<String, List<Team>> groupStandings = new HashMap<>();
        
        // Lấy tất cả tên bảng
        List<String> groupNames = getAllGroupNames(tournamentId);
        
        // Tính toán bảng xếp hạng cho từng bảng
        for (String groupName : groupNames) {
            List<Team> teams = teamService.getTeamsByGroupSorted(tournamentId, groupName);
            groupStandings.put(groupName, teams);
        }
        
        return groupStandings;
    }
    
    /**
     * Lấy bảng xếp hạng của tất cả các bảng đấu
     * Sử dụng cho knockout stage
     */
    public Map<Group, List<Team>> getAllGroupStandings(int tournamentId, List<Group> groups) throws SQLException {
        Map<Group, List<Team>> groupStandings = new HashMap<>();

        for (Group group : groups) {
            List<Team> teams = teamService.getTeamsByGroupSortedWithPlayers(tournamentId, group);
            groupStandings.put(group, teams);
        }
        
        return groupStandings;
    }
    
    /**
     * Lấy tất cả tên bảng đấu
     */
    private List<String> getAllGroupNames(int tournamentId) throws SQLException {
        List<String> groupNames = new ArrayList<>();
        
        String sql = "SELECT name FROM groups WHERE tournament_id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            groupNames.add(rs.getString("name"));
        }
        
        rs.close();
        pstmt.close();
        
        // Sắp xếp tên bảng bằng Java
        Collections.sort(groupNames);
        
        return groupNames;
    }
    
    /**
     * Tìm 2 đội đầu bảng từ mỗi group bằng logic Java
     */
    public List<Team> getQualifiedTeamsCalculatedInJava(int tournamentId) throws SQLException {
        List<Team> qualifiedTeams = new ArrayList<>();
        
        Map<String, List<Team>> groupStandings = getAllGroupStandingsCalculatedInJava(tournamentId);
        
        // Lấy 2 đội đầu từ mỗi bảng bằng logic Java
        for (List<Team> teams : groupStandings.values()) {
            if (teams.size() >= 2) {
                qualifiedTeams.add(teams.get(0)); // Đội nhất bảng
                qualifiedTeams.add(teams.get(1)); // Đội nhì bảng
            }
        }
        
        return qualifiedTeams;
    }
    
    /**
     * Tính toán thống kê trung bình bàn thắng mỗi trận bằng Java
     */
    public double calculateAverageGoalsPerMatch(int tournamentId) throws SQLException {
        TournamentStats stats = calculateTournamentStats(tournamentId);
        
        if (stats.totalMatches == 0) {
            return 0.0;
        }
        
        // Tính toán bằng Java thay vì AVG() trong SQL
        return (double) stats.totalGoals / stats.totalMatches;
    }
    
    /**
     * Inner class để lưu thống kê thẻ
     */
    private static class CardStats {
        int yellowCards;
        int redCards;
        
        CardStats(int yellowCards, int redCards) {
            this.yellowCards = yellowCards;
            this.redCards = redCards;
        }
    }
    
    /**
     * Cập nhật champion, runner-up và 2 đội đồng hạng 3 cho tournament
     */
    public void updateTournamentWinners(int tournamentId, Integer championId, Integer runnerUpId, Integer thirdPlaceId01, Integer thirdPlaceId02) throws SQLException {

    
        // Kiểm tra xem tournament_stats record có tồn tại không
        ensureTournamentStatsExists(tournamentId);
        
        // Lưu cả 2 đội hạng 3 vào các cột riêng biệt
        String sql = """
            UPDATE tournament_stats 
            SET champion_id = ?, runner_up_id = ?, third_place_id_01 = ?, third_place_id_02 = ?
            WHERE tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setObject(1, championId);
        pstmt.setObject(2, runnerUpId);
        pstmt.setObject(3, thirdPlaceId01); // Đội hạng 3 thứ nhất
        pstmt.setObject(4, thirdPlaceId02); // Đội hạng 3 thứ hai
        pstmt.setInt(5, tournamentId);
        
        int rowsUpdated = pstmt.executeUpdate();
        pstmt.close();
        
    }
    
    /**
     * Cập nhật champion, runner-up và third place cho tournament (method cũ để tương thích)
     * @deprecated Sử dụng updateTournamentWinners(int, Integer, Integer, Integer, Integer) thay thế
     */
    @Deprecated
    public void updateTournamentWinners(int tournamentId, Integer championId, Integer runnerUpId, Integer thirdPlaceId) throws SQLException {
        // Gọi method mới với thirdPlaceId02 = null
        updateTournamentWinners(tournamentId, championId, runnerUpId, thirdPlaceId, null);
    }
    
    /**
     * Cập nhật chỉ champion cho tournament
     */
    public void updateTournamentChampion(int tournamentId, int championId) throws SQLException {
        ensureTournamentStatsExists(tournamentId);
        
        String sql = "UPDATE tournament_stats SET champion_id = ? WHERE tournament_id = ?";
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, championId);
        pstmt.setInt(2, tournamentId);
        
        int rowsUpdated = pstmt.executeUpdate();
        pstmt.close();
        
        if (rowsUpdated > 0) {
            System.out.println(" Đã cập nhật champion cho tournament ID: " + tournamentId);
        }
    }
    
    /**
     * Cập nhật chỉ runner-up cho tournament
     */
    public void updateTournamentRunnerUp(int tournamentId, int runnerUpId) throws SQLException {
        ensureTournamentStatsExists(tournamentId);
        
        String sql = "UPDATE tournament_stats SET runner_up_id = ? WHERE tournament_id = ?";
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, runnerUpId);
        pstmt.setInt(2, tournamentId);
        
        int rowsUpdated = pstmt.executeUpdate();
        pstmt.close();
        
        if (rowsUpdated > 0) {
            System.out.println(" Đã cập nhật runner-up cho tournament ID: " + tournamentId);
        }
    }
    
    /**
     * Cập nhật chỉ third place cho tournament
     */
    public void updateTournamentThirdPlace(int tournamentId, int thirdPlaceId) throws SQLException {
        ensureTournamentStatsExists(tournamentId);
        
        String sql = "UPDATE tournament_stats SET third_place_id_01 = ? WHERE tournament_id = ?";
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, thirdPlaceId);
        pstmt.setInt(2, tournamentId);
        
        int rowsUpdated = pstmt.executeUpdate();
        pstmt.close();
        
        if (rowsUpdated > 0) {
            System.out.println(" Đã cập nhật third place cho tournament ID: " + tournamentId);
        }
    }
    
    /**
     * Đảm bảo tournament_stats record tồn tại cho tournament
     */
    private void ensureTournamentStatsExists(int tournamentId) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM tournament_stats WHERE tournament_id = ?";
        PreparedStatement checkPstmt = dbManager.getConnection().prepareStatement(checkSql);
        checkPstmt.setInt(1, tournamentId);
        ResultSet rs = checkPstmt.executeQuery();
        
        boolean exists = rs.next() && rs.getInt(1) > 0;
        rs.close();
        checkPstmt.close();
        
        if (!exists) {
            // Tạo tournament_stats record mới
            String insertSql = """
                INSERT INTO tournament_stats (tournament_id, total_goals, total_matches, 
                                            total_yellow_cards, total_red_cards, total_substitutions, 
                                            top_scorer_goals)
                VALUES (?, 0, 0, 0, 0, 0, 0)
            """;
            
            PreparedStatement insertPstmt = dbManager.getConnection().prepareStatement(insertSql);
            insertPstmt.setInt(1, tournamentId);
            insertPstmt.executeUpdate();
            insertPstmt.close();
            
            
        }
    }
    
    /**
     * Lấy thông tin winners hiện tại của tournament (bao gồm 2 đội đồng hạng 3)
     */
    public Map<String, Object> getTournamentWinners(int tournamentId) throws SQLException {
        String sql = """
            SELECT 
                ts.champion_id,
                ts.runner_up_id,
                ts.third_place_id_01,
                ts.third_place_id_02,
                champion.name as champion_name,
                runner_up.name as runner_up_name,
                third_place_01.name as third_place_01_name,
                third_place_02.name as third_place_02_name
            FROM tournament_stats ts
            LEFT JOIN teams champion ON ts.champion_id = champion.id
            LEFT JOIN teams runner_up ON ts.runner_up_id = runner_up.id
            LEFT JOIN teams third_place_01 ON ts.third_place_id_01 = third_place_01.id
            LEFT JOIN teams third_place_02 ON ts.third_place_id_02 = third_place_02.id
            WHERE ts.tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        Map<String, Object> winners = new HashMap<>();
        if (rs.next()) {
            winners.put("championId", rs.getObject("champion_id"));
            winners.put("runnerUpId", rs.getObject("runner_up_id"));
            winners.put("thirdPlaceId", rs.getObject("third_place_id_01")); // Để tương thích với code cũ, trả về third_place_id_01
            winners.put("thirdPlaceId01", rs.getObject("third_place_id_01"));
            winners.put("thirdPlaceId02", rs.getObject("third_place_id_02"));
            winners.put("championName", rs.getString("champion_name"));
            winners.put("runnerUpName", rs.getString("runner_up_name"));
            winners.put("thirdPlaceName", rs.getString("third_place_01_name")); // Để tương thích với code cũ
            winners.put("thirdPlace01Name", rs.getString("third_place_01_name"));
            winners.put("thirdPlace02Name", rs.getString("third_place_02_name"));
        }
        
        rs.close();
        pstmt.close();
        
        return winners;
    }

    /**
     * Cập nhật tournament stats trực tiếp vào database
     * Method tiện ích để gọi từ bất kỳ đâu khi cần cập nhật stats
     */
    public void updateTournamentStatsToDatabase(int tournamentId) throws SQLException {
        // Tính toán stats mới
        TournamentStats stats = calculateTournamentStats(tournamentId);
        
        // Cập nhật vào database
        updateOrInsertTournamentStats(tournamentId, stats);
        
        System.out.println("✅ Đã cập nhật tournament stats cho tournament ID: " + tournamentId);
    }
    
    /**
     * Cập nhật hoặc insert tournament stats vào database
     */
    private void updateOrInsertTournamentStats(int tournamentId, TournamentStats stats) throws SQLException {
        // Kiểm tra xem đã có record chưa
        String checkSql = "SELECT COUNT(*) FROM tournament_stats WHERE tournament_id = ?";
        PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkSql);
        checkStmt.setInt(1, tournamentId);
        ResultSet rs = checkStmt.executeQuery();
        
        boolean exists = rs.next() && rs.getInt(1) > 0;
        rs.close();
        checkStmt.close();
        
        if (exists) {
            // Update existing record
            String updateSql = """
                UPDATE tournament_stats
                SET total_goals = ?,
                    total_matches = ?,
                    total_yellow_cards = ?,
                    total_red_cards = ?,
                    total_substitutions = ?,
                    top_scorer_id = ?,
                    top_scorer_goals = ?
                WHERE tournament_id = ?
            """;
            
            PreparedStatement updateStmt = dbManager.getConnection().prepareStatement(updateSql);
            updateStmt.setInt(1, stats.totalGoals);
            updateStmt.setInt(2, stats.totalMatches);
            updateStmt.setInt(3, stats.totalYellowCards);
            updateStmt.setInt(4, stats.totalRedCards);
            updateStmt.setInt(5, stats.totalSubstitutions);
            if (stats.topScorerId > 0) {
                updateStmt.setInt(6, stats.topScorerId);
            } else {
                updateStmt.setNull(6, java.sql.Types.INTEGER);
            }
            updateStmt.setInt(7, stats.topScorerGoals);
            updateStmt.setInt(8, tournamentId);
            updateStmt.executeUpdate();
            updateStmt.close();
            
        } else {
            // Insert new record
            String insertSql = """
                INSERT INTO tournament_stats
                (tournament_id, total_goals, total_matches, total_yellow_cards,
                 total_red_cards, total_substitutions, top_scorer_id, top_scorer_goals)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
            PreparedStatement insertStmt = dbManager.getConnection().prepareStatement(insertSql);
            insertStmt.setInt(1, tournamentId);
            insertStmt.setInt(2, stats.totalGoals);
            insertStmt.setInt(3, stats.totalMatches);
            insertStmt.setInt(4, stats.totalYellowCards);
            insertStmt.setInt(5, stats.totalRedCards);
            insertStmt.setInt(6, stats.totalSubstitutions);
            if (stats.topScorerId > 0) {
                insertStmt.setInt(7, stats.topScorerId);
            } else {
                insertStmt.setNull(7, java.sql.Types.INTEGER);
            }
            insertStmt.setInt(8, stats.topScorerGoals);
            insertStmt.executeUpdate();
            insertStmt.close();
        }
    }

    // ==================== METHODS MOVED FROM TournamentStatsCalculator ====================
    
    /**
     * Tính toán lại tất cả tournament stats
     * Moved from TournamentStatsCalculator
     */
    public void recalculateAllTournamentStats() {
        try {
            // Lấy tất cả tournaments
            String selectTournamentsSql = "SELECT id, name FROM tournaments ORDER BY id";
            PreparedStatement stmt = dbManager.getConnection().prepareStatement(selectTournamentsSql);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                int tournamentId = rs.getInt("id");
                String tournamentName = rs.getString("name");

                recalculateTournamentStats(tournamentId, tournamentName);
                count++;
            }

            rs.close();
            stmt.close();

            System.out.println("✅ Đã tính toán lại stats cho " + count + " tournaments!");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tính toán tournament stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tính toán lại stats cho một tournament cụ thể
     * Moved from TournamentStatsCalculator - sử dụng lại method calculateTournamentStats đã có
     */
    public void recalculateTournamentStats(int tournamentId, String tournamentName) throws SQLException {
        // Sử dụng method calculateTournamentStats đã có trong TournamentService
        TournamentStats stats = calculateTournamentStats(tournamentId);

        // Sử dụng method updateOrInsertTournamentStats đã có
        updateOrInsertTournamentStats(tournamentId, stats);
    }

    /**
     * Hiển thị tất cả tournament stats
     * Moved from TournamentStatsCalculator
     */
    public void displayAllTournamentStats() {
        try {
            System.out.println("📊 THỐNG KÊ TẤT CẢ TOURNAMENTS");
            System.out.println("=".repeat(100));

            String sql = """
                SELECT t.id, t.name, t.year, t.host_country,
                       ts.total_matches, ts.total_goals, ts.total_yellow_cards,
                       ts.total_red_cards, ts.total_substitutions,
                       ts.top_scorer_goals
                FROM tournaments t
                LEFT JOIN tournament_stats ts ON t.id = ts.tournament_id
                ORDER BY t.year DESC, t.id DESC
            """;

            PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.printf("%-25s | %d | %-15s%n",
                    rs.getString("name"),
                    rs.getInt("year"),
                    rs.getString("host_country"));

                System.out.printf("   📊 Trận: %-3d | Bàn: %-3d | Vàng: %-3d | Đỏ: %-2d | Thay: %-3d | Vua phá lưới: (%d bàn)%n",
                    rs.getInt("total_matches"),
                    rs.getInt("total_goals"),
                    rs.getInt("total_yellow_cards"),
                    rs.getInt("total_red_cards"),
                    rs.getInt("total_substitutions"),
                    rs.getInt("top_scorer_goals"));
                System.out.println();
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi hiển thị tournament stats: " + e.getMessage());
        }
    }

    /**
     * Tìm vua phá lưới của tournament
     * Moved from TournamentStatsCalculator
     */
    public TopScorer findTopScorer(int tournamentId) throws SQLException {
        String sql = """
            SELECT p.id, p.name, COUNT(g.id) as goals_count
            FROM players p
            JOIN teams t ON p.team_id = t.id
            LEFT JOIN goals g ON p.id = g.player_id
            WHERE t.tournament_id = ?
            GROUP BY p.id, p.name
            HAVING goals_count > 0
            ORDER BY goals_count DESC, p.name ASC
            LIMIT 1
        """;

        PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql);
        stmt.setInt(1, tournamentId);
        ResultSet rs = stmt.executeQuery();

        TopScorer topScorer = new TopScorer();
        if (rs.next()) {
            topScorer.playerId = rs.getInt("id");
            topScorer.playerName = rs.getString("name");
            topScorer.goals = rs.getInt("goals_count");
        }

        rs.close();
        stmt.close();
        return topScorer;
    }

    /**
     * Tính tổng số trận đấu của tournament (alternative method from TournamentStatsCalculator)
     * Sử dụng lại method calculateTotalMatches đã có
     */
    public int calculateTotalMatchesAlternative(int tournamentId) throws SQLException {
        // Sử dụng lại method calculateTotalMatches đã có
        return calculateTotalMatches(tournamentId);
    }

    /**
     * Tính tổng số bàn thắng của tournament từ bảng teams (alternative method from TournamentStatsCalculator)
     */
    public int calculateTotalGoalsFromTeams(int tournamentId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(goals_for), 0) as total_goals
            FROM teams
            WHERE tournament_id = ?
        """;

        PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql);
        stmt.setInt(1, tournamentId);
        ResultSet rs = stmt.executeQuery();

        int total = 0;
        if (rs.next()) {
            total = rs.getInt("total_goals");
        }

        rs.close();
        stmt.close();
        return total;
    }

    /**
     * Tính tổng số thẻ vàng của tournament (alternative method from TournamentStatsCalculator)
     */
    public int calculateTotalYellowCardsAlternative(int tournamentId) throws SQLException {
        String sql = """
            SELECT COUNT(*) as total_yellow_cards
            FROM cards c
            JOIN matches m ON c.match_id = m.id
            JOIN teams t ON c.team_id = t.id
            WHERE t.tournament_id = ? AND c.card_type = 'YELLOW'
        """;

        PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql);
        stmt.setInt(1, tournamentId);
        ResultSet rs = stmt.executeQuery();

        int total = 0;
        if (rs.next()) {
            total = rs.getInt("total_yellow_cards");
        }

        rs.close();
        stmt.close();
        return total;
    }

    /**
     * Tính tổng số thẻ đỏ của tournament (alternative method from TournamentStatsCalculator)
     */
    public int calculateTotalRedCardsAlternative(int tournamentId) throws SQLException {
        String sql = """
            SELECT COUNT(*) as total_red_cards
            FROM cards c
            JOIN matches m ON c.match_id = m.id
            JOIN teams t ON c.team_id = t.id
            WHERE t.tournament_id = ? AND c.card_type = 'RED'
        """;

        PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql);
        stmt.setInt(1, tournamentId);
        ResultSet rs = stmt.executeQuery();

        int total = 0;
        if (rs.next()) {
            total = rs.getInt("total_red_cards");
        }

        rs.close();
        stmt.close();
        return total;
    }

    /**
     * Tính tổng số thay người của tournament (alternative method from TournamentStatsCalculator)
     * Sử dụng lại method calculateTotalSubstitutions đã có
     */
    public int calculateTotalSubstitutionsAlternative(int tournamentId) throws SQLException {
        // Sử dụng lại method calculateTotalSubstitutions đã có
        return calculateTotalSubstitutions(tournamentId);
    }

    /**
     * Cập nhật hoặc tạo mới record trong tournament_stats (alternative method from TournamentStatsCalculator)
     */
    public void updateTournamentStatsAlternative(int tournamentId, int totalMatches, int totalGoals,
                                     int totalYellowCards, int totalRedCards, int totalSubstitutions,
                                     TopScorer topScorer) throws SQLException {

        // Kiểm tra xem đã có record chưa
        String checkSql = "SELECT id FROM tournament_stats WHERE tournament_id = ?";
        PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkSql);
        checkStmt.setInt(1, tournamentId);
        ResultSet rs = checkStmt.executeQuery();

        boolean exists = rs.next();
        rs.close();
        checkStmt.close();

        if (exists) {
            // Update existing record
            String updateSql = """
                UPDATE tournament_stats
                SET total_goals = ?,
                    total_matches = ?,
                    total_yellow_cards = ?,
                    total_red_cards = ?,
                    total_substitutions = ?,
                    top_scorer_id = ?,
                    top_scorer_goals = ?
                WHERE tournament_id = ?
            """;

            PreparedStatement updateStmt = dbManager.getConnection().prepareStatement(updateSql);
            updateStmt.setInt(1, totalGoals);
            updateStmt.setInt(2, totalMatches);
            updateStmt.setInt(3, totalYellowCards);
            updateStmt.setInt(4, totalRedCards);
            updateStmt.setInt(5, totalSubstitutions);
            if (topScorer.playerId > 0) {
                updateStmt.setInt(6, topScorer.playerId);
            } else {
                updateStmt.setNull(6, java.sql.Types.INTEGER);
            }
            updateStmt.setInt(7, topScorer.goals);
            updateStmt.setInt(8, tournamentId);
            updateStmt.executeUpdate();
            updateStmt.close();

        } else {
            // Insert new record
            String insertSql = """
                INSERT INTO tournament_stats
                (tournament_id, total_goals, total_matches, total_yellow_cards,
                 total_red_cards, total_substitutions, top_scorer_id, top_scorer_goals)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement insertStmt = dbManager.getConnection().prepareStatement(insertSql);
            insertStmt.setInt(1, tournamentId);
            insertStmt.setInt(2, totalGoals);
            insertStmt.setInt(3, totalMatches);
            insertStmt.setInt(4, totalYellowCards);
            insertStmt.setInt(5, totalRedCards);
            insertStmt.setInt(6, totalSubstitutions);
            if (topScorer.playerId > 0) {
                insertStmt.setInt(7, topScorer.playerId);
            } else {
                insertStmt.setNull(7, java.sql.Types.INTEGER);
            }
            insertStmt.setInt(8, topScorer.goals);

            insertStmt.executeUpdate();
            insertStmt.close();
        }
    }

    // ==================== INNER CLASSES ====================

    /**
     * Class để lưu thông tin vua phá lưới
     * Moved from TournamentStatsCalculator
     */
    public static class TopScorer {
        public int playerId = 0;
        public String playerName = "";
        public int goals = 0;
        
        @Override
        public String toString() {
            return String.format("TopScorer{playerId=%d, playerName='%s', goals=%d}", 
                playerId, playerName, goals);
        }
    }

    /**
     * Inner class để lưu tất cả thống kê tournament
     */
    public static class TournamentStats {
        public int totalMatches;
        public int totalGoals;
        public int totalYellowCards;
        public int totalRedCards;
        public int totalSubstitutions;
        public int topScorerId;
        public String topScorerName;
        public int topScorerGoals;
        
        @Override
        public String toString() {
            return String.format("""
                Tournament Statistics:
                - Total Matches: %d
                - Total Goals: %d
                - Total Yellow Cards: %d
                - Total Red Cards: %d
                - Total Substitutions: %d
                - Top Scorer: %s (%d goals)
                """, 
                totalMatches, totalGoals, totalYellowCards, totalRedCards, 
                totalSubstitutions, topScorerName != null ? topScorerName : "N/A", topScorerGoals);
        }
    }

    // ==================== MAIN METHOD FOR TESTING ====================
    
    /**
     * Main method để test các method đã chuyển từ TournamentStatsCalculator
     */
    public static void main(String[] args) {
        TournamentService tournamentService = new TournamentService(new DatabaseManager());

        System.out.println("🌟 TOURNAMENT SERVICE - STATS CALCULATOR");
        System.out.println("=".repeat(60));

        // Hiển thị stats trước khi tính toán
        System.out.println("📋 TRƯỚC KHI TÍNH TOÁN:");
        tournamentService.displayAllTournamentStats();

        System.out.println("\n" + "=".repeat(60));

        // Tính toán lại tất cả stats
        tournamentService.recalculateAllTournamentStats();

        System.out.println("=".repeat(60));

        // Hiển thị stats sau khi tính toán
        System.out.println("📋 SAU KHI TÍNH TOÁN:");
        tournamentService.displayAllTournamentStats();
    }
}