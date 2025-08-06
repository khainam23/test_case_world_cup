package com.worldcup.repository.impl;

import com.worldcup.database.DatabaseManager;
import com.worldcup.model.Player;
import com.worldcup.model.Substitution;
import com.worldcup.model.Team;
import com.worldcup.repository.SubstitutionRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation của SubstitutionRepository
 * Xử lý tất cả database operations cho Substitution entity
 */
public class SubstitutionRepositoryImpl implements SubstitutionRepository {

    private final DatabaseManager dbManager;

    public SubstitutionRepositoryImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void save(Substitution substitution) throws SQLException {
        // Validate players exist in the team first
        validatePlayersExist(substitution);

        String sql = """
                    INSERT INTO substitutions (match_id, team_id, player_in_id, player_out_id, minute)
                    VALUES (?, 
                            ?, 
                            ?,
                            ?, 
                            ?)
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, substitution.getMatch().getId());
        pstmt.setInt(2, substitution.getTeam().getId());
        pstmt.setInt(3, substitution.getInPlayer().getId());
        pstmt.setInt(4, substitution.getOutPlayer().getId());
        pstmt.setInt(5, substitution.getMinute());
        pstmt.executeUpdate();
        pstmt.close();

        int substitutionId = dbManager.getLastInsertId();
        substitution.setId(substitutionId);
    }

    /**
     * Validate that both players exist in the team before creating substitution
     */
    private void validatePlayersExist(Substitution substitution) throws SQLException {
        Player playerIn = substitution.getInPlayer();
        Player playerOut = substitution.getOutPlayer();

        boolean playerOutExists = substitution.getTeam().getStartingPlayers().contains(playerOut);
        boolean playerInExists = substitution.getTeam().getSubstitutePlayers().contains(playerIn);
        if (!playerInExists) {
            throw new SQLException("Cầu thủ vào sân không thuộc đội: " + playerIn.getName());
        }

        if (!playerOutExists) {
            throw new SQLException("Cầu thủ bị thay ra không thuộc trận đấu: " + playerOut.getName());
        }
    }

    @Override
    public void update(Substitution substitution) throws SQLException {
        String sql = """
                    UPDATE substitutions 
                    SET match_id = ?, team_id = ?, player_in_id = ?, player_out_id = ?, minute = ?
                    WHERE id = ?
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, substitution.getMatch().getId());
        pstmt.setInt(2, substitution.getTeam().getId());
        pstmt.setInt(3, substitution.getInPlayer().getId());
        pstmt.setInt(4, substitution.getOutPlayer().getId());
        pstmt.setInt(5, substitution.getMinute());
        pstmt.setInt(6, substitution.getId());
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public Optional<Substitution> findById(int id) throws SQLException {
        String sql = """
                    SELECT s.id, s.match_id, s.team_id, s.player_in_id, s.player_out_id, s.minute,
                           t.name as team_name, pin.name as player_in_name, pout.name as player_out_name
                    FROM substitutions s
                    JOIN teams t ON s.team_id = t.id
                    JOIN players pin ON s.player_in_id = pin.id
                    JOIN players pout ON s.player_out_id = pout.id
                    WHERE s.id = ?
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();

        Optional<Substitution> result = Optional.empty();
        if (rs.next()) {
            // TODO: Create Substitution object with full relationships
            result = Optional.empty(); // Simplified for now
        }

        rs.close();
        pstmt.close();
        return result;
    }

    @Override
    public List<Substitution> findByMatch(int matchId) throws SQLException {
        List<Substitution> substitutions = new ArrayList<>();

        String sql = """
                    SELECT s.id, s.match_id, s.team_id, s.player_in_id, s.player_out_id, s.minute,
                           t.name as team_name, pin.name as player_in_name, pout.name as player_out_name
                    FROM substitutions s
                    JOIN teams t ON s.team_id = t.id
                    JOIN players pin ON s.player_in_id = pin.id
                    JOIN players pout ON s.player_out_id = pout.id
                    WHERE s.match_id = ?
                    ORDER BY s.minute ASC
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, matchId);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            // TODO: Create Substitution objects
        }

        rs.close();
        pstmt.close();
        return substitutions;
    }

    @Override
    public List<Substitution> findByTeamAndTournament(String teamName, int tournamentId) throws SQLException {
        List<Substitution> substitutions = new ArrayList<>();

        String sql = """
                    SELECT s.id, s.match_id, s.team_id, s.player_in_id, s.player_out_id, s.minute
                    FROM substitutions s
                    JOIN teams t ON s.team_id = t.id
                    WHERE t.name = ? AND t.tournament_id = ?
                    ORDER BY s.match_id ASC, s.minute ASC
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            // TODO: Create Substitution objects
        }

        rs.close();
        pstmt.close();
        return substitutions;
    }

    @Override
    public int countSubstitutionsByTeam(String teamName, int tournamentId) throws SQLException {
        String sql = """
                    SELECT COUNT(*) as substitution_count
                    FROM substitutions s
                    JOIN teams t ON s.team_id = t.id
                    WHERE t.name = ? AND t.tournament_id = ?
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();

        int count = 0;
        if (rs.next()) {
            count = rs.getInt("substitution_count");
        }

        rs.close();
        pstmt.close();
        return count;
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM substitutions WHERE id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public boolean exists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM substitutions WHERE id = ?";
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