package com.worldcup.database;

import java.sql.*;
import java.util.*;

public class TournamentQueries {
    private DatabaseManager dbManager;

    public TournamentQueries(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // Get all teams with their statistics
    public List<Map<String, Object>> getAllTeamsWithStats() throws SQLException {
        String sql = """
            SELECT 
                t.id,
                t.name,
                t.region,
                t.coach,
                t.is_host,
                t.points,
                t.wins,
                t.draws,
                t.losses,
                t.goals_for,
                t.goals_against,
                t.goal_difference,
                g.name as group_name
            FROM teams t
            LEFT JOIN groups g ON t.group_id = g.id
            ORDER BY t.name
        """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        List<Map<String, Object>> teams = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> team = new HashMap<>();
            team.put("id", rs.getInt("id"));
            team.put("name", rs.getString("name"));
            team.put("region", rs.getString("region"));
            team.put("coach", rs.getString("coach"));
            team.put("isHost", rs.getBoolean("is_host"));
            team.put("points", rs.getInt("points"));
            team.put("wins", rs.getInt("wins"));
            team.put("draws", rs.getInt("draws"));
            team.put("losses", rs.getInt("losses"));
            team.put("goalsFor", rs.getInt("goals_for"));
            team.put("goalsAgainst", rs.getInt("goals_against"));
            team.put("goalDifference", rs.getInt("goal_difference"));
            team.put("groupName", rs.getString("group_name"));
            teams.add(team);
        }

        rs.close();
        pstmt.close();
        return teams;
    }

    // Get all matches with details
    public List<Map<String, Object>> getAllMatches() throws SQLException {
        String sql = """
            SELECT 
                m.id,
                ta.name as team_a_name,
                tb.name as team_b_name,
                m.team_a_score,
                m.team_b_score,
                m.match_type,
                m.match_date,
                m.venue,
                m.referee,
                m.status,
                winner.name as winner_name,
                g.name as group_name
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            LEFT JOIN teams winner ON m.winner_id = winner.id
            LEFT JOIN groups g ON m.group_id = g.id
            ORDER BY m.match_date, m.id
        """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        List<Map<String, Object>> matches = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> match = new HashMap<>();
            match.put("id", rs.getInt("id"));
            match.put("teamAName", rs.getString("team_a_name"));
            match.put("teamBName", rs.getString("team_b_name"));
            match.put("teamAScore", rs.getInt("team_a_score"));
            match.put("teamBScore", rs.getInt("team_b_score"));
            match.put("matchType", rs.getString("match_type"));
            match.put("matchDate", rs.getDate("match_date"));
            match.put("venue", rs.getString("venue"));
            match.put("referee", rs.getString("referee"));
            match.put("status", rs.getString("status"));
            match.put("winnerName", rs.getString("winner_name"));
            match.put("groupName", rs.getString("group_name"));
            matches.add(match);
        }

        rs.close();
        pstmt.close();
        return matches;
    }

    // Get top scorers
    public List<Map<String, Object>> getTopScorers(int limit) throws SQLException {
        String sql = """
            SELECT 
                p.name as player_name,
                t.name as team_name,
                p.position,
                COUNT(g.id) as goals
            FROM players p
            JOIN teams t ON p.team_id = t.id
            JOIN goals g ON p.id = g.player_id
            GROUP BY p.id, p.name, t.name, p.position
            ORDER BY goals DESC, p.name
            LIMIT ?
        """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, limit);
        ResultSet rs = pstmt.executeQuery();

        List<Map<String, Object>> scorers = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> scorer = new HashMap<>();
            scorer.put("playerName", rs.getString("player_name"));
            scorer.put("teamName", rs.getString("team_name"));
            scorer.put("position", rs.getString("position"));
            scorer.put("goals", rs.getInt("goals"));
            scorers.add(scorer);
        }

        rs.close();
        pstmt.close();
        return scorers;
    }

    // Get group standings
    public List<Map<String, Object>> getGroupStandings(String groupName) throws SQLException {
        String sql = """
            SELECT 
                t.name,
                t.points,
                t.wins,
                t.draws,
                t.losses,
                t.goals_for,
                t.goals_against,
                t.goal_difference
            FROM teams t
            JOIN groups g ON t.group_id = g.id
            WHERE g.name = ?
            ORDER BY t.points DESC, t.goal_difference DESC, t.goals_for DESC
        """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, groupName);
        ResultSet rs = pstmt.executeQuery();

        List<Map<String, Object>> standings = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> team = new HashMap<>();
            team.put("name", rs.getString("name"));
            team.put("points", rs.getInt("points"));
            team.put("wins", rs.getInt("wins"));
            team.put("draws", rs.getInt("draws"));
            team.put("losses", rs.getInt("losses"));
            team.put("goalsFor", rs.getInt("goals_for"));
            team.put("goalsAgainst", rs.getInt("goals_against"));
            team.put("goalDifference", rs.getInt("goal_difference"));
            standings.add(team);
        }

        rs.close();
        pstmt.close();
        return standings;
    }

    // Get match details with events
    public Map<String, Object> getMatchDetails(int matchId) throws SQLException {
        // Get basic match info
        String matchSql = """
            SELECT 
                m.id,
                ta.name as team_a_name,
                tb.name as team_b_name,
                m.team_a_score,
                m.team_b_score,
                m.match_type,
                m.match_date,
                m.venue,
                m.referee,
                m.status,
                winner.name as winner_name
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            LEFT JOIN teams winner ON m.winner_id = winner.id
            WHERE m.id = ?
        """;

        PreparedStatement matchPstmt = dbManager.getConnection().prepareStatement(matchSql);
        matchPstmt.setInt(1, matchId);
        ResultSet matchRs = matchPstmt.executeQuery();

        Map<String, Object> matchDetails = new HashMap<>();
        if (matchRs.next()) {
            matchDetails.put("id", matchRs.getInt("id"));
            matchDetails.put("teamAName", matchRs.getString("team_a_name"));
            matchDetails.put("teamBName", matchRs.getString("team_b_name"));
            matchDetails.put("teamAScore", matchRs.getInt("team_a_score"));
            matchDetails.put("teamBScore", matchRs.getInt("team_b_score"));
            matchDetails.put("matchType", matchRs.getString("match_type"));
            matchDetails.put("matchDate", matchRs.getDate("match_date"));
            matchDetails.put("venue", matchRs.getString("venue"));
            matchDetails.put("referee", matchRs.getString("referee"));
            matchDetails.put("status", matchRs.getString("status"));
            matchDetails.put("winnerName", matchRs.getString("winner_name"));
        }
        matchRs.close();
        matchPstmt.close();

        // Get goals
        String goalsSql = """
            SELECT 
                p.name as player_name,
                t.name as team_name,
                g.minute,
                g.goal_type
            FROM goals g
            JOIN players p ON g.player_id = p.id
            JOIN teams t ON g.team_id = t.id
            WHERE g.match_id = ?
            ORDER BY g.minute
        """;

        PreparedStatement goalsPstmt = dbManager.getConnection().prepareStatement(goalsSql);
        goalsPstmt.setInt(1, matchId);
        ResultSet goalsRs = goalsPstmt.executeQuery();

        List<Map<String, Object>> goals = new ArrayList<>();
        while (goalsRs.next()) {
            Map<String, Object> goal = new HashMap<>();
            goal.put("playerName", goalsRs.getString("player_name"));
            goal.put("teamName", goalsRs.getString("team_name"));
            goal.put("minute", goalsRs.getInt("minute"));
            goal.put("goalType", goalsRs.getString("goal_type"));
            goals.add(goal);
        }
        matchDetails.put("goals", goals);
        goalsRs.close();
        goalsPstmt.close();

        // Get cards
        String cardsSql = """
            SELECT 
                p.name as player_name,
                t.name as team_name,
                c.card_type,
                c.minute
            FROM cards c
            JOIN players p ON c.player_id = p.id
            JOIN teams t ON c.team_id = t.id
            WHERE c.match_id = ?
            ORDER BY c.minute
        """;

        PreparedStatement cardsPstmt = dbManager.getConnection().prepareStatement(cardsSql);
        cardsPstmt.setInt(1, matchId);
        ResultSet cardsRs = cardsPstmt.executeQuery();

        List<Map<String, Object>> cards = new ArrayList<>();
        while (cardsRs.next()) {
            Map<String, Object> card = new HashMap<>();
            card.put("playerName", cardsRs.getString("player_name"));
            card.put("teamName", cardsRs.getString("team_name"));
            card.put("cardType", cardsRs.getString("card_type"));
            card.put("minute", cardsRs.getInt("minute"));
            cards.add(card);
        }
        matchDetails.put("cards", cards);
        cardsRs.close();
        cardsPstmt.close();

        // Get substitutions
        String subsSql = """
            SELECT 
                pin.name as player_in_name,
                pout.name as player_out_name,
                t.name as team_name,
                s.minute
            FROM substitutions s
            JOIN players pin ON s.player_in_id = pin.id
            JOIN players pout ON s.player_out_id = pout.id
            JOIN teams t ON s.team_id = t.id
            WHERE s.match_id = ?
            ORDER BY s.minute
        """;

        PreparedStatement subsPstmt = dbManager.getConnection().prepareStatement(subsSql);
        subsPstmt.setInt(1, matchId);
        ResultSet subsRs = subsPstmt.executeQuery();

        List<Map<String, Object>> substitutions = new ArrayList<>();
        while (subsRs.next()) {
            Map<String, Object> sub = new HashMap<>();
            sub.put("playerInName", subsRs.getString("player_in_name"));
            sub.put("playerOutName", subsRs.getString("player_out_name"));
            sub.put("teamName", subsRs.getString("team_name"));
            sub.put("minute", subsRs.getInt("minute"));
            substitutions.add(sub);
        }
        matchDetails.put("substitutions", substitutions);
        subsRs.close();
        subsPstmt.close();

        return matchDetails;
    }

    // Get tournament summary
    public Map<String, Object> getTournamentSummary() throws SQLException {
        String sql = """
            SELECT 
                t.name as tournament_name,
                t.year,
                t.host_country,
                t.start_date,
                t.end_date,
                t.status,
                champion.name as champion_name,
                runner_up.name as runner_up_name,
                third_place.name as third_place_name,
                ts.total_goals,
                ts.total_matches,
                ts.total_yellow_cards,
                ts.total_red_cards,
                ts.total_substitutions,
                top_scorer.name as top_scorer_name,
                ts.top_scorer_goals
            FROM tournaments t
            LEFT JOIN teams champion ON t.champion_id = champion.id
            LEFT JOIN teams runner_up ON t.runner_up_id = runner_up.id
            LEFT JOIN teams third_place ON t.third_place_id = third_place.id
            LEFT JOIN tournament_stats ts ON t.id = ts.tournament_id
            LEFT JOIN players top_scorer ON ts.top_scorer_id = top_scorer.id
            ORDER BY t.id DESC
            LIMIT 1
        """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        Map<String, Object> summary = new HashMap<>();
        if (rs.next()) {
            summary.put("tournamentName", rs.getString("tournament_name"));
            summary.put("year", rs.getInt("year"));
            summary.put("hostCountry", rs.getString("host_country"));
            summary.put("startDate", rs.getDate("start_date"));
            summary.put("endDate", rs.getDate("end_date"));
            summary.put("status", rs.getString("status"));
            summary.put("championName", rs.getString("champion_name"));
            summary.put("runnerUpName", rs.getString("runner_up_name"));
            summary.put("thirdPlaceName", rs.getString("third_place_name"));
            summary.put("totalGoals", rs.getInt("total_goals"));
            summary.put("totalMatches", rs.getInt("total_matches"));
            summary.put("totalYellowCards", rs.getInt("total_yellow_cards"));
            summary.put("totalRedCards", rs.getInt("total_red_cards"));
            summary.put("totalSubstitutions", rs.getInt("total_substitutions"));
            summary.put("topScorerName", rs.getString("top_scorer_name"));
            summary.put("topScorerGoals", rs.getInt("top_scorer_goals"));
        }

        rs.close();
        pstmt.close();
        return summary;
    }

    // Get team roster
    public List<Map<String, Object>> getTeamRoster(String teamName) throws SQLException {
        String sql = """
            SELECT 
                p.name,
                p.jersey_number,
                p.position,
                p.is_starting,
                p.yellow_cards,
                p.red_cards,
                p.goals,
                p.assists,
                p.minutes_played
            FROM players p
            JOIN teams t ON p.team_id = t.id
            WHERE t.name = ?
            ORDER BY p.jersey_number
        """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        ResultSet rs = pstmt.executeQuery();

        List<Map<String, Object>> roster = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> player = new HashMap<>();
            player.put("name", rs.getString("name"));
            player.put("jerseyNumber", rs.getInt("jersey_number"));
            player.put("position", rs.getString("position"));
            player.put("isStarting", rs.getBoolean("is_starting"));
            player.put("yellowCards", rs.getInt("yellow_cards"));
            player.put("redCards", rs.getInt("red_cards"));
            player.put("goals", rs.getInt("goals"));
            player.put("assists", rs.getInt("assists"));
            player.put("minutesPlayed", rs.getInt("minutes_played"));
            roster.add(player);
        }

        rs.close();
        pstmt.close();
        return roster;
    }

    // Get knockout bracket
    public List<Map<String, Object>> getKnockoutMatches() throws SQLException {
        String sql = """
            SELECT 
                m.id,
                ta.name as team_a_name,
                tb.name as team_b_name,
                m.team_a_score,
                m.team_b_score,
                m.match_type,
                winner.name as winner_name,
                m.venue,
                m.match_date
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            LEFT JOIN teams winner ON m.winner_id = winner.id
            WHERE m.match_type IN ('ROUND_16', 'QUARTER', 'SEMI', 'FINAL', 'THIRD_PLACE')
            ORDER BY 
                CASE m.match_type 
                    WHEN 'ROUND_16' THEN 1
                    WHEN 'QUARTER' THEN 2
                    WHEN 'SEMI' THEN 3
                    WHEN 'THIRD_PLACE' THEN 4
                    WHEN 'FINAL' THEN 5
                END,
                m.id
        """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        List<Map<String, Object>> knockoutMatches = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> match = new HashMap<>();
            match.put("id", rs.getInt("id"));
            match.put("teamAName", rs.getString("team_a_name"));
            match.put("teamBName", rs.getString("team_b_name"));
            match.put("teamAScore", rs.getInt("team_a_score"));
            match.put("teamBScore", rs.getInt("team_b_score"));
            match.put("matchType", rs.getString("match_type"));
            match.put("winnerName", rs.getString("winner_name"));
            match.put("venue", rs.getString("venue"));
            match.put("matchDate", rs.getDate("match_date"));
            knockoutMatches.add(match);
        }

        rs.close();
        pstmt.close();
        return knockoutMatches;
    }
}