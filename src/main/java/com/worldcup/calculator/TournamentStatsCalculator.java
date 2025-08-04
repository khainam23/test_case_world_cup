package com.worldcup.calculator;

import com.worldcup.database.DatabaseManager;
import com.worldcup.service.TournamentService;
import com.worldcup.service.TournamentService.TournamentStats;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Class để tính toán lại tất cả các chỉ số trong table tournament_stats
 * Sử dụng TournamentService để tính toán bằng Java thay vì SQL
 * Tuân theo Dependency Inversion Principle
 */
public class TournamentStatsCalculator {
    private DatabaseManager dbManager;
    private TournamentService tournamentService;

    public TournamentStatsCalculator() {
        this.dbManager = new DatabaseManager();
        this.tournamentService = new TournamentService(dbManager);
    }

    public TournamentStatsCalculator(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.tournamentService = new TournamentService(dbManager);
    }

    /**
     * Tính toán lại tất cả tournament stats
     */
    public void recalculateAllTournamentStats() {
        try {
            System.out.println("🔄 Bắt đầu tính toán lại tất cả tournament stats...");

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
     * Sử dụng TournamentService để tính toán bằng Java
     */
    public void recalculateTournamentStats(int tournamentId, String tournamentName) throws SQLException {
        System.out.println("🏆 Đang tính toán stats cho: " + tournamentName + " (ID: " + tournamentId + ")");

        // Sử dụng TournamentService để tính toán tất cả stats bằng Java
        TournamentStats stats = tournamentService.calculateTournamentStats(tournamentId);

        // Cập nhật hoặc tạo mới record trong tournament_stats
        updateTournamentStatsFromService(tournamentId, stats);

        // Hiển thị kết quả
        displayTournamentStatsFromService(tournamentName, stats);
    }

    /**
     * Cập nhật tournament stats từ TournamentService
     */
    private void updateTournamentStatsFromService(int tournamentId, TournamentStats stats) throws SQLException {
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

    /**
     * Hiển thị kết quả tính toán từ TournamentService
     */
    private void displayTournamentStatsFromService(String tournamentName, TournamentStats stats) {
        System.out.println("   📊 Kết quả tính toán:");
        System.out.println("      🏟️  Tổng số trận: " + stats.totalMatches);
        System.out.println("      ⚽ Tổng số bàn thắng: " + stats.totalGoals);
        System.out.println("      🟨 Tổng số thẻ vàng: " + stats.totalYellowCards);
        System.out.println("      🟥 Tổng số thẻ đỏ: " + stats.totalRedCards);
        System.out.println("      🔄 Tổng số thay người: " + stats.totalSubstitutions);

        if (stats.topScorerName != null && stats.topScorerGoals > 0) {
            System.out.println("      👑 Vua phá lưới: " + stats.topScorerName + " (" + stats.topScorerGoals + " bàn)");
        } else {
            System.out.println("      👑 Vua phá lưới: Chưa có");
        }
        System.out.println();
    }

    /**
     * Tính tổng số trận đấu của tournament
     */
    private int calculateTotalMatches(int tournamentId) throws SQLException {
        String sql = """
            SELECT COUNT(*) as total_matches
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            WHERE ta.tournament_id = ? AND tb.tournament_id = ?
            AND (m.team_a_score > 0 OR m.team_b_score > 0 OR m.team_a_score = 0 AND m.team_b_score = 0)
        """;

        PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql);
        stmt.setInt(1, tournamentId);
        stmt.setInt(2, tournamentId);
        ResultSet rs = stmt.executeQuery();

        int total = 0;
        if (rs.next()) {
            total = rs.getInt("total_matches");
        }

        rs.close();
        stmt.close();
        return total;
    }

    /**
     * Tính tổng số bàn thắng của tournament từ bảng teams
     */
    private int calculateTotalGoals(int tournamentId) throws SQLException {
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
     * Tính tổng số thẻ vàng của tournament
     */
    private int calculateTotalYellowCards(int tournamentId) throws SQLException {
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
     * Tính tổng số thẻ đỏ của tournament
     */
    private int calculateTotalRedCards(int tournamentId) throws SQLException {
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
     * Tính tổng số thay người của tournament
     */
    private int calculateTotalSubstitutions(int tournamentId) throws SQLException {
        String sql = """
            SELECT COUNT(*) as total_substitutions
            FROM substitutions s
            JOIN matches m ON s.match_id = m.id
            JOIN teams t ON s.team_id = t.id
            WHERE t.tournament_id = ?
        """;

        PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql);
        stmt.setInt(1, tournamentId);
        ResultSet rs = stmt.executeQuery();

        int total = 0;
        if (rs.next()) {
            total = rs.getInt("total_substitutions");
        }

        rs.close();
        stmt.close();
        return total;
    }

    /**
     * Tìm vua phá lưới của tournament
     */
    private TopScorer findTopScorer(int tournamentId) throws SQLException {
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
     * Cập nhật hoặc tạo mới record trong tournament_stats
     */
    private void updateTournamentStats(int tournamentId, int totalMatches, int totalGoals,
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

    /**
     * Hiển thị kết quả tính toán
     */
    private void displayTournamentStats(String tournamentName, int totalMatches, int totalGoals,
                                      int totalYellowCards, int totalRedCards, int totalSubstitutions,
                                      TopScorer topScorer) {
        System.out.println("   📊 Kết quả tính toán:");
        System.out.println("      🏟️  Tổng số trận: " + totalMatches);
        System.out.println("      ⚽ Tổng số bàn thắng: " + totalGoals);
        System.out.println("      🟨 Tổng số thẻ vàng: " + totalYellowCards);
        System.out.println("      🟥 Tổng số thẻ đỏ: " + totalRedCards);
        System.out.println("      🔄 Tổng số thay người: " + totalSubstitutions);

        if (topScorer.playerId > 0) {
            System.out.println("      👑 Vua phá lưới: " + topScorer.playerName + " (" + topScorer.goals + " bàn)");
        } else {
            System.out.println("      👑 Vua phá lưới: Chưa có");
        }
        System.out.println();
    }

    /**
     * Hiển thị tất cả tournament stats
     */
    public void displayAllTournamentStats() {
        try {
            System.out.println("📊 THỐNG KÊ TẤT CẢ TOURNAMENTS");
            System.out.println("=" .repeat(100));

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
                System.out.printf("🏆 %-25s | %d | %-15s%n",
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
     * Class để lưu thông tin vua phá lưới
     */
    private static class TopScorer {
        int playerId = 0;
        String playerName = "";
        int goals = 0;
    }

    /**
     * Main method để test
     */
    public static void main(String[] args) {
        TournamentStatsCalculator calculator = new TournamentStatsCalculator();

        System.out.println("🌟 TOURNAMENT STATS CALCULATOR");
        System.out.println("=" .repeat(60));

        // Hiển thị stats trước khi tính toán
        System.out.println("📋 TRƯỚC KHI TÍNH TOÁN:");
        calculator.displayAllTournamentStats();

        System.out.println("\n" + "=" .repeat(60));

        // Tính toán lại tất cả stats
        calculator.recalculateAllTournamentStats();

        System.out.println("=" .repeat(60));

        // Hiển thị stats sau khi tính toán
        System.out.println("📋 SAU KHI TÍNH TOÁN:");
        calculator.displayAllTournamentStats();
    }
}