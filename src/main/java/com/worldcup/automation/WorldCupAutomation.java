package com.worldcup.automation;

import com.worldcup.calculator.TournamentStatsCalculator;
import com.worldcup.database.DatabaseManager;
import com.worldcup.generator.DataGenerator;
import com.worldcup.manager.ObjectManager;
import com.worldcup.model.*;
import com.worldcup.service.TeamService;
import com.worldcup.service.TournamentService;
import com.worldcup.service.PlayerService;
import com.worldcup.service.OOPMatchService;
//import com.worldcup.validator.StatisticsValidator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class WorldCupAutomation {
    private DatabaseManager dbManager;
    private ObjectManager objectManager; // NEW: OOP Manager
    // DataGenerator has only static methods, no instance needed
    private TournamentStatsCalculator statsCalculator;
    private TeamService teamService;
    private TournamentService tournamentService;
    private PlayerService playerService;
    private OOPMatchService oopMatchService; // NEW: OOP Match Service
    private List<Team> teams;
    private List<Match> matches;
    private List<Group> groups;
    private KnockoutStageManager knockoutManager;
    private int currentTournamentId;
    private Random random = new Random();

    public WorldCupAutomation() {
        this.dbManager = new DatabaseManager();
        this.objectManager = ObjectManager.getInstance(dbManager); // NEW: Initialize OOP Manager
        // DataGenerator uses static methods only
        this.statsCalculator = new TournamentStatsCalculator(dbManager);
        this.teamService = new TeamService(dbManager);
        this.tournamentService = new TournamentService(dbManager);
        this.playerService = new PlayerService(dbManager);
        this.oopMatchService = new OOPMatchService(objectManager); // NEW: Initialize OOP Match Service

        this.teams = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.matches = new ArrayList<>();
        this.knockoutManager = new KnockoutStageManager();
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

    

        } catch (Exception e) {
            System.err.println("❌ Lỗi trong quá trình mô phỏng World Cup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
 
    private void createTournament() throws SQLException {

        int randomYear = DataGenerator.getRandomWorldCupYear(); // trả về 1 năm
        String randomHostCountry = DataGenerator.getRandomHostCountry(); // trả về 1 địa điểm
        String[] randomDates = DataGenerator.generateTournamentDates(randomYear); // trả về yyyy/MM/dd
        String name = DataGenerator.generateTournamentName(randomYear); // trả về tên giải đấu

        System.out.println(name);
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

    private void generateTeams() throws Exception {
        System.out.println("Tạo 32 đội bóng...");

        teams = DataGenerator.generateRandomTeams(32);

        // Reset tất cả thống kê về 0 trước khi bắt đầu tournament
        for (Team team : teams) {
            team.resetStatistics();
            insertTeamToDatabase(team);
        }
    }

    /**
     * NEW OOP APPROACH: Insert team using ObjectManager and repositories
     */
    private void insertTeamToDatabase(Team team) throws Exception {
        // Set tournament ID for team
        team.setTournamentId(currentTournamentId);
        
        // Save team using OOP approach - repository handles all database operations
        objectManager.saveTeamComplete(team);
        
        
    }
    
    /**
     * LEGACY METHOD - kept for backward compatibility
     */
    private void insertTeamToDatabaseLegacy(Team team) throws SQLException {
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
        
        // Set the player ID from database
        int playerId = dbManager.getLastInsertId();
        player.setId(playerId);
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

            // Cập nhật đội với bảng được phân - sử dụng team ID thay vì name để đảm bảo chính xác
            String sql = "UPDATE teams SET group_id = ? WHERE id = ?";
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, teams.get(i).getId());
            int rowsUpdated = pstmt.executeUpdate();
            pstmt.close();


            // Cập nhật group ID trong team object
            teams.get(i).setGroupId(groupId);

            // Thêm đội vào đối tượng bảng
            groups.get(groupIndex).addTeam(teams.get(i));
        }
    }

    private void runGroupStage() throws Exception {
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
    }

    /**
     * NEW OOP APPROACH: Simulate match using OOPMatchService
     */
    private void simulateMatch(Team teamA, Team teamB, String matchType, int groupId) throws Exception {
        // Generate match score
        int[] score = DataGenerator.generateMatchScore();
        int teamAScore = score[0];
        int teamBScore = score[1];
        
        String venue = DataGenerator.getRandomVenue();
        String referee = DataGenerator.getRandomReferee();
        boolean isKnockout = !matchType.equals("GROUP");
        
        // Create match using OOP service
        Match match = oopMatchService.createMatch(teamA, teamB, venue, referee, isKnockout);
        
        // Update match result using OOP service
        oopMatchService.updateMatchResult(match, teamAScore, teamBScore);
        
        // Generate match events using OOP service
        oopMatchService.generateMatchEvents(match, teamAScore, teamBScore);
        
        System.out.println("  ⚽ " + teamA.getName() + " " + teamAScore + " - " + teamBScore + " " + teamB.getName());
    }
    
    /**
     * LEGACY METHOD: kept for backward compatibility
     */
    @Deprecated
    private void simulateMatchLegacy(Team teamA, Team teamB, String matchType, int groupId) throws Exception {
        // This method has been replaced by the OOP approach above
        System.out.println("⚠️ Warning: Using deprecated match simulation method.");
    }
    
    /**
     * Tạo Match object với đầy đủ thông tin
     */
    private Match createMatchObject(Team teamA, Team teamB, boolean isKnockout) {
        // Ensure teams have complete lineups
        if (teamA.getStartingPlayers().size() != 11 || teamB.getStartingPlayers().size() != 11) {
            System.out.println("   ⚠️ Incomplete lineups detected - fixing...");
            ensureTeamHasPlayers(teamA);
            ensureTeamHasPlayers(teamB);
        }
        
        return new Match(
            teamA, teamB, isKnockout
        );
    }
    
    /**
     * Lưu Match object vào database
     */
    private int saveMatchToDatabase(Match match, String venue, String referee) throws SQLException {
        String matchSql = """
                    INSERT INTO matches (team_a_id, team_b_id, team_a_score, team_b_score, match_type, 
                                       match_date, venue, referee)
                    VALUES ((SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                            (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                            ?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(matchSql);
        pstmt.setString(1, match.getTeamA().getName());
        pstmt.setInt(2, currentTournamentId);
        pstmt.setString(3, match.getTeamB().getName());
        pstmt.setInt(4, currentTournamentId);
        pstmt.setInt(5, match.getTeamAScore());
        pstmt.setInt(6, match.getTeamBScore());
        pstmt.setString(7, match.getMatchType());
        pstmt.setString(8, match.getMatchDate());
        pstmt.setString(9, venue);
        pstmt.setString(10, referee);
        pstmt.executeUpdate();
        pstmt.close();

        int matchId = dbManager.getLastInsertId();
        match.setId(matchId);
        
        return matchId;
    }


    private void generateMatchEvents(int matchId, Team teamA, Team teamB, int teamAScore, int teamBScore) throws Exception {
        // Tạo Match object để sử dụng trong events
        Match match = createMatchObject(teamA, teamB, false);
        match.setId(matchId);
        
        // Tạo bàn thắng cho đội A sử dụng Goal objects
        generateGoalsForTeamUsingObjects(match, teamA, teamAScore);

        // Tạo bàn thắng cho đội B sử dụng Goal objects
        generateGoalsForTeamUsingObjects(match, teamB, teamBScore);

        // Tạo thẻ và thay người sử dụng model objects
        generateCardsAndSubstitutionsUsingObjects(match, teamA, teamB);
    }

    /**
     * NEW OOP APPROACH: Tạo bàn thắng sử dụng ObjectManager
     */
    private void generateGoalsForTeamUsingObjects(Match match, Team team, int goalCount) throws Exception {
        for (int i = 0; i < goalCount; i++) {
            // Chọn cầu thủ ngẫu nhiên từ đội hình xuất phát
            List<Player> startingPlayers = team.getStartingPlayers();
            if (!startingPlayers.isEmpty()) {
                Player scorer = DataGenerator.getRandomElement(startingPlayers);
                int minute = DataGenerator.generateRandomMinute();

                // Tạo Goal object và lưu vào database sử dụng ObjectManager
                Goal goal = objectManager.createGoal(scorer, team, minute, match);
                
                // Thêm goal vào match object
                match.addGoal(goal);
                
            
            }
        }
    }
    
    /**
     * DEPRECATED: Legacy SQL methods - replaced by OOP approach
     * These methods are kept for backward compatibility but should not be used
     */
    @Deprecated
    private void saveGoalToDatabaseLegacy(Goal goal) throws SQLException {
        // This method has been replaced by objectManager.createGoal()
        // which handles both object creation and database persistence
        System.out.println("⚠️ Warning: Using deprecated SQL method. Use ObjectManager instead.");
    }
    
    @Deprecated
    private void updatePlayerGoalsInDatabaseLegacy(Player player, Team team) throws SQLException {
        // This method has been replaced by playerRepository.updateGoals()
        // which is called automatically by objectManager.createGoal()
        System.out.println("⚠️ Warning: Using deprecated SQL method. Use PlayerRepository instead.");
    }
    
    /**
     * Legacy method - giữ lại để tương thích
     */
    private void generateGoalsForTeam(int matchId, Team team, int goalCount) throws Exception {
        // Tạo temporary match object
        Match tempMatch = createMatchObject(team, team, false);
        tempMatch.setId(matchId);
        generateGoalsForTeamUsingObjects(tempMatch, team, goalCount);
    }

    /**
     * NEW OOP APPROACH: Tạo thẻ và thay người sử dụng ObjectManager
     */
    private void generateCardsAndSubstitutionsUsingObjects(Match match, Team teamA, Team teamB) throws Exception {
        // Tạo thẻ cho đội A sử dụng Card objects
        generateCardsForTeamUsingObjects(match, teamA);

        // Tạo thẻ cho đội B sử dụng Card objects
        generateCardsForTeamUsingObjects(match, teamB);

        // Tạo thay người cho đội A
        generateSubstitutionsForTeamUsingObjects(match, teamA);

        // Tạo thay người cho đội B
        generateSubstitutionsForTeamUsingObjects(match, teamB);
    }
    
    /**
     * NEW OOP APPROACH: Tạo thẻ phạt cho đội sử dụng ObjectManager
     */
    private void generateCardsForTeamUsingObjects(Match match, Team team) throws Exception {
        // Lấy tất cả cầu thủ của đội (chỉ đá chính)
        List<Player> startingPlayers = new ArrayList<>(team.getStartingPlayers());

        // Thẻ vàng
        if (DataGenerator.shouldHaveYellowCard()) {
            Player player = DataGenerator.getRandomElement(startingPlayers);
            if (player != null) {
                int minute = DataGenerator.generateRandomMinute();
                
                // Tạo Card object và lưu vào database sử dụng ObjectManager
                Card yellowCard = objectManager.createCard(player, team, match, minute, Card.CardType.YELLOW);
                
                // Thêm card vào match object
                match.addCard(player, team, "YELLOW");
                
                
            }
        }

        // Thẻ đỏ (ít phổ biến hơn)
        if (DataGenerator.shouldHaveRedCard()) {
            Player player = DataGenerator.getRandomElement(startingPlayers);
            if (player != null) {
                int minute = DataGenerator.generateRandomMinute();
                
                // Tạo Card object và lưu vào database sử dụng ObjectManager
                Card redCard = objectManager.createCard(player, team, match, minute, Card.CardType.RED);
                
                // Thêm card vào match object
                match.addCard(player, team, "RED");
                
                
            }
        }
    }
    
    /**
     * Lưu Card object vào database
     */
    private void saveCardToDatabase(Card card) throws SQLException {
        String sql = """
                    INSERT INTO cards (match_id, player_id, team_id, card_type, minute)
                    VALUES (?, (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)), 
                            (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), ?, ?)
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, card.getMatch().getId());
        pstmt.setString(2, card.getPlayer().getName());
        pstmt.setString(3, card.getTeam().getName());
        pstmt.setInt(4, currentTournamentId);
        pstmt.setString(5, card.getTeam().getName());
        pstmt.setInt(6, currentTournamentId);
        pstmt.setString(7, card.getType().getLabel().toUpperCase());
        pstmt.setInt(8, card.getMinutes());
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    /**
     * Cập nhật số thẻ của cầu thủ trong database
     */
    private void updatePlayerCardsInDatabase(Player player, Team team, String cardType) throws SQLException {
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
    
    /**
     * NEW OOP APPROACH: Tạo thay người cho đội sử dụng ObjectManager
     * Lưu ý: Không thực hiện thay người thực tế để không ảnh hưởng đến các trận đấu sau
     */
    private void generateSubstitutionsForTeamUsingObjects(Match match, Team team) throws Exception {
        if (DataGenerator.shouldHaveSubstitution()) {
            // Validate team has players loaded
            if (team.getStartingPlayers().isEmpty() || team.getSubstitutePlayers().isEmpty()) {
                System.out.println("   ⚠️ Team " + team.getName() + " không có đầy đủ thông tin cầu thủ để thay người");
                return;
            }
            
            int substitutionCount = random.nextInt(3) + 1; // 1-3 lần thay người

            for (int i = 0; i < substitutionCount && i < team.getSubstitutePlayers().size(); i++) {
                Player playerOut = DataGenerator.getRandomElement(team.getStartingPlayers());
                Player playerIn = DataGenerator.getRandomElement(team.getSubstitutePlayers());

                if (playerOut != null && playerIn != null) {
                    // Additional validation - ensure players have valid IDs and names
                    if (playerOut.getName() == null || playerOut.getName().trim().isEmpty()) {
                        System.out.println("   ⚠️ PlayerOut có tên không hợp lệ, bỏ qua thay người");
                        continue;
                    }
                    
                    if (playerIn.getName() == null || playerIn.getName().trim().isEmpty()) {
                        System.out.println("   ⚠️ PlayerIn có tên không hợp lệ, bỏ qua thay người");
                        continue;
                    }
                    
                    // Validate players exist in database before creating substitution
                    if (!validatePlayerExistsInDatabase(playerOut, team) || !validatePlayerExistsInDatabase(playerIn, team)) {
                        System.out.println("   ⚠️ Một trong hai cầu thủ không tồn tại trong database, bỏ qua thay người");
                        continue;
                    }
                    
                    int minute = DataGenerator.generateSubstitutionMinute();

                    // Tạo Substitution object và lưu vào database sử dụng ObjectManager
                    // Không thực hiện thay người thực tế để không ảnh hưởng đến các trận đấu sau
                    try {
                        Substitution substitution = objectManager.createSubstitution(match, team, playerIn, playerOut, minute);
                        
                        // Cập nhật số lần thay người cho team
                        team.setSubstitutionCount(team.getSubstitutionCount() + 1);
                        
                        
                        
                    } catch (Exception e) {
                        System.out.println("   Không thể thay người: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Lưu Substitution object vào database
     */
    private void saveSubstitutionToDatabase(Substitution substitution) throws SQLException {
        String sql = """
                    INSERT INTO substitutions (match_id, team_id, player_in_id, player_out_id, minute)
                    VALUES (?, (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                            (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)),
                            (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)), ?)
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, substitution.getMatch().getId());
        pstmt.setString(2, substitution.getTeam().getName());
        pstmt.setInt(3, currentTournamentId);
        pstmt.setString(4, substitution.getInPlayer().getName());
        pstmt.setString(5, substitution.getTeam().getName());
        pstmt.setInt(6, currentTournamentId);
        pstmt.setString(7, substitution.getOutPlayer().getName());
        pstmt.setString(8, substitution.getTeam().getName());
        pstmt.setInt(9, currentTournamentId);
        pstmt.setInt(10, substitution.getMinute());
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    /**
     * Lưu thay người trực tiếp vào database mà không tạo Substitution object
     */
    private void saveSubstitutionToDatabase(Match match, Team team, Player playerIn, Player playerOut, int minute) throws SQLException {
        String sql = """
                    INSERT INTO substitutions (match_id, team_id, player_in_id, player_out_id, minute)
                    VALUES (?, (SELECT id FROM teams WHERE name = ? AND tournament_id = ?), 
                            (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)),
                            (SELECT id FROM players WHERE name = ? AND team_id = (SELECT id FROM teams WHERE name = ? AND tournament_id = ?)), ?)
                """;

        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, match.getId());
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
    
    /**
     * Đồng bộ thống kê Team object với database
     * Lưu ý: Bảng teams không có các cột thống kê, thống kê được tính toán trong Java
     * Method này được giữ lại để tương thích, nhưng không thực hiện gì
     */
    private void syncTeamStatisticsToDatabase(Team team) throws SQLException {
        // Bảng teams trong database không có các cột thống kê như points, goal_difference, v.v.
        // Các thống kê này được tính toán trực tiếp trong Java thông qua Team objects
        // và được sử dụng bởi các Service classes để tính toán kết quả
        
        // Không cần cập nhật database vì thống kê được tính toán động
        // Chỉ cần đảm bảo Team objects có dữ liệu chính xác
    }
    
    /**
     * Legacy method - giữ lại để tương thích
     */
    private void generateCardsAndSubstitutions(int matchId, Team teamA, Team teamB) throws Exception {
        // Tạo temporary match object
        Match tempMatch = createMatchObject(teamA, teamB, false);
        tempMatch.setId(matchId);
        generateCardsAndSubstitutionsUsingObjects(tempMatch, teamA, teamB);
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
        

        // Sử dụng TournamentService để lấy group standings với ID và players chính xác từ database
        Map<String, List<Team>> teamsByGroup = tournamentService.getAllGroupStandingsWithPlayersCalculatedInJava(currentTournamentId);

    

        // Lấy đội nhất và nhì bảng theo thứ tự A, B, C, D, E, F, G, H
        List<Team> firstPlaceTeams = new ArrayList<>();
        List<Team> secondPlaceTeams = new ArrayList<>();
        String[] groupOrder = {"A", "B", "C", "D", "E", "F", "G", "H"};
        
        for (String groupName : groupOrder) {
            List<Team> groupTeams = teamsByGroup.get(groupName);
            
            if (groupTeams != null && groupTeams.size() >= 2) {
                firstPlaceTeams.add(groupTeams.get(0)); // Nhất bảng
                secondPlaceTeams.add(groupTeams.get(1)); // Nhì bảng
                
            } else {
                System.out.println("   ❌ Group " + groupName + " không đủ teams!");
            }
        }
        
        // Tạo danh sách đội vượt qua với ghép đôi vòng 16 đội phù hợp
        List<Team> qualifiedTeams = createRoundOf16Pairings(firstPlaceTeams, secondPlaceTeams);

        // Hiển thị kết quả
        displayQualifiedTeams(qualifiedTeams);

        return qualifiedTeams;
    }

    /**
     * Hiển thị các đội đã vượt qua vòng bảng
     */
    private void displayQualifiedTeams(List<Team> qualifiedTeams) throws SQLException {
        // Lấy group standings để hiển thị
        Map<String, List<Team>> groupStandings = tournamentService.getAllGroupStandingsWithPlayersCalculatedInJava(currentTournamentId);
        
        // Đảm bảo thứ tự bảng A, B, C, D, E, F, G, H
        String[] groupOrder = {"A", "B", "C", "D", "E", "F", "G", "H"};
        
        for (String groupName : groupOrder) {
            List<Team> teams = groupStandings.get(groupName);
        }

        
    }

    private List<Team> createRoundOf16Pairings(List<Team> firstPlace, List<Team> secondPlace) {
       
        
        // Kiểm tra đủ teams
        if (firstPlace.size() < 8 || secondPlace.size() < 8) {
            System.err.println("❌ Không đủ teams để tạo vòng 16 đội!");
            System.err.println("   First place: " + firstPlace.size() + "/8");
            System.err.println("   Second place: " + secondPlace.size() + "/8");
            return new ArrayList<>();
        }

        List<Team> pairings = new ArrayList<>();

        // Ghép đôi vòng 16 đội FIFA World Cup:
        // Trận 1: Nhất bảng A vs Nhì bảng B
        pairings.add(firstPlace.get(0));   // Nhất bảng A
        pairings.add(secondPlace.get(1));  // Nhì bảng B
        

        // Trận 2: Nhất bảng B vs Nhì bảng A  
        pairings.add(firstPlace.get(1));   // Nhất bảng B
        pairings.add(secondPlace.get(0));  // Nhì bảng A
        

        // Trận 3: Nhất bảng C vs Nhì bảng D
        pairings.add(firstPlace.get(2));   // Nhất bảng C
        pairings.add(secondPlace.get(3));  // Nhì bảng D
        

        // Trận 4: Nhất bảng D vs Nhì bảng C
        pairings.add(firstPlace.get(3));   // Nhất bảng D
        pairings.add(secondPlace.get(2));  // Nhì bảng C
        

        // Trận 5: Nhất bảng E vs Nhì bảng F
        pairings.add(firstPlace.get(4));   // Nhất bảng E
        pairings.add(secondPlace.get(5));  // Nhì bảng F
        

        // Trận 6: Nhất bảng F vs Nhì bảng E
        pairings.add(firstPlace.get(5));   // Nhất bảng F
        pairings.add(secondPlace.get(4));  // Nhì bảng E
        

        // Trận 7: Nhất bảng G vs Nhì bảng H
        pairings.add(firstPlace.get(6));   // Nhất bảng G
        pairings.add(secondPlace.get(7));  // Nhì bảng H
        

        // Trận 8: Nhất bảng H vs Nhì bảng G
        pairings.add(firstPlace.get(7));   // Nhất bảng H
        pairings.add(secondPlace.get(6));  // Nhì bảng G
        

        System.out.println();
        return pairings;
    }

    private void runKnockoutStage(List<Team> qualifiedTeams) throws Exception {
        

        // Tạo bracket cho vòng 16 đội sử dụng KnockoutStageManager
        setupRoundOf16Bracket(qualifiedTeams);

        // Vòng 16 đội
        List<Team> quarterFinalists = runKnockoutRound(qualifiedTeams, "ROUND_16");
        
        // Cập nhật KnockoutStageManager với kết quả vòng 16
        List<String> quarterFinalistNames = quarterFinalists.stream().map(Team::getName).collect(Collectors.toList());
        knockoutManager.setRoundOf16Winners(quarterFinalistNames);

        // Tứ kết
        List<Team> semiFinalists = runKnockoutRound(quarterFinalists, "QUARTER");
        
        // Cập nhật KnockoutStageManager với kết quả tứ kết
        List<String> semiFinalistNames = semiFinalists.stream().map(Team::getName).collect(Collectors.toList());
        knockoutManager.setQuarterFinalWinners(semiFinalistNames);

        // Bán kết
        List<Team> finalists = runKnockoutRound(semiFinalists, "SEMI");

        // Xác định 2 đội thua bán kết (đồng hạng 3 theo quy định FIFA mới)
        List<Team> thirdPlaceTeams = getThirdPlaceTeams(semiFinalists, finalists);
        
        // Cập nhật KnockoutStageManager với kết quả bán kết
        List<String> finalistNames = finalists.stream().map(Team::getName).collect(Collectors.toList());
        List<String> thirdPlaceNames = thirdPlaceTeams.stream().map(Team::getName).collect(Collectors.toList());
        knockoutManager.setSemiFinalWinners(finalistNames, thirdPlaceNames);
        
        

        // Chung kết
        if (finalists.size() == 2) {
            System.out.println("Trận chung kết:");
            Team champion = runKnockoutMatch(finalists.get(0), finalists.get(1), "FINAL");
          

            // Cập nhật KnockoutStageManager với kết quả chung kết
            Team runnerUp = finalists.stream().filter(t -> !t.equals(champion)).findFirst().orElse(null);
            knockoutManager.setFinalResult(champion.getName(), runnerUp != null ? runnerUp.getName() : null);

            // Cập nhật giải đấu với kết quả cuối cùng
            updateTournamentResults(champion, finalists, thirdPlaceTeams);
        }
    }
    
    /**
     * Thiết lập bracket cho vòng 16 đội sử dụng KnockoutStageManager
     */
    private void setupRoundOf16Bracket(List<Team> qualifiedTeams) throws SQLException {
        // Lấy group standings để tạo bracket
        Map<String, List<Team>> groupStandings = tournamentService.getAllGroupStandingsWithPlayersCalculatedInJava(currentTournamentId);
        
        Map<String, String> winners = new HashMap<>();
        Map<String, String> runners = new HashMap<>();
        
        // Đảm bảo thứ tự bảng A, B, C, D, E, F, G, H
        String[] groupOrder = {"A", "B", "C", "D", "E", "F", "G", "H"};
        
        for (String groupName : groupOrder) {
            List<Team> teams = groupStandings.get(groupName);
            if (teams != null && teams.size() >= 2) {
                winners.put(groupName, teams.get(0).getName());
                runners.put(groupName, teams.get(1).getName());
            }
        }
        
        // Tạo bracket sử dụng KnockoutStageManager
        knockoutManager.generateRoundOf16Bracket(winners, runners);
        
    }

    private List<Team> runKnockoutRound(List<Team> teams, String roundType) throws Exception {
        System.out.println("Các trận đấu " + roundType + ":");

        List<Team> winners = new ArrayList<>();

        for (int i = 0; i < teams.size(); i += 2) {
            if (i + 1 < teams.size()) {
                Team winner = runKnockoutMatch(teams.get(i), teams.get(i + 1), roundType);
                winners.add(winner);
            }
        }

        return winners;
    }

    private Team runKnockoutMatch(Team teamA, Team teamB, String matchType) throws Exception {
        // Reload players for both teams to ensure consistency with database
        reloadPlayersForTeam(teamA);
        reloadPlayersForTeam(teamB);
        
        // Tạo Match object cho trận knockout
        Match match = createMatchObject(teamA, teamB, true);
        
        int[] score = DataGenerator.generateMatchScore();
        int teamAScore = score[0];
        int teamBScore = score[1];

        // Trong vòng loại trực tiếp, cần có người thắng - mô phỏng hiệp phụ/penalty nếu cần
        if (teamAScore == teamBScore) {
            // Mô phỏng loạt sút penalty
            teamAScore += random.nextBoolean() ? 1 : 0;
            teamBScore += (teamAScore > teamBScore) ? 0 : 1;
        }

        // Cập nhật kết quả thông qua Match object
        match.updateMatchResult(teamAScore, teamBScore);
        

        // Cập nhật thống kê đội bóng
        teamA.updateMatchStatistics(teamAScore, teamBScore);
        teamB.updateMatchStatistics(teamBScore, teamAScore);
        
        // Đồng bộ thống kê Team objects với database
        syncTeamStatisticsToDatabase(teamA);
        syncTeamStatisticsToDatabase(teamB);
        
        // Lấy đội thắng từ Match object
        Team winner = match.getWinnerTeam();
        if (winner == null) {
            // Fallback nếu có vấn đề với logic
            winner = (teamAScore > teamBScore) ? teamA : teamB;
            System.out.println("   ⚠️ DEBUG: getWinnerTeam() trả về null, sử dụng fallback logic");
        }
        
        

        String venue = DataGenerator.getRandomVenue();
        String referee = DataGenerator.getRandomReferee();
        
        // Format date as yyyy/mm/dd
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");
        String matchDate = dateFormat.format(new java.util.Date(System.currentTimeMillis() + random.nextInt(1000000000)));
        
        match.setMatchDate(matchDate);

        // Lưu Match object vào database
        int matchId = saveMatchToDatabase(match, venue, referee);

        // Tạo các sự kiện trận đấu
        generateMatchEvents(matchId, teamA, teamB, teamAScore, teamBScore);

        System.out.println("  " + teamA.getName() + " " + teamAScore + " - " + teamBScore + " " + teamB.getName());

        return winner;
    }
    
    /**
     * Reload players for a team from database to ensure consistency
     */
    private void reloadPlayersForTeam(Team team) throws Exception {
        try {
            // Use ObjectManager to reload team with fresh player data
            Team reloadedTeam = objectManager.loadTeamWithPlayers(team.getName(), team.getTournamentId());
            
            if (reloadedTeam != null && 
                !reloadedTeam.getStartingPlayers().isEmpty() && 
                !reloadedTeam.getSubstitutePlayers().isEmpty()) {
                
                // Update the current team object with fresh player data
                team.setStartingPlayers(reloadedTeam.getStartingPlayers());
                team.setSubstitutePlayers(reloadedTeam.getSubstitutePlayers());
            } else {
                // Fallback: Use existing players and ensure lineup is complete
               
                ensureTeamHasPlayers(team);
            }
            
        } catch (Exception e) {
            System.out.println("   ⚠️ Reload failed for " + team.getName() + ", using existing players");
            // Fallback: Use existing players and ensure lineup is complete
            ensureTeamHasPlayers(team);
        }
    }
    
    /**
     * Ensure team has complete lineup using existing players
     */
    private void ensureTeamHasPlayers(Team team) {
        List<Player> startingPlayers = new ArrayList<>(team.getStartingPlayers());
        List<Player> substitutePlayers = new ArrayList<>(team.getSubstitutePlayers());
        
        // If team has no players at all, this is a critical error
        if (startingPlayers.isEmpty() && substitutePlayers.isEmpty()) {
            System.out.println("   ❌ Team " + team.getName() + " has no players at all - critical error");
            return;
        }
        
        // Combine all players
        List<Player> allPlayers = new ArrayList<>();
        allPlayers.addAll(startingPlayers);
        allPlayers.addAll(substitutePlayers);
        
        // Clear current lists
        startingPlayers.clear();
        substitutePlayers.clear();
        
        // Redistribute players: first 11 eligible players go to starting lineup
        int startingCount = 0;
        for (Player player : allPlayers) {
            if (startingCount < 11 && player.isPlayerEligible() && !player.isSuspended()) {
                startingPlayers.add(player);
                startingCount++;
            } else {
                substitutePlayers.add(player);
            }
        }
        
        // If still not enough starting players, promote from substitutes
        while (startingPlayers.size() < 11 && !substitutePlayers.isEmpty()) {
            Player substitute = substitutePlayers.remove(0);
            startingPlayers.add(substitute);
            System.out.println("   ⬆️ " + substitute.getName() + " promoted to starting lineup");
        }
        
        // Update team
        team.setStartingPlayers(startingPlayers);
        team.setSubstitutePlayers(substitutePlayers);
        
        
    }
    
    /**
     * Validate that a player exists in the database for the given team
     */
    private boolean validatePlayerExistsInDatabase(Player player, Team team) {
        try {
            String sql = """
                SELECT COUNT(*) FROM players p 
                JOIN teams t ON p.team_id = t.id 
                WHERE p.name = ? AND t.name = ? AND t.tournament_id = ?
            """;
            
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            pstmt.setString(1, player.getName());
            pstmt.setString(2, team.getName());
            pstmt.setInt(3, team.getTournamentId());
            ResultSet rs = pstmt.executeQuery();
            
            boolean exists = false;
            if (rs.next()) {
                exists = rs.getInt(1) > 0;
            }
            
            rs.close();
            pstmt.close();
            return exists;
            
        } catch (Exception e) {
            System.out.println("   ⚠️ Lỗi khi validate player " + player.getName() + ": " + e.getMessage());
            return false;
        }
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
        
        System.out.println("Kết quả:");
        System.out.println("   Đội vô địch: " + champion.getName());
        System.out.println("   Đội về nhì: " + (runnerUp != null ? runnerUp.getName() : "N/A"));
        
        // Theo quy định FIFA mới: 2 đội thua bán kết đồng hạng 3
        if (thirdPlaceTeams.size() == 2) {
            System.out.println("   Hai đội đồng hạng ba: " + thirdPlaceTeams.get(0).getName() + " và " + thirdPlaceTeams.get(1).getName());
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

        tournamentService.updateTournamentWinners(currentTournamentId, championId, runnerUpId, thirdPlaceId01, thirdPlaceId02);
        
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

            
        }
    }

    /**
     * Phương thức này đã được loại bỏ vì cột status không còn tồn tại
     */
    // private void updateTournamentStatus(String status) throws SQLException {
    //     // Đã xóa cột status khỏi bảng tournaments
    // }

    private void generateTournamentStatistics() throws SQLException {
        

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
                
                System.out.printf("  %d. %-15s %2d pts | W:%d D:%d L:%d | GF:%d GA:%d GD:%+d%s%n",
                        position,
                        team.getName(),
                        stats.getPoints(),
                        stats.wins,
                        stats.draws,
                        stats.losses,
                        stats.goalsFor,
                        stats.goalsAgainst,
                        stats.getGoalDifference(),
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
        // Tính toán thống kê từ matches CHỈ VÒNG BẢNG thay vì lấy từ teams table
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
            LEFT JOIN matches m ON (t.id = m.team_a_id OR t.id = m.team_b_id) 
                AND (m.team_a_score >= 0 AND m.team_b_score >= 0)
                AND m.match_type = 'GROUP'
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
        
        // Tính điểm theo quy định FIFA: Thắng = 3 điểm, Hòa = 1 điểm, Thua = 0 điểm
        public int getPoints() {
            return wins * 3 + draws * 1 + losses * 0;
        }
        
        // Tính hiệu số bàn thắng
        public int getGoalDifference() {
            return goalsFor - goalsAgainst;
        }
    }

    /**
     * Create a new tournament with sample data
     * Demonstrates OOP principles with proper object creation
     */
    public void createNewTournament() {
        try {
            
            
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
                
                
                // Create groups and teams using OOP
                createGroupsAndTeams();
                
                // Generate some sample matches
                generateSampleMatches();
                
                
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
                    System.out.println(" Đã random hóa tournament thành công!");
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
                System.out.println("");

                // Lấy tên tournament hiện tại
                String selectSql = "SELECT name FROM tournaments WHERE id = ?";
                PreparedStatement selectStmt = dbManager.getConnection().prepareStatement(selectSql);
                selectStmt.setInt(1, currentTournamentId);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    String tournamentName = rs.getString("name");
                    statsCalculator.recalculateTournamentStats(currentTournamentId, tournamentName);
                    
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

            System.out.println(" Đã cập nhật goal_difference cho " + updatedRows + " teams!");

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

                System.out.println(" Đã cập nhật goal_difference cho " + updatedRows + " teams trong tournament hiện tại!");

            } else {
                System.out.println("⚠️ Không có tournament hiện tại để cập nhật!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tính lại goal_difference cho tournament hiện tại: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void close() {
        if (dbManager != null) {
            dbManager.close();
        }
    }
}