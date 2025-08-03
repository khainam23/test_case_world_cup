package com.worldcup.automation;

import com.worldcup.*;
import com.worldcup.database.DatabaseManager;
import com.worldcup.generator.DataGenerator;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class WorldCupAutomation {
    private DatabaseManager dbManager;
    private List<Team> teams;
    private List<Group> groups;
    private int currentTournamentId;
    private Random random = new Random();

    public WorldCupAutomation() {
        this.dbManager = new DatabaseManager();
        this.teams = new ArrayList<>();
        this.groups = new ArrayList<>();
    }

    public void runCompleteWorldCup() {
        try {
            System.out.println("🏆 Starting FIFA World Cup Automation...");
            
            // Step 1: Clear previous data and setup
            clearPreviousData();
            
            // Step 2: Create tournament
            createTournament();
            
            // Step 3: Generate teams
            generateTeams();
            
            // Step 4: Create groups and assign teams
            createGroupsAndAssignTeams();
            
            // Step 5: Run group stage
            runGroupStage();
            
            // Step 6: Determine group winners and runners-up
            List<Team> qualifiedTeams = determineQualifiedTeams();
            
            // Step 7: Run knockout stage
            runKnockoutStage(qualifiedTeams);
            
            // Step 8: Generate final statistics
            generateTournamentStatistics();
            
            // Step 9: Display results
            displayFinalResults();
            
            System.out.println("🎉 World Cup completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error during World Cup automation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearPreviousData() throws SQLException {
        System.out.println("🧹 Clearing previous tournament data...");
        dbManager.clearAllData();
    }

    private void createTournament() throws SQLException {
        System.out.println("🏟️ Creating new tournament...");
        
        int year = 2024;
        String name = DataGenerator.generateTournamentName(year);
        String hostCountry = "Qatar"; // Can be randomized
        
        java.sql.Date startDate = new java.sql.Date(System.currentTimeMillis());
        java.sql.Date endDate = new java.sql.Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)); // 30 days later
        
        String sql = """
            INSERT INTO tournaments (name, year, host_country, start_date, end_date, status)
            VALUES (?, ?, ?, ?, ?, 'GROUP_STAGE')
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, name);
        pstmt.setInt(2, year);
        pstmt.setString(3, hostCountry);
        pstmt.setDate(4, startDate);
        pstmt.setDate(5, endDate);
        pstmt.executeUpdate();
        pstmt.close();
        
        currentTournamentId = dbManager.getLastInsertId();
        System.out.println("✅ Tournament created: " + name);
    }

    private void generateTeams() throws SQLException {
        System.out.println("🌍 Generating 32 teams...");
        
        teams = DataGenerator.generateRandomTeams(32);
        
        for (Team team : teams) {
            insertTeamToDatabase(team);
        }
        
        System.out.println("✅ Generated " + teams.size() + " teams");
    }

    private void insertTeamToDatabase(Team team) throws SQLException {
        // Insert team
        String teamSql = """
            INSERT INTO teams (name, region, coach, medical_staff, is_host)
            VALUES (?, ?, ?, ?, ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(teamSql);
        pstmt.setString(1, team.getName());
        pstmt.setString(2, team.getRegion());
        pstmt.setString(3, team.getCoach());
        pstmt.setString(4, team.getMedicalStaff());
        pstmt.setBoolean(5, team.isHost());
        pstmt.executeUpdate();
        pstmt.close();
        
        int teamId = dbManager.getLastInsertId();
        
        // Insert assistant coaches
        for (String assistant : team.getAssistantCoaches()) {
            String assistantSql = "INSERT INTO assistant_coaches (name, team_id) VALUES (?, ?)";
            PreparedStatement assistantPstmt = dbManager.getConnection().prepareStatement(assistantSql);
            assistantPstmt.setString(1, assistant);
            assistantPstmt.setInt(2, teamId);
            assistantPstmt.executeUpdate();
            assistantPstmt.close();
        }
        
        // Insert starting players
        for (Player player : team.getStartingPlayers()) {
            insertPlayerToDatabase(player, teamId, true);
        }
        
        // Insert substitute players
        for (Player player : team.getSubstitutePlayers()) {
            insertPlayerToDatabase(player, teamId, false);
        }
    }

    private void insertPlayerToDatabase(Player player, int teamId, boolean isStarting) throws SQLException {
        String sql = """
            INSERT INTO players (name, jersey_number, position, team_id, is_starting, yellow_cards, red_cards, is_eligible)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, player.getName());
        pstmt.setInt(2, player.getJerseyNumber());
        pstmt.setString(3, player.getPosition());
        pstmt.setInt(4, teamId);
        pstmt.setBoolean(5, isStarting);
        pstmt.setInt(6, player.getYellowCards());
        pstmt.setInt(7, player.getRedCards());
        pstmt.setBoolean(8, player.isEligible());
        pstmt.executeUpdate();
        pstmt.close();
    }

    private void createGroupsAndAssignTeams() throws SQLException {
        System.out.println("🔤 Creating groups and assigning teams...");
        
        // Create 8 groups (A-H)
        String[] groupNames = {"A", "B", "C", "D", "E", "F", "G", "H"};
        
        for (String groupName : groupNames) {
            String sql = "INSERT INTO groups (name) VALUES (?)";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, groupName);
            pstmt.executeUpdate();
            pstmt.close();
            
            int groupId = dbManager.getLastInsertId();
            Group group = new Group(groupName);
            groups.add(group);
        }
        
        // Assign teams to groups (4 teams per group)
        Collections.shuffle(teams); // Randomize team assignment
        
        for (int i = 0; i < teams.size(); i++) {
            int groupIndex = i / 4; // 4 teams per group
            int groupId = groupIndex + 1; // Group IDs start from 1
            
            // Update team with group assignment
            String sql = "UPDATE teams SET group_id = ? WHERE name = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setString(2, teams.get(i).getName());
            pstmt.executeUpdate();
            pstmt.close();
            
            // Add team to group object
            groups.get(groupIndex).addTeam(teams.get(i));
        }
        
        System.out.println("✅ Created 8 groups with 4 teams each");
    }

    private void runGroupStage() throws SQLException {
        System.out.println("⚽ Running Group Stage matches...");
        
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            Group group = groups.get(groupIndex);
            List<Team> groupTeams = group.getTeams();
            
            System.out.println("🔤 Group " + group.getName() + " matches:");
            
            // Generate all possible matches in the group (6 matches total)
            for (int i = 0; i < groupTeams.size(); i++) {
                for (int j = i + 1; j < groupTeams.size(); j++) {
                    Team teamA = groupTeams.get(i);
                    Team teamB = groupTeams.get(j);
                    
                    simulateMatch(teamA, teamB, "GROUP", groupIndex + 1);
                }
            }
        }
        
        System.out.println("✅ Group Stage completed");
    }

    private void simulateMatch(Team teamA, Team teamB, String matchType, int groupId) throws SQLException {
        // Generate match result
        int[] score = DataGenerator.generateMatchScore();
        int teamAScore = score[0];
        int teamBScore = score[1];
        
        String venue = DataGenerator.getRandomVenue();
        String referee = DataGenerator.getRandomReferee();
        java.sql.Date matchDate = new java.sql.Date(System.currentTimeMillis() + random.nextInt(1000000000));
        
        // Insert match
        String matchSql = """
            INSERT INTO matches (team_a_id, team_b_id, team_a_score, team_b_score, match_type, 
                               match_date, venue, referee, status, group_id)
            VALUES ((SELECT id FROM teams WHERE name = ?), 
                    (SELECT id FROM teams WHERE name = ?), 
                    ?, ?, ?, ?, ?, ?, 'COMPLETED', ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(matchSql);
        pstmt.setString(1, teamA.getName());
        pstmt.setString(2, teamB.getName());
        pstmt.setInt(3, teamAScore);
        pstmt.setInt(4, teamBScore);
        pstmt.setString(5, matchType);
        pstmt.setDate(6, matchDate);
        pstmt.setString(7, venue);
        pstmt.setString(8, referee);
        pstmt.setInt(9, groupId);
        pstmt.executeUpdate();
        pstmt.close();
        
        int matchId = dbManager.getLastInsertId();
        
        // Update team statistics
        updateTeamStatistics(teamA, teamB, teamAScore, teamBScore);
        
        // Generate match events (goals, cards, substitutions)
        generateMatchEvents(matchId, teamA, teamB, teamAScore, teamBScore);
        
        System.out.println("  " + teamA.getName() + " " + teamAScore + " - " + teamBScore + " " + teamB.getName());
    }

    private void updateTeamStatistics(Team teamA, Team teamB, int teamAScore, int teamBScore) throws SQLException {
        // Update team A
        updateSingleTeamStats(teamA.getName(), teamAScore, teamBScore);
        
        // Update team B
        updateSingleTeamStats(teamB.getName(), teamBScore, teamAScore);
    }

    private void updateSingleTeamStats(String teamName, int goalsFor, int goalsAgainst) throws SQLException {
        int points = 0;
        int wins = 0, draws = 0, losses = 0;
        
        if (goalsFor > goalsAgainst) {
            points = 3;
            wins = 1;
        } else if (goalsFor == goalsAgainst) {
            points = 1;
            draws = 1;
        } else {
            losses = 1;
        }
        
        String sql = """
            UPDATE teams SET 
                points = points + ?,
                goals_for = goals_for + ?,
                goals_against = goals_against + ?,
                goal_difference = goals_for - goals_against,
                wins = wins + ?,
                draws = draws + ?,
                losses = losses + ?
            WHERE name = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, points);
        pstmt.setInt(2, goalsFor);
        pstmt.setInt(3, goalsAgainst);
        pstmt.setInt(4, wins);
        pstmt.setInt(5, draws);
        pstmt.setInt(6, losses);
        pstmt.setString(7, teamName);
        pstmt.executeUpdate();
        pstmt.close();
    }

    private void generateMatchEvents(int matchId, Team teamA, Team teamB, int teamAScore, int teamBScore) throws SQLException {
        // Generate goals for team A
        generateGoalsForTeam(matchId, teamA, teamAScore);
        
        // Generate goals for team B
        generateGoalsForTeam(matchId, teamB, teamBScore);
        
        // Generate cards and substitutions
        generateCardsAndSubstitutions(matchId, teamA, teamB);
    }

    private void generateGoalsForTeam(int matchId, Team team, int goalCount) throws SQLException {
        for (int i = 0; i < goalCount; i++) {
            // Select random player from starting lineup
            List<Player> startingPlayers = team.getStartingPlayers();
            if (!startingPlayers.isEmpty()) {
                Player scorer = DataGenerator.getRandomElement(startingPlayers);
                int minute = DataGenerator.generateRandomMinute();
                
                String sql = """
                    INSERT INTO goals (match_id, player_id, team_id, minute, goal_type)
                    VALUES (?, (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ?)), 
                            (SELECT id FROM teams WHERE name = ?), ?, 'REGULAR')
                """;
                
                PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
                pstmt.setInt(1, matchId);
                pstmt.setString(2, scorer.getName());
                pstmt.setString(3, team.getName());
                pstmt.setString(4, team.getName());
                pstmt.setInt(5, minute);
                pstmt.executeUpdate();
                pstmt.close();
            }
        }
    }

    private void generateCardsAndSubstitutions(int matchId, Team teamA, Team teamB) throws SQLException {
        // Generate cards for both teams
        generateCardsForTeam(matchId, teamA);
        generateCardsForTeam(matchId, teamB);
        
        // Generate substitutions for both teams
        generateSubstitutionsForTeam(matchId, teamA);
        generateSubstitutionsForTeam(matchId, teamB);
    }

    private void generateCardsForTeam(int matchId, Team team) throws SQLException {
        List<Player> allPlayers = new ArrayList<>(team.getStartingPlayers());
        
        // Yellow cards
        if (DataGenerator.shouldHaveYellowCard()) {
            Player player = DataGenerator.getRandomElement(allPlayers);
            if (player != null) {
                int minute = DataGenerator.generateRandomMinute();
                insertCard(matchId, team, player, "YELLOW", minute);
            }
        }
        
        // Red cards (less common)
        if (DataGenerator.shouldHaveRedCard()) {
            Player player = DataGenerator.getRandomElement(allPlayers);
            if (player != null) {
                int minute = DataGenerator.generateRandomMinute();
                insertCard(matchId, team, player, "RED", minute);
            }
        }
    }

    private void insertCard(int matchId, Team team, Player player, String cardType, int minute) throws SQLException {
        String sql = """
            INSERT INTO cards (match_id, player_id, team_id, card_type, minute)
            VALUES (?, (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ?)), 
                    (SELECT id FROM teams WHERE name = ?), ?, ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, matchId);
        pstmt.setString(2, player.getName());
        pstmt.setString(3, team.getName());
        pstmt.setString(4, team.getName());
        pstmt.setString(5, cardType);
        pstmt.setInt(6, minute);
        pstmt.executeUpdate();
        pstmt.close();
    }

    private void generateSubstitutionsForTeam(int matchId, Team team) throws SQLException {
        if (DataGenerator.shouldHaveSubstitution()) {
            int substitutionCount = random.nextInt(3) + 1; // 1-3 substitutions
            
            for (int i = 0; i < substitutionCount && i < team.getSubstitutePlayers().size(); i++) {
                Player playerOut = DataGenerator.getRandomElement(team.getStartingPlayers());
                Player playerIn = DataGenerator.getRandomElement(team.getSubstitutePlayers());
                
                if (playerOut != null && playerIn != null) {
                    int minute = DataGenerator.generateSubstitutionMinute();
                    
                    String sql = """
                        INSERT INTO substitutions (match_id, team_id, player_in_id, player_out_id, minute)
                        VALUES (?, (SELECT id FROM teams WHERE name = ?), 
                                (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ?)),
                                (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ?)), ?)
                    """;
                    
                    PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
                    pstmt.setInt(1, matchId);
                    pstmt.setString(2, team.getName());
                    pstmt.setString(3, playerIn.getName());
                    pstmt.setString(4, team.getName());
                    pstmt.setString(5, playerOut.getName());
                    pstmt.setString(6, team.getName());
                    pstmt.setInt(7, minute);
                    pstmt.executeUpdate();
                    pstmt.close();
                }
            }
        }
    }

    private List<Team> determineQualifiedTeams() throws SQLException {
        System.out.println("🏅 Determining qualified teams from each group...");
        
        List<Team> qualifiedTeams = new ArrayList<>();
        
        for (int groupId = 1; groupId <= 8; groupId++) {
            String sql = """
                SELECT name FROM teams 
                WHERE group_id = ? 
                ORDER BY points DESC, goal_difference DESC, goals_for DESC
                LIMIT 2
            """;
            
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String teamName = rs.getString("name");
                Team team = teams.stream()
                    .filter(t -> t.getName().equals(teamName))
                    .findFirst()
                    .orElse(null);
                
                if (team != null) {
                    qualifiedTeams.add(team);
                }
            }
            
            rs.close();
            pstmt.close();
        }
        
        System.out.println("✅ " + qualifiedTeams.size() + " teams qualified for knockout stage");
        return qualifiedTeams;
    }

    private void runKnockoutStage(List<Team> qualifiedTeams) throws SQLException {
        System.out.println("🏆 Running Knockout Stage...");
        
        // Update tournament status
        String updateSql = "UPDATE tournaments SET status = 'KNOCKOUT' WHERE id = ?";
        PreparedStatement updatePstmt = dbManager.getConnection().prepareStatement(updateSql);
        updatePstmt.setInt(1, currentTournamentId);
        updatePstmt.executeUpdate();
        updatePstmt.close();
        
        // Round of 16
        List<Team> quarterFinalists = runKnockoutRound(qualifiedTeams, "ROUND_16");
        
        // Quarter Finals
        List<Team> semiFinalists = runKnockoutRound(quarterFinalists, "QUARTER");
        
        // Semi Finals
        List<Team> finalists = runKnockoutRound(semiFinalists, "SEMI");
        
        // Third Place Match (losers of semi-finals)
        List<Team> thirdPlaceTeams = getThirdPlaceTeams(semiFinalists, finalists);
        if (thirdPlaceTeams.size() == 2) {
            runKnockoutMatch(thirdPlaceTeams.get(0), thirdPlaceTeams.get(1), "THIRD_PLACE");
        }
        
        // Final
        if (finalists.size() == 2) {
            Team champion = runKnockoutMatch(finalists.get(0), finalists.get(1), "FINAL");
            
            // Update tournament with final results
            updateTournamentResults(champion, finalists, thirdPlaceTeams);
        }
    }

    private List<Team> runKnockoutRound(List<Team> teams, String roundType) throws SQLException {
        System.out.println("🔥 " + roundType + " matches:");
        
        List<Team> winners = new ArrayList<>();
        
        for (int i = 0; i < teams.size(); i += 2) {
            if (i + 1 < teams.size()) {
                Team winner = runKnockoutMatch(teams.get(i), teams.get(i + 1), roundType);
                winners.add(winner);
            }
        }
        
        return winners;
    }

    private Team runKnockoutMatch(Team teamA, Team teamB, String matchType) throws SQLException {
        int[] score = DataGenerator.generateMatchScore();
        int teamAScore = score[0];
        int teamBScore = score[1];
        
        // In knockout, we need a winner - simulate extra time/penalties if needed
        if (teamAScore == teamBScore) {
            // Simulate penalty shootout
            teamAScore += random.nextBoolean() ? 1 : 0;
            teamBScore += (teamAScore > teamBScore) ? 0 : 1;
        }
        
        Team winner = (teamAScore > teamBScore) ? teamA : teamB;
        
        String venue = DataGenerator.getRandomVenue();
        String referee = DataGenerator.getRandomReferee();
        java.sql.Date matchDate = new java.sql.Date(System.currentTimeMillis() + random.nextInt(1000000000));
        
        // Insert match
        String matchSql = """
            INSERT INTO matches (team_a_id, team_b_id, team_a_score, team_b_score, match_type, 
                               match_date, venue, referee, status, winner_id)
            VALUES ((SELECT id FROM teams WHERE name = ?), 
                    (SELECT id FROM teams WHERE name = ?), 
                    ?, ?, ?, ?, ?, ?, 'COMPLETED',
                    (SELECT id FROM teams WHERE name = ?))
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(matchSql);
        pstmt.setString(1, teamA.getName());
        pstmt.setString(2, teamB.getName());
        pstmt.setInt(3, teamAScore);
        pstmt.setInt(4, teamBScore);
        pstmt.setString(5, matchType);
        pstmt.setDate(6, matchDate);
        pstmt.setString(7, venue);
        pstmt.setString(8, referee);
        pstmt.setString(9, winner.getName());
        pstmt.executeUpdate();
        pstmt.close();
        
        int matchId = dbManager.getLastInsertId();
        
        // Generate match events
        generateMatchEvents(matchId, teamA, teamB, teamAScore, teamBScore);
        
        System.out.println("  " + teamA.getName() + " " + teamAScore + " - " + teamBScore + " " + teamB.getName() + " (Winner: " + winner.getName() + ")");
        
        return winner;
    }

    private List<Team> getThirdPlaceTeams(List<Team> semiFinalists, List<Team> finalists) {
        List<Team> thirdPlaceTeams = new ArrayList<>();
        for (Team team : semiFinalists) {
            if (!finalists.contains(team)) {
                thirdPlaceTeams.add(team);
            }
        }
        return thirdPlaceTeams;
    }

    private void updateTournamentResults(Team champion, List<Team> finalists, List<Team> thirdPlaceTeams) throws SQLException {
        Team runnerUp = finalists.stream().filter(t -> !t.equals(champion)).findFirst().orElse(null);
        Team thirdPlace = thirdPlaceTeams.isEmpty() ? null : thirdPlaceTeams.get(0); // Winner of third place match
        
        String sql = """
            UPDATE tournaments SET 
                champion_id = (SELECT id FROM teams WHERE name = ?),
                runner_up_id = (SELECT id FROM teams WHERE name = ?),
                third_place_id = (SELECT id FROM teams WHERE name = ?),
                status = 'COMPLETED'
            WHERE id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, champion.getName());
        pstmt.setString(2, runnerUp != null ? runnerUp.getName() : null);
        pstmt.setString(3, thirdPlace != null ? thirdPlace.getName() : null);
        pstmt.setInt(4, currentTournamentId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    private void generateTournamentStatistics() throws SQLException {
        System.out.println("📊 Generating tournament statistics...");
        
        // Calculate total statistics
        String statsSql = """
            SELECT 
                COUNT(DISTINCT m.id) as total_matches,
                COALESCE(SUM(m.team_a_score + m.team_b_score), 0) as total_goals,
                COUNT(DISTINCT c.id) as total_cards,
                COUNT(DISTINCT s.id) as total_substitutions
            FROM matches m
            LEFT JOIN cards c ON m.id = c.match_id
            LEFT JOIN substitutions s ON m.id = s.match_id
        """;
        
        PreparedStatement statsPstmt = dbManager.getConnection().prepareStatement(statsSql);
        ResultSet statsRs = statsPstmt.executeQuery();
        
        int totalMatches = 0, totalGoals = 0, totalCards = 0, totalSubstitutions = 0;
        
        if (statsRs.next()) {
            totalMatches = statsRs.getInt("total_matches");
            totalGoals = statsRs.getInt("total_goals");
            totalCards = statsRs.getInt("total_cards");
            totalSubstitutions = statsRs.getInt("total_substitutions");
        }
        
        statsRs.close();
        statsPstmt.close();
        
        // Find top scorer
        String topScorerSql = """
            SELECT p.id, p.name, COUNT(g.id) as goal_count
            FROM players p
            JOIN goals g ON p.id = g.player_id
            GROUP BY p.id, p.name
            ORDER BY goal_count DESC
            LIMIT 1
        """;
        
        PreparedStatement topScorerPstmt = dbManager.getConnection().prepareStatement(topScorerSql);
        ResultSet topScorerRs = topScorerPstmt.executeQuery();
        
        Integer topScorerId = null;
        int topScorerGoals = 0;
        
        if (topScorerRs.next()) {
            topScorerId = topScorerRs.getInt("id");
            topScorerGoals = topScorerRs.getInt("goal_count");
        }
        
        topScorerRs.close();
        topScorerPstmt.close();
        
        // Insert tournament statistics
        String insertStatsSql = """
            INSERT INTO tournament_stats (tournament_id, total_goals, total_matches, 
                                        total_yellow_cards, total_red_cards, total_substitutions,
                                        top_scorer_id, top_scorer_goals)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        PreparedStatement insertStatsPstmt = dbManager.getConnection().prepareStatement(insertStatsSql);
        insertStatsPstmt.setInt(1, currentTournamentId);
        insertStatsPstmt.setInt(2, totalGoals);
        insertStatsPstmt.setInt(3, totalMatches);
        insertStatsPstmt.setInt(4, totalCards); // Simplified - counting all cards as yellow
        insertStatsPstmt.setInt(5, 0); // Red cards would need separate counting
        insertStatsPstmt.setInt(6, totalSubstitutions);
        if (topScorerId != null) {
            insertStatsPstmt.setInt(7, topScorerId);
        } else {
            insertStatsPstmt.setNull(7, Types.INTEGER);
        }
        insertStatsPstmt.setInt(8, topScorerGoals);
        insertStatsPstmt.executeUpdate();
        insertStatsPstmt.close();
        
        System.out.println("✅ Tournament statistics generated");
    }

    private void displayFinalResults() throws SQLException {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🏆 FIFA WORLD CUP FINAL RESULTS 🏆");
        System.out.println("=".repeat(50));
        
        // Get tournament results
        String resultsSql = """
            SELECT 
                t.name as tournament_name,
                t.year,
                champion.name as champion,
                runner_up.name as runner_up,
                third_place.name as third_place,
                ts.total_goals,
                ts.total_matches,
                top_scorer.name as top_scorer_name,
                ts.top_scorer_goals
            FROM tournaments t
            LEFT JOIN teams champion ON t.champion_id = champion.id
            LEFT JOIN teams runner_up ON t.runner_up_id = runner_up.id
            LEFT JOIN teams third_place ON t.third_place_id = third_place.id
            LEFT JOIN tournament_stats ts ON t.id = ts.tournament_id
            LEFT JOIN players top_scorer ON ts.top_scorer_id = top_scorer.id
            WHERE t.id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(resultsSql);
        pstmt.setInt(1, currentTournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            System.out.println("🏆 CHAMPION: " + rs.getString("champion"));
            System.out.println("🥈 RUNNER-UP: " + rs.getString("runner_up"));
            System.out.println("🥉 THIRD PLACE: " + rs.getString("third_place"));
            System.out.println();
            System.out.println("📊 TOURNAMENT STATISTICS:");
            System.out.println("   Total Matches: " + rs.getInt("total_matches"));
            System.out.println("   Total Goals: " + rs.getInt("total_goals"));
            System.out.println("   Top Scorer: " + rs.getString("top_scorer_name") + " (" + rs.getInt("top_scorer_goals") + " goals)");
        }
        
        rs.close();
        pstmt.close();
        
        // Display group stage final standings
        System.out.println("\n📋 GROUP STAGE FINAL STANDINGS:");
        displayGroupStandings();
        
        System.out.println("\n" + "=".repeat(50));
    }

    private void displayGroupStandings() throws SQLException {
        for (int groupId = 1; groupId <= 8; groupId++) {
            String groupName = String.valueOf((char)('A' + groupId - 1));
            System.out.println("\nGroup " + groupName + ":");
            
            String sql = """
                SELECT name, points, wins, draws, losses, goals_for, goals_against, goal_difference
                FROM teams 
                WHERE group_id = ? 
                ORDER BY points DESC, goal_difference DESC, goals_for DESC
            """;
            
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();
            
            int position = 1;
            while (rs.next()) {
                String status = position <= 2 ? " ✅" : "";
                System.out.printf("  %d. %-15s %2d pts (%d-%d-%d) %2d:%2d (%+d)%s%n",
                    position,
                    rs.getString("name"),
                    rs.getInt("points"),
                    rs.getInt("wins"),
                    rs.getInt("draws"),
                    rs.getInt("losses"),
                    rs.getInt("goals_for"),
                    rs.getInt("goals_against"),
                    rs.getInt("goal_difference"),
                    status
                );
                position++;
            }
            
            rs.close();
            pstmt.close();
        }
    }

    public void close() {
        if (dbManager != null) {
            dbManager.close();
        }
    }

    // Main method to run the automation
    public static void main(String[] args) {
        WorldCupAutomation automation = new WorldCupAutomation();
        try {
            automation.runCompleteWorldCup();
        } finally {
            automation.close();
        }
    }
}