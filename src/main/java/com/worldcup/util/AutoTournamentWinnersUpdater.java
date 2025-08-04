package com.worldcup.util;

import com.worldcup.database.DatabaseManager;
import com.worldcup.service.TournamentService;

import java.sql.*;
import java.util.*;

/**
 * Utility class để tự động cập nhật tournament winners dựa trên kết quả matches
 */
public class AutoTournamentWinnersUpdater {
    private DatabaseManager dbManager;
    private TournamentService tournamentService;

    public AutoTournamentWinnersUpdater() {
        this.dbManager = new DatabaseManager();
        this.tournamentService = new TournamentService(dbManager);
    }

    public static void main(String[] args) {
        AutoTournamentWinnersUpdater updater = new AutoTournamentWinnersUpdater();
        
        try {
            if (args.length > 0) {
                int tournamentId = Integer.parseInt(args[0]);
                updater.updateWinnersForTournament(tournamentId);
            } else {
                updater.updateWinnersForAllCompletedTournaments();
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật winners cho tất cả tournaments đã hoàn thành
     */
    public void updateWinnersForAllCompletedTournaments() throws SQLException {
        System.out.println("🔄 Đang cập nhật winners cho tất cả tournaments đã hoàn thành...");

        String sql = """
            SELECT DISTINCT t.id, t.name, t.year
            FROM tournaments t
            WHERE EXISTS (
                SELECT 1 FROM matches m 
                WHERE m.team_a_id IN (SELECT id FROM teams WHERE tournament_id = t.id)
                AND m.match_type = 'FINAL' 
                AND m.status = 'COMPLETED'
            )
            ORDER BY t.year DESC
        """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        int updatedCount = 0;
        while (rs.next()) {
            int tournamentId = rs.getInt("id");
            String tournamentName = rs.getString("name");
            int year = rs.getInt("year");

            System.out.println("\n📊 Đang xử lý: " + tournamentName + " (" + year + ")");
            
            if (updateWinnersForTournament(tournamentId)) {
                updatedCount++;
            }
        }

        rs.close();
        pstmt.close();

        System.out.println("\n✅ Hoàn thành! Đã cập nhật " + updatedCount + " tournaments.");
    }

    /**
     * Cập nhật winners cho một tournament cụ thể
     */
    public boolean updateWinnersForTournament(int tournamentId) throws SQLException {
        System.out.println("🏆 Đang phân tích kết quả tournament ID: " + tournamentId);

        // Tìm champion (đội thắng trận FINAL)
        Integer championId = findMatchWinner(tournamentId, "FINAL");
        
        // Tìm runner-up (đội thua trận FINAL)
        Integer runnerUpId = findMatchLoser(tournamentId, "FINAL");
        
        // Tìm third place (2 đội thua bán kết đồng hạng 3 theo quy định FIFA mới)
        List<Integer> thirdPlaceIds = findSemiFinalLosers(tournamentId);
        Integer thirdPlaceId01 = thirdPlaceIds.size() >= 1 ? thirdPlaceIds.get(0) : null;
        Integer thirdPlaceId02 = thirdPlaceIds.size() >= 2 ? thirdPlaceIds.get(1) : null;

        if (championId == null && runnerUpId == null && thirdPlaceId01 == null) {
            System.out.println("⚠️ Không tìm thấy kết quả chung kết hoặc bán kết cho tournament này");
            return false;
        }

        // Hiển thị kết quả tìm được
        System.out.println("📋 Kết quả phân tích:");
        if (championId != null) {
            System.out.println("   🥇 Champion: " + getTeamName(championId));
        }
        if (runnerUpId != null) {
            System.out.println("   🥈 Runner-up: " + getTeamName(runnerUpId));
        }
        if (thirdPlaceId01 != null && thirdPlaceId02 != null) {
            System.out.println("   🥉 Third place (đồng hạng): " + getTeamName(thirdPlaceId01) + " & " + getTeamName(thirdPlaceId02));
        } else if (thirdPlaceId01 != null) {
            System.out.println("   🥉 Third place: " + getTeamName(thirdPlaceId01));
        }

        // Cập nhật vào database với 2 đội đồng hạng 3
        tournamentService.updateTournamentWinners(tournamentId, championId, runnerUpId, thirdPlaceId01, thirdPlaceId02);
        
        return true;
    }

    /**
     * Tìm đội thắng trong một loại trận đấu cụ thể
     */
    private Integer findMatchWinner(int tournamentId, String matchType) throws SQLException {
        String sql = """
            SELECT 
                m.team_a_id, m.team_b_id, m.team_a_score, m.team_b_score,
                ta.name as team_a_name, tb.name as team_b_name
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            WHERE ta.tournament_id = ? AND tb.tournament_id = ?
            AND m.match_type = ? AND m.status = 'COMPLETED'
            ORDER BY m.id DESC
            LIMIT 1
        """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        pstmt.setInt(2, tournamentId);
        pstmt.setString(3, matchType);
        ResultSet rs = pstmt.executeQuery();

        Integer winnerId = null;
        if (rs.next()) {
            int teamAScore = rs.getInt("team_a_score");
            int teamBScore = rs.getInt("team_b_score");
            
            if (teamAScore > teamBScore) {
                winnerId = rs.getInt("team_a_id");
                System.out.println("   " + matchType + ": " + rs.getString("team_a_name") + " " + 
                                 teamAScore + "-" + teamBScore + " " + rs.getString("team_b_name"));
            } else if (teamBScore > teamAScore) {
                winnerId = rs.getInt("team_b_id");
                System.out.println("   " + matchType + ": " + rs.getString("team_a_name") + " " + 
                                 teamAScore + "-" + teamBScore + " " + rs.getString("team_b_name"));
            } else {
                System.out.println("   " + matchType + ": Trận hòa - không xác định được người thắng");
            }
        }

        rs.close();
        pstmt.close();
        return winnerId;
    }

    /**
     * Tìm đội thua trong một loại trận đấu cụ thể
     */
    private Integer findMatchLoser(int tournamentId, String matchType) throws SQLException {
        String sql = """
            SELECT 
                m.team_a_id, m.team_b_id, m.team_a_score, m.team_b_score
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            WHERE ta.tournament_id = ? AND tb.tournament_id = ?
            AND m.match_type = ? AND m.status = 'COMPLETED'
            ORDER BY m.id DESC
            LIMIT 1
        """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        pstmt.setInt(2, tournamentId);
        pstmt.setString(3, matchType);
        ResultSet rs = pstmt.executeQuery();

        Integer loserId = null;
        if (rs.next()) {
            int teamAScore = rs.getInt("team_a_score");
            int teamBScore = rs.getInt("team_b_score");
            
            if (teamAScore > teamBScore) {
                loserId = rs.getInt("team_b_id");
            } else if (teamBScore > teamAScore) {
                loserId = rs.getInt("team_a_id");
            }
        }

        rs.close();
        pstmt.close();
        return loserId;
    }

    /**
     * Tìm 2 đội thua bán kết (đồng hạng 3 theo quy định FIFA mới)
     */
    private List<Integer> findSemiFinalLosers(int tournamentId) throws SQLException {
        String sql = """
            SELECT 
                m.team_a_id, m.team_b_id, m.team_a_score, m.team_b_score
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            WHERE ta.tournament_id = ? AND tb.tournament_id = ?
            AND m.match_type = 'SEMI' AND m.status = 'COMPLETED'
            ORDER BY m.id
        """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();

        List<Integer> losers = new ArrayList<>();
        while (rs.next()) {
            int teamAScore = rs.getInt("team_a_score");
            int teamBScore = rs.getInt("team_b_score");
            
            if (teamAScore > teamBScore) {
                losers.add(rs.getInt("team_b_id")); // Team B thua
            } else if (teamBScore > teamAScore) {
                losers.add(rs.getInt("team_a_id")); // Team A thua
            }
        }

        rs.close();
        pstmt.close();
        return losers;
    }

    /**
     * Lấy tên team theo ID
     */
    private String getTeamName(int teamId) throws SQLException {
        String sql = "SELECT name FROM teams WHERE id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, teamId);
        ResultSet rs = pstmt.executeQuery();

        String name = "Unknown";
        if (rs.next()) {
            name = rs.getString("name");
        }

        rs.close();
        pstmt.close();
        return name;
    }

    /**
     * Hiển thị tournament winners hiện tại
     */
    public void displayCurrentWinners(int tournamentId) throws SQLException {
        Map<String, Object> winners = tournamentService.getTournamentWinners(tournamentId);
        
        System.out.println("\n🏆 Tournament Winners hiện tại (Tournament ID: " + tournamentId + "):");
        System.out.println("   Champion: " + winners.get("champion_name"));
        System.out.println("   Runner-up: " + winners.get("runner_up_name"));
        System.out.println("   Third place: " + winners.get("third_place_name"));
    }
}