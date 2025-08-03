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
            // Bước 2: Tạo giải đấu
            createTournament();
            
            // Bước 3: Tạo các đội bóng
            generateTeams();
            
            // Bước 4: Tạo bảng đấu và phân chia đội
            createGroupsAndAssignTeams();
            
            // Bước 5: Chạy vòng bảng
            runGroupStage();
            
            // Bước 6: Xác định đội nhất và nhì bảng
            List<Team> qualifiedTeams = determineQualifiedTeams();
            
            // Bước 7: Chạy vòng loại trực tiếp
            runKnockoutStage(qualifiedTeams);
            
            // Bước 8: Tạo thống kê cuối giải
            generateTournamentStatistics();
            
            // Bước 9: Hiển thị kết quả
            displayFinalResults();
            
            System.out.println("🎉 World Cup hoàn thành thành công!");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong quá trình mô phỏng World Cup: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void createTournament() throws SQLException {
        System.out.println("Tạo giải đấu mới...");
        int year = 2024;
        String name = DataGenerator.generateTournamentName(year);
        String hostCountry = "Qatar"; // Có thể được ngẫu nhiên hóa
        
        java.sql.Date startDate = new java.sql.Date(System.currentTimeMillis());
        java.sql.Date endDate = new java.sql.Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)); // 30 ngày sau
        
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
        System.out.println("✅ Đã tạo giải đấu: " + name);
    }

    private void generateTeams() throws SQLException {
        System.out.println("🌍 Đang tạo 32 đội bóng...");
        
        teams = DataGenerator.generateRandomTeams(32);
        
        for (Team team : teams) {
            insertTeamToDatabase(team);
        }
        
        System.out.println("✅ Đã tạo " + teams.size() + " đội bóng");
    }

    private void insertTeamToDatabase(Team team) throws SQLException {
        // Thêm đội bóng
        String teamSql = """
            INSERT INTO teams (name, region, coach, medical_staff, is_host, tournament_id)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(teamSql);
        pstmt.setString(1, team.getName());
        pstmt.setString(2, team.getRegion());
        pstmt.setString(3, team.getCoach());
        pstmt.setString(4, team.getMedicalStaff());
        pstmt.setBoolean(5, team.isHost());
        pstmt.setInt(6, currentTournamentId);
        pstmt.executeUpdate();
        pstmt.close();
        
        int teamId = dbManager.getLastInsertId();
        
        // Thêm trợ lý huấn luyện viên
        for (String assistant : team.getAssistantCoaches()) {
            String assistantSql = "INSERT INTO assistant_coaches (name, team_id) VALUES (?, ?)";
            PreparedStatement assistantPstmt = dbManager.getConnection().prepareStatement(assistantSql);
            assistantPstmt.setString(1, assistant);
            assistantPstmt.setInt(2, teamId);
            assistantPstmt.executeUpdate();
            assistantPstmt.close();
        }
        
        // Thêm cầu thủ chính
        for (Player player : team.getStartingPlayers()) {
            insertPlayerToDatabase(player, teamId, true);
        }
        
        // Thêm cầu thủ dự bị
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
        System.out.println("🔤 Đang tạo bảng đấu và phân chia đội...");
        
        // Tạo 8 bảng đấu (A-H)
        String[] groupNames = {"A", "B", "C", "D", "E", "F", "G", "H"};
        
        for (String groupName : groupNames) {
            String sql = "INSERT INTO groups (name, tournament_id) VALUES (?, ?)";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, groupName);
            pstmt.setInt(2, currentTournamentId);
            pstmt.executeUpdate();
            pstmt.close();
            
            int groupId = dbManager.getLastInsertId();
            Group group = new Group(groupName);
            groups.add(group);
        }
        
        // Phân chia đội vào các bảng (4 đội mỗi bảng)
        Collections.shuffle(teams); // Ngẫu nhiên hóa việc phân chia đội
        
        for (int i = 0; i < teams.size(); i++) {
            int groupIndex = i / 4; // 4 đội mỗi bảng
            int groupId = groupIndex + 1; // ID bảng bắt đầu từ 1
            
            // Cập nhật đội với bảng được phân
            String sql = "UPDATE teams SET group_id = ? WHERE name = ? AND tournament_id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setString(2, teams.get(i).getName());
            pstmt.setInt(3, currentTournamentId);
            pstmt.executeUpdate();
            pstmt.close();
            
            // Thêm đội vào đối tượng bảng
            groups.get(groupIndex).addTeam(teams.get(i));
        }
        
        System.out.println("✅ Đã tạo 8 bảng đấu với 4 đội mỗi bảng");
    }

    private void runGroupStage() throws SQLException {
        System.out.println("⚽ Đang chạy các trận đấu vòng bảng...");
        
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            Group group = groups.get(groupIndex);
            List<Team> groupTeams = group.getTeams();
            
            System.out.println("🔤 Các trận đấu Bảng " + group.getName() + ":");
            
            // Tạo tất cả các trận đấu có thể trong bảng (tổng 6 trận)
            for (int i = 0; i < groupTeams.size(); i++) {
                for (int j = i + 1; j < groupTeams.size(); j++) {
                    Team teamA = groupTeams.get(i);
                    Team teamB = groupTeams.get(j);
                    
                    simulateMatch(teamA, teamB, "GROUP", groupIndex + 1);
                }
            }
        }
        
        System.out.println("✅ Vòng bảng hoàn thành");
    }

    private void simulateMatch(Team teamA, Team teamB, String matchType, int groupId) throws SQLException {
        // Tạo kết quả trận đấu
        int[] score = DataGenerator.generateMatchScore();
        int teamAScore = score[0];
        int teamBScore = score[1];
        
        String venue = DataGenerator.getRandomVenue();
        String referee = DataGenerator.getRandomReferee();
        java.sql.Date matchDate = new java.sql.Date(System.currentTimeMillis() + random.nextInt(1000000000));
        
        // Thêm trận đấu
        String matchSql = """
            INSERT INTO matches (team_a_id, team_b_id, team_a_score, team_b_score, match_type, 
                               match_date, venue, referee, status, group_id)
            VALUES ((SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                    (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                    ?, ?, ?, ?, ?, ?, 'COMPLETED', ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(matchSql);
        pstmt.setString(1, teamA.getName());
        pstmt.setInt(2, currentTournamentId);
        pstmt.setString(3, teamB.getName());
        pstmt.setInt(4, currentTournamentId);
        pstmt.setInt(5, teamAScore);
        pstmt.setInt(6, teamBScore);
        pstmt.setString(7, matchType);
        pstmt.setDate(8, matchDate);
        pstmt.setString(9, venue);
        pstmt.setString(10, referee);
        pstmt.setInt(11, groupId);
        pstmt.executeUpdate();
        pstmt.close();
        
        int matchId = dbManager.getLastInsertId();
        
        // Cập nhật thống kê đội
        updateTeamStatistics(teamA, teamB, teamAScore, teamBScore);
        
        // Tạo các sự kiện trận đấu (bàn thắng, thẻ, thay người)
        generateMatchEvents(matchId, teamA, teamB, teamAScore, teamBScore);
        
        System.out.println("  " + teamA.getName() + " " + teamAScore + " - " + teamBScore + " " + teamB.getName());
    }

    private void updateTeamStatistics(Team teamA, Team teamB, int teamAScore, int teamBScore) throws SQLException {
        // Cập nhật đội A
        updateSingleTeamStats(teamA.getName(), teamAScore, teamBScore);
        
        // Cập nhật đội B
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
            WHERE name = ? AND tournament_id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, points);
        pstmt.setInt(2, goalsFor);
        pstmt.setInt(3, goalsAgainst);
        pstmt.setInt(4, wins);
        pstmt.setInt(5, draws);
        pstmt.setInt(6, losses);
        pstmt.setString(7, teamName);
        pstmt.setInt(8, currentTournamentId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    private void generateMatchEvents(int matchId, Team teamA, Team teamB, int teamAScore, int teamBScore) throws SQLException {
        // Tạo bàn thắng cho đội A
        generateGoalsForTeam(matchId, teamA, teamAScore);
        
        // Tạo bàn thắng cho đội B
        generateGoalsForTeam(matchId, teamB, teamBScore);
        
        // Tạo thẻ và thay người
        generateCardsAndSubstitutions(matchId, teamA, teamB);
    }

    private void generateGoalsForTeam(int matchId, Team team, int goalCount) throws SQLException {
        for (int i = 0; i < goalCount; i++) {
            // Chọn cầu thủ ngẫu nhiên từ đội hình xuất phát
            List<Player> startingPlayers = team.getStartingPlayers();
            if (!startingPlayers.isEmpty()) {
                Player scorer = DataGenerator.getRandomElement(startingPlayers);
                int minute = DataGenerator.generateRandomMinute();
                
                String sql = """
                    INSERT INTO goals (match_id, player_id, team_id, minute, goal_type)
                    VALUES (?, (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)), 
                            (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), ?, 'REGULAR')
                """;
                
                PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
                pstmt.setInt(1, matchId);
                pstmt.setString(2, scorer.getName());
                pstmt.setString(3, team.getName());
                pstmt.setInt(4, currentTournamentId);
                pstmt.setString(5, team.getName());
                pstmt.setInt(6, currentTournamentId);
                pstmt.setInt(7, minute);
                pstmt.executeUpdate();
                pstmt.close();
            }
        }
    }

    private void generateCardsAndSubstitutions(int matchId, Team teamA, Team teamB) throws SQLException {
        // Tạo thẻ cho cả hai đội
        generateCardsForTeam(matchId, teamA);
        generateCardsForTeam(matchId, teamB);
        
        // Tạo thay người cho cả hai đội
        generateSubstitutionsForTeam(matchId, teamA);
        generateSubstitutionsForTeam(matchId, teamB);
    }

    private void generateCardsForTeam(int matchId, Team team) throws SQLException {
        List<Player> allPlayers = new ArrayList<>(team.getStartingPlayers());
        
        // Thẻ vàng
        if (DataGenerator.shouldHaveYellowCard()) {
            Player player = DataGenerator.getRandomElement(allPlayers);
            if (player != null) {
                int minute = DataGenerator.generateRandomMinute();
                insertCard(matchId, team, player, "YELLOW", minute);
            }
        }
        
        // Thẻ đỏ (ít phổ biến hơn)
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
            VALUES (?, (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)), 
                    (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), ?, ?)
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, matchId);
        pstmt.setString(2, player.getName());
        pstmt.setString(3, team.getName());
        pstmt.setInt(4, currentTournamentId);
        pstmt.setString(5, team.getName());
        pstmt.setInt(6, currentTournamentId);
        pstmt.setString(7, cardType);
        pstmt.setInt(8, minute);
        pstmt.executeUpdate();
        pstmt.close();
    }

    private void generateSubstitutionsForTeam(int matchId, Team team) throws SQLException {
        if (DataGenerator.shouldHaveSubstitution()) {
            int substitutionCount = random.nextInt(3) + 1; // 1-3 lần thay người
            
            for (int i = 0; i < substitutionCount && i < team.getSubstitutePlayers().size(); i++) {
                Player playerOut = DataGenerator.getRandomElement(team.getStartingPlayers());
                Player playerIn = DataGenerator.getRandomElement(team.getSubstitutePlayers());
                
                if (playerOut != null && playerIn != null) {
                    int minute = DataGenerator.generateSubstitutionMinute();
                    
                    String sql = """
                        INSERT INTO substitutions (match_id, team_id, player_in_id, player_out_id, minute)
                        VALUES (?, (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                                (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)),
                                (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)), ?)
                    """;
                    
                    PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
                    pstmt.setInt(1, matchId);
                    pstmt.setString(2, team.getName());
                    pstmt.setInt(3, currentTournamentId);
                    pstmt.setString(4, playerIn.getName());
                    pstmt.setString(5, team.getName());
                    pstmt.setInt(6, currentTournamentId);
                    pstmt.setString(7, playerOut.getName());
                    pstmt.setString(8, team.getName());
                    pstmt.setInt(9, currentTournamentId);
                    pstmt.setInt(10, minute);
                    pstmt.executeUpdate();
                    pstmt.close();
                }
            }
        }
    }

    private List<Team> determineQualifiedTeams() throws SQLException {
        System.out.println("🏅 Đang xác định các đội vượt qua vòng bảng...");
        
        // Phân chia đội nhất và nhì bảng
        List<Team> firstPlaceTeams = new ArrayList<>();  // Nhất bảng của mỗi bảng
        List<Team> secondPlaceTeams = new ArrayList<>(); // Nhì bảng của mỗi bảng
        
        for (int groupId = 1; groupId <= 8; groupId++) {
            String sql = """
                SELECT name FROM teams 
                WHERE group_id = ? AND tournament_id = ?
                ORDER BY points DESC, goal_difference DESC, goals_for DESC
                LIMIT 2
            """;
            
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, currentTournamentId);
            ResultSet rs = pstmt.executeQuery();
            
            int position = 1;
            while (rs.next()) {
                String teamName = rs.getString("name");
                Team team = teams.stream()
                    .filter(t -> t.getName().equals(teamName))
                    .findFirst()
                    .orElse(null);
                
                if (team != null) {
                    if (position == 1) {
                        firstPlaceTeams.add(team);
                        System.out.println("🥇 Nhất Bảng " + (char)('A' + groupId - 1) + ": " + team.getName());
                    } else {
                        secondPlaceTeams.add(team);
                        System.out.println("🥈 Nhì Bảng " + (char)('A' + groupId - 1) + ": " + team.getName());
                    }
                    position++;
                }
            }
            
            rs.close();
            pstmt.close();
        }
        
        // Tạo danh sách đội vượt qua với ghép đôi vòng 16 đội phù hợp
        List<Team> qualifiedTeams = createRoundOf16Pairings(firstPlaceTeams, secondPlaceTeams);
        
        System.out.println("✅ " + qualifiedTeams.size() + " đội đã vào vòng loại trực tiếp");
        return qualifiedTeams;
    }
    
    private List<Team> createRoundOf16Pairings(List<Team> firstPlace, List<Team> secondPlace) {
        System.out.println("\n🏆 Ghép Đôi Vòng 16 Đội (Theo Quy Định FIFA):");
        
        List<Team> pairings = new ArrayList<>();
        
        // Ghép đôi vòng 16 đội FIFA World Cup:
        // Trận 1: Nhất bảng A vs Nhì bảng B
        pairings.add(firstPlace.get(0));   // Nhất bảng A
        pairings.add(secondPlace.get(1));  // Nhì bảng B
        System.out.println("Trận 1: " + firstPlace.get(0).getName() + " (A1) vs " + secondPlace.get(1).getName() + " (B2)");
        
        // Trận 2: Nhất bảng B vs Nhì bảng A  
        pairings.add(firstPlace.get(1));   // Nhất bảng B
        pairings.add(secondPlace.get(0));  // Nhì bảng A
        System.out.println("Trận 2: " + firstPlace.get(1).getName() + " (B1) vs " + secondPlace.get(0).getName() + " (A2)");
        
        // Trận 3: Nhất bảng C vs Nhì bảng D
        pairings.add(firstPlace.get(2));   // Nhất bảng C
        pairings.add(secondPlace.get(3));  // Nhì bảng D
        System.out.println("Trận 3: " + firstPlace.get(2).getName() + " (C1) vs " + secondPlace.get(3).getName() + " (D2)");
        
        // Trận 4: Nhất bảng D vs Nhì bảng C
        pairings.add(firstPlace.get(3));   // Nhất bảng D
        pairings.add(secondPlace.get(2));  // Nhì bảng C
        System.out.println("Trận 4: " + firstPlace.get(3).getName() + " (D1) vs " + secondPlace.get(2).getName() + " (C2)");
        
        // Trận 5: Nhất bảng E vs Nhì bảng F
        pairings.add(firstPlace.get(4));   // Nhất bảng E
        pairings.add(secondPlace.get(5));  // Nhì bảng F
        System.out.println("Trận 5: " + firstPlace.get(4).getName() + " (E1) vs " + secondPlace.get(5).getName() + " (F2)");
        
        // Trận 6: Nhất bảng F vs Nhì bảng E
        pairings.add(firstPlace.get(5));   // Nhất bảng F
        pairings.add(secondPlace.get(4));  // Nhì bảng E
        System.out.println("Trận 6: " + firstPlace.get(5).getName() + " (F1) vs " + secondPlace.get(4).getName() + " (E2)");
        
        // Trận 7: Nhất bảng G vs Nhì bảng H
        pairings.add(firstPlace.get(6));   // Nhất bảng G
        pairings.add(secondPlace.get(7));  // Nhì bảng H
        System.out.println("Trận 7: " + firstPlace.get(6).getName() + " (G1) vs " + secondPlace.get(7).getName() + " (H2)");
        
        // Trận 8: Nhất bảng H vs Nhì bảng G
        pairings.add(firstPlace.get(7));   // Nhất bảng H
        pairings.add(secondPlace.get(6));  // Nhì bảng G
        System.out.println("Trận 8: " + firstPlace.get(7).getName() + " (H1) vs " + secondPlace.get(6).getName() + " (G2)");
        
        System.out.println();
        return pairings;
    }

    private void runKnockoutStage(List<Team> qualifiedTeams) throws SQLException {
        System.out.println("🏆 Đang chạy vòng loại trực tiếp...");
        
        // Cập nhật trạng thái giải đấu
        String updateSql = "UPDATE tournaments SET status = 'KNOCKOUT' WHERE id = ?";
        PreparedStatement updatePstmt = dbManager.getConnection().prepareStatement(updateSql);
        updatePstmt.setInt(1, currentTournamentId);
        updatePstmt.executeUpdate();
        updatePstmt.close();
        
        // Vòng 16 đội
        List<Team> quarterFinalists = runKnockoutRound(qualifiedTeams, "ROUND_16");
        
        // Tứ kết
        List<Team> semiFinalists = runKnockoutRound(quarterFinalists, "QUARTER");
        
        // Bán kết
        List<Team> finalists = runKnockoutRound(semiFinalists, "SEMI");
        
        // Tranh hạng ba (đội thua bán kết)
        List<Team> thirdPlaceTeams = getThirdPlaceTeams(semiFinalists, finalists);
        if (thirdPlaceTeams.size() == 2) {
            runKnockoutMatch(thirdPlaceTeams.get(0), thirdPlaceTeams.get(1), "THIRD_PLACE");
        }
        
        // Chung kết
        if (finalists.size() == 2) {
            Team champion = runKnockoutMatch(finalists.get(0), finalists.get(1), "FINAL");
            
            // Cập nhật giải đấu với kết quả cuối cùng
            updateTournamentResults(champion, finalists, thirdPlaceTeams);
        }
    }

    private List<Team> runKnockoutRound(List<Team> teams, String roundType) throws SQLException {
        System.out.println("🔥 Các trận đấu " + roundType + ":");
        
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
        
        // Trong vòng loại trực tiếp, cần có người thắng - mô phỏng hiệp phụ/penalty nếu cần
        if (teamAScore == teamBScore) {
            // Mô phỏng loạt sút penalty
            teamAScore += random.nextBoolean() ? 1 : 0;
            teamBScore += (teamAScore > teamBScore) ? 0 : 1;
        }
        
        Team winner = (teamAScore > teamBScore) ? teamA : teamB;
        
        String venue = DataGenerator.getRandomVenue();
        String referee = DataGenerator.getRandomReferee();
        java.sql.Date matchDate = new java.sql.Date(System.currentTimeMillis() + random.nextInt(1000000000));
        
        // Thêm trận đấu
        String matchSql = """
            INSERT INTO matches (team_a_id, team_b_id, team_a_score, team_b_score, match_type, 
                               match_date, venue, referee, status, winner_id)
            VALUES ((SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                    (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                    ?, ?, ?, ?, ?, ?, 'COMPLETED',
                    (SELECT id FROM teams WHERE name = ? AND tournament_id = ?))
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(matchSql);
        pstmt.setString(1, teamA.getName());
        pstmt.setInt(2, currentTournamentId);
        pstmt.setString(3, teamB.getName());
        pstmt.setInt(4, currentTournamentId);
        pstmt.setInt(5, teamAScore);
        pstmt.setInt(6, teamBScore);
        pstmt.setString(7, matchType);
        pstmt.setDate(8, matchDate);
        pstmt.setString(9, venue);
        pstmt.setString(10, referee);
        pstmt.setString(11, winner.getName());
        pstmt.setInt(12, currentTournamentId);
        pstmt.executeUpdate();
        pstmt.close();
        
        int matchId = dbManager.getLastInsertId();
        
        // Tạo các sự kiện trận đấu
        generateMatchEvents(matchId, teamA, teamB, teamAScore, teamBScore);
        
        System.out.println("  " + teamA.getName() + " " + teamAScore + " - " + teamBScore + " " + teamB.getName() + " (Người thắng: " + winner.getName() + ")");
        
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
        Team thirdPlace = thirdPlaceTeams.isEmpty() ? null : thirdPlaceTeams.get(0); // Người thắng trận tranh hạng ba
        
        String sql = """
            UPDATE tournaments SET 
                champion_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?),
                runner_up_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?),
                third_place_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?),
                status = 'COMPLETED'
            WHERE id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, champion.getName());
        pstmt.setInt(2, currentTournamentId);
        pstmt.setString(3, runnerUp != null ? runnerUp.getName() : null);
        pstmt.setInt(4, currentTournamentId);
        pstmt.setString(5, thirdPlace != null ? thirdPlace.getName() : null);
        pstmt.setInt(6, currentTournamentId);
        pstmt.setInt(7, currentTournamentId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    private void generateTournamentStatistics() throws SQLException {
        System.out.println("📊 Đang tạo thống kê giải đấu...");
        
        // Tính toán tổng thống kê
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
        
        // Tìm vua phá lưới
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
        
        // Thêm thống kê giải đấu
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
        insertStatsPstmt.setInt(4, totalCards); // Đơn giản hóa - tính tất cả thẻ là thẻ vàng
        insertStatsPstmt.setInt(5, 0); // Thẻ đỏ cần tính riêng
        insertStatsPstmt.setInt(6, totalSubstitutions);
        if (topScorerId != null) {
            insertStatsPstmt.setInt(7, topScorerId);
        } else {
            insertStatsPstmt.setNull(7, Types.INTEGER);
        }
        insertStatsPstmt.setInt(8, topScorerGoals);
        insertStatsPstmt.executeUpdate();
        insertStatsPstmt.close();
        
        System.out.println("✅ Đã tạo thống kê giải đấu");
    }

    private void displayFinalResults() throws SQLException {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🏆 KẾT QUẢ CUỐI CÙNG FIFA WORLD CUP 🏆");
        System.out.println("=".repeat(50));
        
        // Lấy kết quả giải đấu
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
            System.out.println("🏆 VÔ ĐỊCH: " + rs.getString("champion"));
            System.out.println("🥈 Á QUÂN: " + rs.getString("runner_up"));
            System.out.println("🥉 HẠNG BA: " + rs.getString("third_place"));
            System.out.println();
            System.out.println("📊 THỐNG KÊ GIẢI ĐẤU:");
            System.out.println("   Tổng số trận: " + rs.getInt("total_matches"));
            System.out.println("   Tổng số bàn thắng: " + rs.getInt("total_goals"));
            System.out.println("   Vua phá lưới: " + rs.getString("top_scorer_name") + " (" + rs.getInt("top_scorer_goals") + " bàn thắng)");
        }
        
        rs.close();
        pstmt.close();
        
        // Hiển thị bảng xếp hạng cuối vòng bảng
        System.out.println("\n📋 BẢNG XẾP HẠNG CUỐI VÒNG BẢNG:");
        displayGroupStandings();
        
        System.out.println("\n" + "=".repeat(50));
    }

    private void displayGroupStandings() throws SQLException {
        for (int groupId = 1; groupId <= 8; groupId++) {
            String groupName = String.valueOf((char)('A' + groupId - 1));
            System.out.println("\nBảng " + groupName + ":");
            
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

    // Phương thức chính để chạy mô phỏng
    public static void main(String[] args) {
        WorldCupAutomation automation = new WorldCupAutomation();
        try {
            automation.runCompleteWorldCup();
        } finally {
            automation.close();
        }
    }
}