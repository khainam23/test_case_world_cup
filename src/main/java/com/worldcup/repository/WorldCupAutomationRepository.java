package com.worldcup.repository;

import com.worldcup.database.DatabaseManager;
import com.worldcup.model.Match;
import com.worldcup.model.Tournament;
import com.worldcup.service.TournamentService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Repository class để xử lý các câu SQL cho WorldCupAutomation
 * Tách biệt logic SQL khỏi business logic
 */
public class WorldCupAutomationRepository {
    private final DatabaseManager dbManager;

    public WorldCupAutomationRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Lưu tournament vào database
     */
    public int saveTournament(Tournament tournament) throws SQLException {
        String sql = """
                INSERT INTO tournaments (name, year, host_country, start_date, end_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, tournament.getName());
        pstmt.setInt(2, tournament.getYear());
        pstmt.setString(3, tournament.getHost().getName());
        pstmt.setString(4, tournament.getStart() + ""); // start_date dạng YYYY/MM/DD
        pstmt.setString(5, tournament.getEnd() + ""); // end_date dạng YYYY/MM/DD
        pstmt.executeUpdate();
        pstmt.close();
        
        return dbManager.getLastInsertId();
    }

    /**
     * Lưu group vào database
     */
    public int saveGroup(String groupName, int tournamentId) throws SQLException {
        String sql = "INSERT INTO groups (name, tournament_id) VALUES (?, ?)";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, groupName);
        pstmt.setInt(2, tournamentId);
        pstmt.executeUpdate();
        pstmt.close();
        
        return dbManager.getLastInsertId();
    }

    /**
     * Cập nhật group_id cho team
     */
    public void updateTeamGroup(int teamId, int groupId) throws SQLException {
        String sql = "UPDATE teams SET group_id = ? WHERE id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, groupId);
        pstmt.setInt(2, teamId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    /**
     * Lưu Match vào database
     */
    public int saveMatch(Match match, int tournamentId) throws SQLException {
        String matchSql = """
                INSERT INTO matches (team_a_id, team_b_id, team_a_score, team_b_score, match_type, 
                                   match_date, venue, referee)
                VALUES ((SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                        (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                        ?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(matchSql);
        pstmt.setString(1, match.getTeamA().getName());
        pstmt.setInt(2, tournamentId);
        pstmt.setString(3, match.getTeamB().getName());
        pstmt.setInt(4, tournamentId);
        pstmt.setInt(5, match.getTeamAScore());
        pstmt.setInt(6, match.getTeamBScore());
        pstmt.setString(7, match.getMatchType());
        pstmt.setString(8, match.getMatchDate());
        pstmt.setString(9, match.getVenue());
        pstmt.setString(10, match.getReferee());
        pstmt.executeUpdate();
        pstmt.close();

        return dbManager.getLastInsertId();
    }

    /**
     * Lấy team ID từ tên team
     */
    public Integer getTeamIdByName(String teamName, int tournamentId) throws SQLException {
        String sql = "SELECT id FROM teams WHERE name = ? AND tournament_id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();

        Integer teamId = null;
        if (rs.next()) {
            teamId = rs.getInt("id");
        }

        rs.close();
        pstmt.close();
        return teamId;
    }

    /**
     * Kiểm tra xem tournament_stats record có tồn tại không
     */
    public boolean tournamentStatsExists(int tournamentId) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM tournament_stats WHERE tournament_id = ?";
        PreparedStatement checkPstmt = dbManager.getConnection().prepareStatement(checkSql);
        checkPstmt.setInt(1, tournamentId);
        ResultSet rs = checkPstmt.executeQuery();

        boolean exists = rs.next() && rs.getInt(1) > 0;
        rs.close();
        checkPstmt.close();
        
        return exists;
    }

    /**
     * Tạo tournament_stats record mới
     */
    public void createTournamentStatsRecord(int tournamentId) throws SQLException {
        String insertSql = """
                INSERT INTO tournament_stats (tournament_id, total_goals, total_matches, 
                                            total_yellow_cards, total_red_cards, total_substitutions, 
                                            top_scorer_id, top_scorer_goals)
                VALUES (?, 0, 0, 0, 0, 0, NULL, 0)
                """;

        PreparedStatement insertPstmt = dbManager.getConnection().prepareStatement(insertSql);
        insertPstmt.setInt(1, tournamentId);
        insertPstmt.executeUpdate();
        insertPstmt.close();
    }

    /**
     * Lưu thống kê tournament vào database
     */
    public void saveTournamentStats(int tournamentId, TournamentService.TournamentStats stats) throws SQLException {
        String insertStatsSql = """
                INSERT INTO tournament_stats (tournament_id, total_goals, total_matches, 
                                            total_yellow_cards, total_red_cards, total_substitutions,
                                            top_scorer_id, top_scorer_goals)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement insertStatsPstmt = dbManager.getConnection().prepareStatement(insertStatsSql);
        insertStatsPstmt.setInt(1, tournamentId);
        insertStatsPstmt.setInt(2, stats.totalGoals);
        insertStatsPstmt.setInt(3, stats.totalMatches);
        insertStatsPstmt.setInt(4, stats.totalYellowCards);
        insertStatsPstmt.setInt(5, stats.totalRedCards);
        insertStatsPstmt.setInt(6, stats.totalSubstitutions);
        if (stats.topScorerId > 0) {
            insertStatsPstmt.setInt(7, stats.topScorerId);
        } else {
            insertStatsPstmt.setNull(7, java.sql.Types.INTEGER);
        }
        insertStatsPstmt.setInt(8, stats.topScorerGoals);
        insertStatsPstmt.executeUpdate();
        insertStatsPstmt.close();
    }

    /**
     * Cập nhật thống kê tournament trong database
     */
    public void updateTournamentStats(int tournamentId, TournamentService.TournamentStats stats) throws SQLException {
        String updateStatsSql = """
                UPDATE tournament_stats SET 
                    total_goals = ?, total_matches = ?, 
                    total_yellow_cards = ?, total_red_cards = ?, 
                    total_substitutions = ?, top_scorer_id = ?, top_scorer_goals = ?
                WHERE tournament_id = ?
                """;

        PreparedStatement updateStatsPstmt = dbManager.getConnection().prepareStatement(updateStatsSql);
        updateStatsPstmt.setInt(1, stats.totalGoals);
        updateStatsPstmt.setInt(2, stats.totalMatches);
        updateStatsPstmt.setInt(3, stats.totalYellowCards);
        updateStatsPstmt.setInt(4, stats.totalRedCards);
        updateStatsPstmt.setInt(5, stats.totalSubstitutions);
        if (stats.topScorerId > 0) {
            updateStatsPstmt.setInt(6, stats.topScorerId);
        } else {
            updateStatsPstmt.setNull(6, java.sql.Types.INTEGER);
        }
        updateStatsPstmt.setInt(7, stats.topScorerGoals);
        updateStatsPstmt.setInt(8, tournamentId);
        updateStatsPstmt.executeUpdate();
        updateStatsPstmt.close();
    }

    /**
     * Lấy tên tournament theo ID
     */
    public String getTournamentName(int tournamentId) throws SQLException {
        String selectSql = "SELECT name FROM tournaments WHERE id = ?";
        PreparedStatement selectStmt = dbManager.getConnection().prepareStatement(selectSql);
        selectStmt.setInt(1, tournamentId);
        ResultSet rs = selectStmt.executeQuery();

        String tournamentName = null;
        if (rs.next()) {
            tournamentName = rs.getString("name");
        }

        rs.close();
        selectStmt.close();
        return tournamentName;
    }
}