package com.worldcup.automation;

import com.worldcup.calculator.TournamentStatsCalculator;
import com.worldcup.database.DatabaseManager;
import com.worldcup.generator.DataGenerator;
import com.worldcup.model.Group;
import com.worldcup.model.Match;
import com.worldcup.model.Player;
import com.worldcup.model.Team;
import com.worldcup.service.TeamService;
import com.worldcup.service.TournamentService;
import com.worldcup.service.PlayerService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class WorldCupAutomation {
    private DatabaseManager dbManager;
    // DataGenerator has only static methods, no instance needed
    private TournamentStatsCalculator statsCalculator;
    private TeamService teamService;
    private TournamentService tournamentService;
    private PlayerService playerService;
    private List<Team> teams;
    private List<Match> matches;
    private List<Group> groups;
    private int currentTournamentId;
    private Random random = new Random();

    public WorldCupAutomation() {
        this.dbManager = new DatabaseManager();
        // DataGenerator uses static methods only
        this.statsCalculator = new TournamentStatsCalculator(dbManager);
        this.teamService = new TeamService(dbManager);
        this.tournamentService = new TournamentService(dbManager);
        this.playerService = new PlayerService(dbManager);

        this.teams = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.matches = new ArrayList<>();
    }


    public void runCompleteWorldCup() {
        try {
            // Bước 1: Tạo giải đấu mới với dữ liệu random
            createTournament();

            // Bước 2: Tạo các đội bóng
            generateTeams();

            // Bước 3: Tạo bảng đấu và phân chia đội
            createGroupsAndAssignTeams();

            // Bước 5: Chạy vòng bảng
            runGroupStage();

            // Bước 6: Xác định đội nhất và nhì bảng
            List<Team> qualifiedTeams = determineQualifiedTeams();

            // Bước 7: Chạy vòng loại trực tiếp
            runKnockoutStage(qualifiedTeams);

            // Bước 8: Tạo thống kê cuối giải
            generateTournamentStatistics();

            // Bước 9: Tính toán lại tournament stats chính xác
            recalculateCurrentTournamentStats();


            // Bước 12: Hiển thị kết quả
            displayFinalResults();

            System.out.println("🎉 World Cup hoàn thành thành công!");

        } catch (Exception e) {
            System.err.println("❌ Lỗi trong quá trình mô phỏng World Cup: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void createTournament() throws SQLException {
        System.out.println("Khởi tạo các cầu thủ và đội");

        int randomYear = DataGenerator.getRandomWorldCupYear(); // trả về 1 năm
        String randomHostCountry = DataGenerator.getRandomHostCountry(); // trả về 1 địa điểm
        String[] randomDates = DataGenerator.generateTournamentDates(randomYear); // trả về yyyy/MM/dd
        String name = DataGenerator.generateTournamentName(randomYear); // trả về tên giải đấu

        System.out.println("Đã tạo giải đấu: " + name);
        System.out.println("Năm: " + randomYear);
        System.out.println("Nước chủ nhà: " + randomHostCountry);
        System.out.println("Ngày bắt đầu: " + randomDates[0]);
        System.out.println("Ngày kết thúc: " + randomDates[1]);

        String sql = """
                    INSERT INTO tournaments (name, year, host_country, start_date, end_date)
                    VALUES (?, ?, ?, ?, ?)
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, name);
        pstmt.setInt(2, randomYear);
        pstmt.setString(3, randomHostCountry);
        pstmt.setString(4, randomDates[0]); // start_date dạng YYYY/MM/DD
        pstmt.setString(5, randomDates[1]); // end_date dạng YYYY/MM/DD
        pstmt.executeUpdate();
        pstmt.close();
        currentTournamentId = dbManager.getLastInsertId();
    }

    private void generateTeams() throws SQLException {
        System.out.println("Tạo 32 đội bóng...");

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
        System.out.println("Tạo bảng đấu và phân chia đội...");

        for (String groupName : DataGenerator.getGroupsName()) {
            String sql = "INSERT INTO groups (name, tournament_id) VALUES (?, ?)";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, groupName);
            pstmt.setInt(2, currentTournamentId);
            pstmt.executeUpdate();
            pstmt.close();

            int groupId = dbManager.getLastInsertId();
            Group group = new Group(groupId, groupName);
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

        System.out.println("Đã tạo 8 bảng đấu với 4 đội mỗi bảng");
    }

    private void runGroupStage() throws SQLException {
        System.out.println("Chạy các trận đấu vòng bảng...");

        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            Group group = groups.get(groupIndex);
            List<Team> groupTeams = group.getTeams();

            System.out.println("Các trận đấu Bảng " + group.getName() + ":");

            // Tạo tất cả các trận đấu có thể trong bảng (tổng 6 trận)
            for (int i = 0; i < groupTeams.size(); i++) {
                for (int j = i + 1; j < groupTeams.size(); j++) {
                    Team teamA = groupTeams.get(i);
                    Team teamB = groupTeams.get(j);

                    simulateMatch(teamA, teamB, "GROUP", groupIndex + 1);
                }
            }
        }

        System.out.println("Vòng bảng hoàn thành");
    }

    private void simulateMatch(Team teamA, Team teamB, String matchType, int groupId) throws SQLException {
        // Tạo kết quả trận đấu
        int[] score = DataGenerator.generateMatchScore();
        int teamAScore = score[0];
        int teamBScore = score[1];

        String venue = DataGenerator.getRandomVenue();
        String referee = DataGenerator.getRandomReferee();
        
        // Format date as yyyy/mm/dd
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");
        String matchDate = dateFormat.format(new java.util.Date(System.currentTimeMillis() + random.nextInt(1000000000)));

        // Thêm trận đấu (removed round_number and status columns)
        String matchSql = """
                    INSERT INTO matches (team_a_id, team_b_id, team_a_score, team_b_score, match_type, 
                                       match_date, venue, referee)
                    VALUES ((SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                            (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                            ?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(matchSql);
        pstmt.setString(1, teamA.getName());
        pstmt.setInt(2, currentTournamentId);
        pstmt.setString(3, teamB.getName());
        pstmt.setInt(4, currentTournamentId);
        pstmt.setInt(5, teamAScore);
        pstmt.setInt(6, teamBScore);
        pstmt.setString(7, matchType);
        pstmt.setString(8, matchDate);
        pstmt.setString(9, venue);
        pstmt.setString(10, referee);
        pstmt.executeUpdate();
        pstmt.close();

        int matchId = dbManager.getLastInsertId();

        // Tạo các sự kiện trận đấu (bàn thắng, thẻ, thay người)
        generateMatchEvents(matchId, teamA, teamB, teamAScore, teamBScore);

        System.out.println("  " + teamA.getName() + " " + teamAScore + " - " + teamBScore + " " + teamB.getName());
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
                
                // Cập nhật goals trong bảng players
                String updatePlayerSql = """
                    UPDATE players 
                    SET goals = goals + 1 
                    WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)
                """;
                PreparedStatement updateStmt = dbManager.getConnection().prepareStatement(updatePlayerSql);
                updateStmt.setString(1, scorer.getName());
                updateStmt.setString(2, team.getName());
                updateStmt.setInt(3, currentTournamentId);
                updateStmt.executeUpdate();
                updateStmt.close();
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
        
        // Cập nhật cards trong bảng players
        String updatePlayerSql;
        if ("YELLOW".equals(cardType)) {
            updatePlayerSql = """
                UPDATE players 
                SET yellow_cards = yellow_cards + 1 
                WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)
            """;
        } else {
            updatePlayerSql = """
                UPDATE players 
                SET red_cards = red_cards + 1 
                WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)
            """;
        }
        
        PreparedStatement updateStmt = dbManager.getConnection().prepareStatement(updatePlayerSql);
        updateStmt.setString(1, player.getName());
        updateStmt.setString(2, team.getName());
        updateStmt.setInt(3, currentTournamentId);
        updateStmt.executeUpdate();
        updateStmt.close();
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

        // Sử dụng TournamentService để lấy qualified teams với logic Java
        List<Team> qualifiedTeams = tournamentService.getQualifiedTeamsCalculatedInJava(currentTournamentId);

        // Hiển thị kết quả
        displayQualifiedTeams(qualifiedTeams);

        return qualifiedTeams;
    }

    /**
     * Hiển thị các đội đã vượt qua vòng bảng
     */
    private void displayQualifiedTeams(List<Team> qualifiedTeams) throws SQLException {
        // Lấy group standings để hiển thị
        Map<String, List<Team>> groupStandings = tournamentService.getAllGroupStandingsCalculatedInJava(currentTournamentId);
        
        List<Team> firstPlaceTeams = new ArrayList<>();
        List<Team> secondPlaceTeams = new ArrayList<>();
        
        for (Map.Entry<String, List<Team>> entry : groupStandings.entrySet()) {
            String groupName = entry.getKey();
            List<Team> teams = entry.getValue();
            
            if (teams.size() >= 2) {
                System.out.println("🥇 Nhất Bảng " + groupName + ": " + teams.get(0).getName());
                System.out.println("🥈 Nhì Bảng " + groupName + ": " + teams.get(1).getName());
                
                firstPlaceTeams.add(teams.get(0));
                secondPlaceTeams.add(teams.get(1));
            }
        }

        // Tạo danh sách đội vượt qua với ghép đôi vòng 16 đội phù hợp
        List<Team> roundOf16Teams = createRoundOf16Pairings(firstPlaceTeams, secondPlaceTeams);

        System.out.println("✅ " + roundOf16Teams.size() + " đội đã vào vòng loại trực tiếp");
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

        // Không cần cập nhật trạng thái giải đấu nữa vì đã xóa cột status

        // Vòng 16 đội
        List<Team> quarterFinalists = runKnockoutRound(qualifiedTeams, "ROUND_16");

        // Tứ kết
        List<Team> semiFinalists = runKnockoutRound(quarterFinalists, "QUARTER");

        // Bán kết
        List<Team> finalists = runKnockoutRound(semiFinalists, "SEMI");

        // Xác định 2 đội thua bán kết (đồng hạng 3 theo quy định FIFA mới)
        List<Team> thirdPlaceTeams = getThirdPlaceTeams(semiFinalists, finalists);
        if (thirdPlaceTeams.size() == 2) {
            System.out.println("🥉 Hai đội đồng hạng 3 (thua bán kết):");
            System.out.println("   " + thirdPlaceTeams.get(0).getName());
            System.out.println("   " + thirdPlaceTeams.get(1).getName());
        }

        // Chung kết
        if (finalists.size() == 2) {
            System.out.println("🏆 Trận chung kết:");
            Team champion = runKnockoutMatch(finalists.get(0), finalists.get(1), "FINAL");
            System.out.println("   Đội vô địch: " + champion.getName());

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
        
        // Format date as yyyy/mm/dd
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");
        String matchDate = dateFormat.format(new java.util.Date(System.currentTimeMillis() + random.nextInt(1000000000)));

        // Thêm trận đấu (removed status column)
        String matchSql = """
                    INSERT INTO matches (team_a_id, team_b_id, team_a_score, team_b_score, match_type, 
                                       match_date, venue, referee)
                    VALUES ((SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                            (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                            ?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(matchSql);
        pstmt.setString(1, teamA.getName());
        pstmt.setInt(2, currentTournamentId);
        pstmt.setString(3, teamB.getName());
        pstmt.setInt(4, currentTournamentId);
        pstmt.setInt(5, teamAScore);
        pstmt.setInt(6, teamBScore);
        pstmt.setString(7, matchType);
        pstmt.setString(8, matchDate);
        pstmt.setString(9, venue);
        pstmt.setString(10, referee);
        pstmt.executeUpdate();
        pstmt.close();

        int matchId = dbManager.getLastInsertId();

        // Tạo các sự kiện trận đấu
        generateMatchEvents(matchId, teamA, teamB, teamAScore, teamBScore);

        System.out.println("  " + teamA.getName() + " " + teamAScore + " - " + teamBScore + " " + teamB.getName());

        return winner;
    }

    private List<Team> getThirdPlaceTeams(List<Team> semiFinalists, List<Team> finalists) {
        List<Team> thirdPlaceTeams = new ArrayList<>();
        
        System.out.println("🔍 Debug - Tìm đội hạng 3:");
        System.out.println("   Số đội bán kết: " + semiFinalists.size());
        System.out.println("   Số đội chung kết: " + finalists.size());
        
        for (Team team : semiFinalists) {
            System.out.println("   Kiểm tra đội: " + team.getName());
            if (!finalists.contains(team)) {
                thirdPlaceTeams.add(team);
                System.out.println("     ✅ Thêm vào danh sách hạng 3: " + team.getName());
            } else {
                System.out.println("     ❌ Đội này vào chung kết: " + team.getName());
            }
        }
        
        System.out.println("   Tổng số đội hạng 3: " + thirdPlaceTeams.size());
        return thirdPlaceTeams;
    }

    private void updateTournamentResults(Team champion, List<Team> finalists, List<Team> thirdPlaceTeams) throws SQLException {
        Team runnerUp = finalists.stream().filter(t -> !t.equals(champion)).findFirst().orElse(null);
        
        System.out.println("🏆 Cập nhật kết quả cuối cùng:");
        System.out.println("   Champion: " + champion.getName());
        System.out.println("   Runner-up: " + (runnerUp != null ? runnerUp.getName() : "N/A"));
        
        // Theo quy định FIFA mới: 2 đội thua bán kết đồng hạng 3
        if (thirdPlaceTeams.size() == 2) {
            System.out.println("   Third place (đồng hạng): " + thirdPlaceTeams.get(0).getName() + " & " + thirdPlaceTeams.get(1).getName());
        }

        // Đảm bảo tournament_stats record tồn tại
        ensureTournamentStatsRecord();

        // Sử dụng TournamentService để cập nhật winners
        Integer championId = getTeamId(champion.getName());
        Integer runnerUpId = runnerUp != null ? getTeamId(runnerUp.getName()) : null;
        
        // Lưu cả 2 đội đồng hạng 3 vào DB (sử dụng 2 cột mới)
        Integer thirdPlaceId01 = null;
        Integer thirdPlaceId02 = null;
        
        if (thirdPlaceTeams.size() >= 1) {
            thirdPlaceId01 = getTeamId(thirdPlaceTeams.get(0).getName());
        }
        if (thirdPlaceTeams.size() >= 2) {
            thirdPlaceId02 = getTeamId(thirdPlaceTeams.get(1).getName());
        }

        System.out.println("🔍 Debug - Cập nhật DB:");
        System.out.println("   Tournament ID: " + currentTournamentId);
        System.out.println("   Champion ID: " + championId);
        System.out.println("   Runner-up ID: " + runnerUpId);
        System.out.println("   Third place ID 01: " + thirdPlaceId01);
        System.out.println("   Third place ID 02: " + thirdPlaceId02);

        tournamentService.updateTournamentWinners(currentTournamentId, championId, runnerUpId, thirdPlaceId01, thirdPlaceId02);
        System.out.println("✅ Đã gọi updateTournamentWinners với 2 đội đồng hạng 3");
    }



    /**
     * Lấy team ID từ tên team
     */
    private Integer getTeamId(String teamName) throws SQLException {
        String sql = "SELECT id FROM teams WHERE name = ? AND tournament_id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, currentTournamentId);
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
     * Đảm bảo tournament_stats record tồn tại
     */
    private void ensureTournamentStatsRecord() throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM tournament_stats WHERE tournament_id = ?";
        PreparedStatement checkPstmt = dbManager.getConnection().prepareStatement(checkSql);
        checkPstmt.setInt(1, currentTournamentId);
        ResultSet rs = checkPstmt.executeQuery();

        boolean exists = rs.next() && rs.getInt(1) > 0;
        rs.close();
        checkPstmt.close();

        if (!exists) {
            String insertSql = """
                INSERT INTO tournament_stats (tournament_id, total_goals, total_matches, 
                                            total_yellow_cards, total_red_cards, total_substitutions, 
                                            top_scorer_id, top_scorer_goals)
                VALUES (?, 0, 0, 0, 0, 0, NULL, 0)
            """;

            PreparedStatement insertPstmt = dbManager.getConnection().prepareStatement(insertSql);
            insertPstmt.setInt(1, currentTournamentId);
            insertPstmt.executeUpdate();
            insertPstmt.close();

            System.out.println("✅ Đã tạo tournament_stats record cho tournament ID: " + currentTournamentId);
        }
    }

    /**
     * Phương thức này đã được loại bỏ vì cột status không còn tồn tại
     */
    // private void updateTournamentStatus(String status) throws SQLException {
    //     // Đã xóa cột status khỏi bảng tournaments
    // }

    private void generateTournamentStatistics() throws SQLException {
        System.out.println("📊 Đang tạo thống kê giải đấu...");

        // Sử dụng TournamentService để tính toán thống kê bằng Java
        TournamentService.TournamentStats stats = tournamentService.calculateTournamentStats(currentTournamentId);

        // Kiểm tra xem tournament_stats đã tồn tại chưa
        String checkSql = "SELECT COUNT(*) FROM tournament_stats WHERE tournament_id = ?";
        PreparedStatement checkPstmt = dbManager.getConnection().prepareStatement(checkSql);
        checkPstmt.setInt(1, currentTournamentId);
        ResultSet rs = checkPstmt.executeQuery();

        boolean exists = rs.next() && rs.getInt(1) > 0;
        rs.close();
        checkPstmt.close();

        if (!exists) {
            // Lưu thống kê vào database nếu chưa tồn tại
            saveTournamentStatsToDatabase(stats);
        } else {
            // Cập nhật thống kê nếu đã tồn tại
            updateTournamentStatsInDatabase(stats);
        }

        System.out.println("✅ Đã cập nhật thống kê giải đấu");
    }
    
    /**
     * Lưu thống kê tournament vào database
     */
    private void saveTournamentStatsToDatabase(TournamentService.TournamentStats stats) throws SQLException {
        String insertStatsSql = """
                    INSERT INTO tournament_stats (tournament_id, total_goals, total_matches, 
                                                total_yellow_cards, total_red_cards, total_substitutions,
                                                top_scorer_id, top_scorer_goals)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement insertStatsPstmt = dbManager.getConnection().prepareStatement(insertStatsSql);
        insertStatsPstmt.setInt(1, currentTournamentId);
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
    private void updateTournamentStatsInDatabase(TournamentService.TournamentStats stats) throws SQLException {
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
        updateStatsPstmt.setInt(8, currentTournamentId);
        updateStatsPstmt.executeUpdate();
        updateStatsPstmt.close();
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
                        ts.total_goals,
                        ts.total_matches,
                        ts.top_scorer_id,
                        ts.top_scorer_goals,
                        top_scorer.name as top_scorer_name
                    FROM tournaments t
                    LEFT JOIN tournament_stats ts ON t.id = ts.tournament_id
                    LEFT JOIN teams champion ON ts.champion_id = champion.id
                    LEFT JOIN teams runner_up ON ts.runner_up_id = runner_up.id
                    LEFT JOIN players top_scorer ON ts.top_scorer_id = top_scorer.id
                    WHERE t.id = ?
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(resultsSql);
        pstmt.setInt(1, currentTournamentId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            System.out.println("🏆 VÔ ĐỊCH: " + rs.getString("champion"));
            System.out.println("🥈 Á QUÂN: " + rs.getString("runner_up"));
            
            // Hiển thị 2 đội đồng hạng 3 (theo quy định FIFA mới)
            displayThirdPlaceTeams();
            System.out.println();
            System.out.println("📊 THỐNG KÊ GIẢI ĐẤU:");
            System.out.println("   Tổng số trận: " + rs.getInt("total_matches"));
            System.out.println("   Tổng số bàn thắng: " + rs.getInt("total_goals"));
            
            // Hiển thị top scorer từ tournament_stats
            String topScorerName = rs.getString("top_scorer_name");
            int topScorerGoals = rs.getInt("top_scorer_goals");
            
            if (topScorerName != null && topScorerGoals > 0) {
                System.out.println("   Vua phá lưới: " + topScorerName + " (" + topScorerGoals + " bàn thắng)");
            } else {
                System.out.println("   Vua phá lưới: Chưa có bàn thắng nào");
            }
        }

        rs.close();
        pstmt.close();

        // Hiển thị bảng xếp hạng cuối vòng bảng
        System.out.println("\n📋 BẢNG XẾP HẠNG CUỐI VÒNG BẢNG:");
        displayGroupStandings();

        System.out.println("\n" + "=".repeat(50));
    }

    private void displayGroupStandings() throws SQLException {
        // Sử dụng TournamentService để lấy group standings đã được sắp xếp bằng Java
        Map<String, List<Team>> groupStandings = tournamentService.getAllGroupStandingsCalculatedInJava(currentTournamentId);
        
        // Sắp xếp tên bảng theo thứ tự A, B, C...
        List<String> sortedGroupNames = new ArrayList<>(groupStandings.keySet());
        Collections.sort(sortedGroupNames);
        
        for (String groupName : sortedGroupNames) {
            System.out.println("\nBảng " + groupName + ":");
            List<Team> teams = groupStandings.get(groupName);
            
            int position = 1;
            for (Team team : teams) {
                String status = position <= 2 ? " ✅" : "";
                
                // Lấy thống kê chi tiết từ database (chỉ để hiển thị)
                TeamDisplayStats stats = getTeamDisplayStats(team.getName(), currentTournamentId);
                
                System.out.printf("  %d. %-15s %2d pts (%d-%d-%d) %2d:%2d (%+d)%s%n",
                        position,
                        team.getName(),
                        team.getPoints(),
                        stats.wins,
                        stats.draws,
                        stats.losses,
                        stats.goalsFor,
                        stats.goalsAgainst,
                        team.getGoalDifference(),
                        status
                );
                position++;
            }
        }
    }
    
    /**
     * Lấy thống kê hiển thị cho team (chỉ để hiển thị, không dùng cho logic)
     */
    private TeamDisplayStats getTeamDisplayStats(String teamName, int tournamentId) throws SQLException {
        // Tính toán thống kê từ matches thay vì lấy từ teams table
        String sql = """
            SELECT 
                t.id as team_id,
                SUM(CASE 
                    WHEN (m.team_a_id = t.id AND m.team_a_score > m.team_b_score) OR 
                         (m.team_b_id = t.id AND m.team_b_score > m.team_a_score) 
                    THEN 1 ELSE 0 END) as wins,
                SUM(CASE 
                    WHEN m.team_a_score = m.team_b_score AND (m.team_a_score > 0 OR m.team_b_score > 0 OR m.team_a_score = 0)
                    THEN 1 ELSE 0 END) as draws,
                SUM(CASE 
                    WHEN (m.team_a_id = t.id AND m.team_a_score < m.team_b_score) OR 
                         (m.team_b_id = t.id AND m.team_b_score < m.team_a_score) 
                    THEN 1 ELSE 0 END) as losses,
                SUM(CASE 
                    WHEN m.team_a_id = t.id THEN m.team_a_score 
                    WHEN m.team_b_id = t.id THEN m.team_b_score 
                    ELSE 0 END) as goals_for,
                SUM(CASE 
                    WHEN m.team_a_id = t.id THEN m.team_b_score 
                    WHEN m.team_b_id = t.id THEN m.team_a_score 
                    ELSE 0 END) as goals_against
            FROM teams t
            LEFT JOIN matches m ON (t.id = m.team_a_id OR t.id = m.team_b_id) AND (m.team_a_score >= 0 AND m.team_b_score >= 0)
            WHERE t.name = ? AND t.tournament_id = ?
            GROUP BY t.id
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, teamName);
        pstmt.setInt(2, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        TeamDisplayStats stats = new TeamDisplayStats();
        if (rs.next()) {
            stats.wins = rs.getInt("wins");
            stats.draws = rs.getInt("draws");
            stats.losses = rs.getInt("losses");
            stats.goalsFor = rs.getInt("goals_for");
            stats.goalsAgainst = rs.getInt("goals_against");
        }
        
        rs.close();
        pstmt.close();
        
        return stats;
    }
    
    /**
     * Hiển thị 2 đội đồng hạng 3 (theo quy định FIFA mới)
     */
    private void displayThirdPlaceTeams() throws SQLException {
        // Tìm 2 đội thua bán kết
        String sql = """
            SELECT 
                CASE 
                    WHEN m.team_a_score > m.team_b_score THEN tb.name
                    ELSE ta.name
                END as loser_name
            FROM matches m
            JOIN teams ta ON m.team_a_id = ta.id
            JOIN teams tb ON m.team_b_id = tb.id
            WHERE ta.tournament_id = ? AND tb.tournament_id = ?
            AND m.match_type = 'SEMI' AND (m.team_a_score >= 0 AND m.team_b_score >= 0)
            ORDER BY m.id
        """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, currentTournamentId);
        pstmt.setInt(2, currentTournamentId);
        ResultSet rs = pstmt.executeQuery();

        List<String> thirdPlaceTeams = new ArrayList<>();
        while (rs.next()) {
            thirdPlaceTeams.add(rs.getString("loser_name"));
        }

        rs.close();
        pstmt.close();

        if (thirdPlaceTeams.size() == 2) {
            System.out.println("🥉 ĐỒNG HẠNG BA: " + thirdPlaceTeams.get(0) + " & " + thirdPlaceTeams.get(1));
        } else if (thirdPlaceTeams.size() == 1) {
            System.out.println("🥉 HẠNG BA: " + thirdPlaceTeams.get(0));
        } else {
            // Fallback: lấy từ tournament_stats với 2 cột mới
            String fallbackSql = """
                SELECT t1.name as team1_name, t2.name as team2_name
                FROM tournament_stats ts
                LEFT JOIN teams t1 ON ts.third_place_id_01 = t1.id
                LEFT JOIN teams t2 ON ts.third_place_id_02 = t2.id
                WHERE ts.tournament_id = ?
            """;
            PreparedStatement fallbackStmt = dbManager.getConnection().prepareStatement(fallbackSql);
            fallbackStmt.setInt(1, currentTournamentId);
            ResultSet fallbackRs = fallbackStmt.executeQuery();
            
            if (fallbackRs.next()) {
                String team1 = fallbackRs.getString("team1_name");
                String team2 = fallbackRs.getString("team2_name");
                
                if (team1 != null && team2 != null) {
                    System.out.println("🥉 ĐỒNG HẠNG BA: " + team1 + " & " + team2);
                } else if (team1 != null) {
                    System.out.println("🥉 HẠNG BA: " + team1);
                } else if (team2 != null) {
                    System.out.println("🥉 HẠNG BA: " + team2);
                } else {
                    System.out.println("🥉 HẠNG BA: Chưa xác định");
                }
            } else {
                System.out.println("🥉 HẠNG BA: Chưa xác định");
            }
            
            fallbackRs.close();
            fallbackStmt.close();
        }
    }

    /**
     * Inner class để lưu thống kê hiển thị
     */
    private static class TeamDisplayStats {
        int wins, draws, losses, goalsFor, goalsAgainst;
    }

    /**
     * Create a new tournament with sample data
     * Demonstrates OOP principles with proper object creation
     */
    public void createNewTournament() {
        try {
            System.out.println("🏆 Creating new tournament...");
            
            // Create tournament using Java object creation (OOP)
            int year = DataGenerator.getRandomWorldCupYear();
            String hostCountry = DataGenerator.getRandomHostCountry();
            String[] dates = DataGenerator.generateTournamentDates(year);
            
            String tournamentName = "World Cup " + year + " - " + hostCountry;
            
            // Insert tournament into database
            String sql = "INSERT INTO tournaments (name, year, host_country, start_date, end_date) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, tournamentName);
            pstmt.setInt(2, year);
            pstmt.setString(3, hostCountry);
            pstmt.setString(4, dates[0]);
            pstmt.setString(5, dates[1]);
            
            pstmt.executeUpdate();
            
            // Get generated tournament ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                currentTournamentId = rs.getInt(1);
                System.out.println("✅ Tournament created: " + tournamentName + " (ID: " + currentTournamentId + ")");
                
                // Create groups and teams using OOP
                createGroupsAndTeams();
                
                // Generate some sample matches
                generateSampleMatches();
                
                System.out.println("✅ Tournament setup completed!");
            }
            
            rs.close();
            pstmt.close();
            
        } catch (Exception e) {
            System.err.println("❌ Error creating tournament: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create groups and teams using OOP principles
     */
    private void createGroupsAndTeams() throws Exception {
        String[] groupNames = DataGenerator.getGroupsName();
        
        for (String groupName : groupNames) {
            // Create group
            String groupSql = "INSERT INTO groups (name, tournament_id) VALUES (?, ?)";
            PreparedStatement groupStmt = dbManager.getConnection().prepareStatement(groupSql, PreparedStatement.RETURN_GENERATED_KEYS);
            groupStmt.setString(1, groupName);
            groupStmt.setInt(2, currentTournamentId);
            groupStmt.executeUpdate();
            
            ResultSet groupRs = groupStmt.getGeneratedKeys();
            int groupId = 0;
            if (groupRs.next()) {
                groupId = groupRs.getInt(1);
            }
            groupRs.close();
            groupStmt.close();
            
            // Create 4 teams per group using OOP
            List<Team> groupTeams = DataGenerator.generateTeams(4);
            for (Team team : groupTeams) {
                // Insert team
                String teamSql = """
                    INSERT INTO teams (name, region, coach, medical_staff, is_host, 
                                     group_id, tournament_id) 
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
                
                PreparedStatement teamStmt = dbManager.getConnection().prepareStatement(teamSql, PreparedStatement.RETURN_GENERATED_KEYS);
                teamStmt.setString(1, team.getName());
                teamStmt.setString(2, team.getRegion());
                teamStmt.setString(3, team.getCoach());
                teamStmt.setString(4, team.getMedicalStaff());
                teamStmt.setBoolean(5, team.isHost());
                teamStmt.setInt(6, groupId);
                teamStmt.setInt(7, currentTournamentId);
                
                teamStmt.executeUpdate();
                
                ResultSet teamRs = teamStmt.getGeneratedKeys();
                int teamId = 0;
                if (teamRs.next()) {
                    teamId = teamRs.getInt(1);
                }
                teamRs.close();
                teamStmt.close();
                
                // Insert players for this team
                for (Player player : team.getPlayers()) {
                    String playerSql = """
                        INSERT INTO players (name, jersey_number, position, goals, 
                                           yellow_cards, red_cards, is_starting, 
                                           is_eligible, team_id) 
                        VALUES (?, ?, ?, 0, 0, 0, ?, true, ?)
                    """;
                    
                    PreparedStatement playerStmt = dbManager.getConnection().prepareStatement(playerSql);
                    playerStmt.setString(1, player.getName());
                    playerStmt.setInt(2, player.getJerseyNumber());
                    playerStmt.setString(3, player.getPosition());
                    playerStmt.setBoolean(4, player.isStarting());
                    playerStmt.setInt(5, teamId);
                    
                    playerStmt.executeUpdate();
                    playerStmt.close();
                }
            }
        }
        
        System.out.println("✅ Created 8 groups with 32 teams and players");
    }
    
    /**
     * Generate sample matches using OOP
     */
    private void generateSampleMatches() throws Exception {
        // Get all teams for this tournament
        String sql = "SELECT id, name FROM teams WHERE tournament_id = ?";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, currentTournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        List<Integer> teamIds = new ArrayList<>();
        while (rs.next()) {
            teamIds.add(rs.getInt("id"));
        }
        rs.close();
        pstmt.close();
        
        // Generate some group stage matches
        for (int i = 0; i < teamIds.size() - 1; i += 2) {
            if (i + 1 < teamIds.size()) {
                int teamA = teamIds.get(i);
                int teamB = teamIds.get(i + 1);
                
                // Generate match score using Java logic
                int[] score = DataGenerator.generateMatchScore();
                
                String matchSql = """
                    INSERT INTO matches (team_a_id, team_b_id, team_a_score, team_b_score, 
                                       match_type, match_date, venue, referee) 
                    VALUES (?, ?, ?, ?, 'GROUP', datetime('now'), ?, ?)
                """;
                
                PreparedStatement matchStmt = dbManager.getConnection().prepareStatement(matchSql);
                matchStmt.setInt(1, teamA);
                matchStmt.setInt(2, teamB);
                matchStmt.setInt(3, score[0]);
                matchStmt.setInt(4, score[1]);
                matchStmt.setString(5, "Stadium " + (i/2 + 1));
                matchStmt.setString(6, "Referee " + (i/2 + 1));
                
                matchStmt.executeUpdate();
                matchStmt.close();
                
                // Update team statistics using Java calculations (OOP)
                updateTeamStatsAfterMatch(teamA, score[0], score[1]);
                updateTeamStatsAfterMatch(teamB, score[1], score[0]);
            }
        }
        
        System.out.println("✅ Generated sample matches with calculated results");
    }
    
    /**
     * Update team statistics using Java calculations (OOP principle)
     */
    private void updateTeamStatsAfterMatch(int teamId, int goalsFor, int goalsAgainst) throws Exception {
        // Calculate points using Java logic (not SQL)
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
        
        int goalDifference = goalsFor - goalsAgainst;
        
        // Update using Java-calculated values
        String sql = """
            UPDATE teams SET 
                points = points + ?, 
                goals_for = goals_for + ?, 
                goals_against = goals_against + ?, 
                goal_difference = goals_for - goals_against,
                wins = wins + ?, 
                draws = draws + ?, 
                losses = losses + ?
            WHERE id = ?
        """;
        
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, points);
        pstmt.setInt(2, goalsFor);
        pstmt.setInt(3, goalsAgainst);
        pstmt.setInt(4, wins);
        pstmt.setInt(5, draws);
        pstmt.setInt(6, losses);
        pstmt.setInt(7, teamId);
        
        pstmt.executeUpdate();
        pstmt.close();
    }

    /**
     * Method để random hóa tournament hiện tại sau khi tạo
     */
    public void randomizeCurrentTournament() {
        try {
            if (currentTournamentId > 0) {
                System.out.println("🎲 Đang random hóa tournament hiện tại...");

                // Lấy tên tournament hiện tại
                String selectSql = "SELECT name FROM tournaments WHERE id = ?";
                PreparedStatement selectStmt = dbManager.getConnection().prepareStatement(selectSql);
                selectStmt.setInt(1, currentTournamentId);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    String tournamentName = rs.getString("name");
                    // DataGenerator.randomizeTournament(currentTournamentId, tournamentName); // TODO: Implement
                    System.out.println("✅ Đã random hóa tournament thành công!");
                }

                rs.close();
                selectStmt.close();
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi random hóa tournament: " + e.getMessage());
        }
    }


    /**
     * Tính toán lại tất cả tournament stats
     */
    public void recalculateAllTournamentStats() {
        System.out.println("📊 Đang tính toán lại tất cả tournament stats...");
        statsCalculator.recalculateAllTournamentStats();
    }

    /**
     * Tính toán lại stats cho tournament hiện tại
     */
    public void recalculateCurrentTournamentStats() {
        try {
            if (currentTournamentId > 0) {
                System.out.println("📊 Đang tính toán stats cho tournament hiện tại...");

                // Lấy tên tournament hiện tại
                String selectSql = "SELECT name FROM tournaments WHERE id = ?";
                PreparedStatement selectStmt = dbManager.getConnection().prepareStatement(selectSql);
                selectStmt.setInt(1, currentTournamentId);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    String tournamentName = rs.getString("name");
                    statsCalculator.recalculateTournamentStats(currentTournamentId, tournamentName);
                    System.out.println("✅ Đã tính toán stats cho tournament hiện tại!");
                }

                rs.close();
                selectStmt.close();
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tính toán stats cho tournament hiện tại: " + e.getMessage());
        }
    }

    /**
     * Hiển thị tất cả tournament stats
     */
    public void displayAllTournamentStats() {
        statsCalculator.displayAllTournamentStats();
    }

    /**
     * Tính lại goal_difference cho tất cả teams
     * goal_difference = goals_for - goals_against
     */
    public void recalculateAllGoalDifference() {
        try {
            System.out.println("⚽ Đang tính lại goal_difference cho tất cả teams...");

            String sql = """
                        UPDATE teams 
                        SET goal_difference = goals_for - goals_against
                        WHERE goals_for IS NOT NULL AND goals_against IS NOT NULL
                    """;

            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            int updatedRows = pstmt.executeUpdate();
            pstmt.close();

            System.out.println("✅ Đã cập nhật goal_difference cho " + updatedRows + " teams!");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tính lại goal_difference: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tính lại goal_difference cho tournament hiện tại
     */
    public void recalculateCurrentTournamentGoalDifference() {
        try {
            if (currentTournamentId > 0) {
                System.out.println("⚽ Đang tính lại goal_difference cho tournament hiện tại...");

                String sql = """
                            UPDATE teams 
                            SET goal_difference = goals_for - goals_against
                            WHERE tournament_id = ? 
                            AND goals_for IS NOT NULL 
                            AND goals_against IS NOT NULL
                        """;

                PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
                pstmt.setInt(1, currentTournamentId);
                int updatedRows = pstmt.executeUpdate();
                pstmt.close();

                System.out.println("✅ Đã cập nhật goal_difference cho " + updatedRows + " teams trong tournament hiện tại!");

            } else {
                System.out.println("⚠️ Không có tournament hiện tại để cập nhật!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tính lại goal_difference cho tournament hiện tại: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị thống kê goal_difference của tất cả teams
     */
    public void displayGoalDifferenceStats() {
        try {
            System.out.println("📊 THỐNG KÊ GOAL DIFFERENCE CỦA TẤT CẢ TEAMS");
            System.out.println("=".repeat(80));

            String sql = """
                        SELECT t.name as team_name, 
                               tour.name as tournament_name,
                               t.goals_for, 
                               t.goals_against, 
                               t.goal_difference,
                               t.points,
                               t.wins, t.draws, t.losses
                        FROM teams t
                        JOIN tournaments tour ON t.tournament_id = tour.id
                        ORDER BY t.goal_difference DESC, t.points DESC, t.goals_for DESC
                    """;

            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            System.out.printf("%-25s %-20s %4s %4s %4s %6s %3s %3s %3s%n",
                    "TEAM", "TOURNAMENT", "GF", "GA", "GD", "PTS", "W", "D", "L");
            System.out.println("-".repeat(80));

            while (rs.next()) {
                System.out.printf("%-25s %-20s %4d %4d %+4d %6d %3d %3d %3d%n",
                        rs.getString("team_name"),
                        rs.getString("tournament_name"),
                        rs.getInt("goals_for"),
                        rs.getInt("goals_against"),
                        rs.getInt("goal_difference"),
                        rs.getInt("points"),
                        rs.getInt("wins"),
                        rs.getInt("draws"),
                        rs.getInt("losses")
                );
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi hiển thị thống kê goal_difference: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        if (dbManager != null) {
            dbManager.close();
        }
    }
}