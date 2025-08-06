package com.worldcup.automation;


import com.worldcup.database.DatabaseManager;
import com.worldcup.generator.DataGenerator;
import com.worldcup.manager.ObjectManager;
import com.worldcup.model.*;
import com.worldcup.repository.WorldCupAutomationRepository;
import com.worldcup.service.MatchService;
import com.worldcup.service.PlayerService;
import com.worldcup.service.TeamService;
import com.worldcup.service.TournamentService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class WorldCupAutomation {
    private DatabaseManager dbManager;
    private ObjectManager objectManager; // NEW: OOP Manager
    private WorldCupAutomationRepository repository; // NEW: Repository for SQL operations
    // DataGenerator has only static methods, no instance needed

    private TeamService teamService;
    private TournamentService tournamentService;
    private PlayerService playerService;
    private MatchService matchService;
    private List<Team> teams;
    private List<Match> matches;
    private Tournament tournament;
    private List<Group> groups;
    private KnockoutStageManager knockoutManager;
    private Random random = new Random();

    public WorldCupAutomation() {
        this.dbManager = new DatabaseManager();
        this.objectManager = ObjectManager.getInstance(dbManager);
        this.repository = new WorldCupAutomationRepository(dbManager); // NEW: Initialize repository
        // DataGenerator uses static methods only

        this.teamService = new TeamService(dbManager);
        this.tournamentService = new TournamentService(dbManager);
        this.playerService = new PlayerService(dbManager);
        this.matchService = new MatchService(objectManager);

        this.teams = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.matches = new ArrayList<>();
        this.knockoutManager = new KnockoutStageManager();
    }

    public void runCompleteWorldCup() {
        try {
            // Bước 0: Xóa dữ liệu cũ
            clearOldData();
            
            // Bước 1: Tạo các đội bóng
            generateTeams();

            // Bước 2: Tạo bảng đấu và phân chia đội
            createGroupsAndAssignTeams();

            // Bước 3: Chạy vòng bảng
            runGroupStage();

            // Bước 4: Xác định đội nhất và nhì bảng
            List<Team> qualifiedTeams = determineQualifiedTeams();

            // Bước 5: Chạy vòng loại trực tiếp
            runKnockoutStage(qualifiedTeams);

            // Bước 6: Tạo thống kê cuối giải
            generateTournamentStatistics();

            // Bước 7: Cập nhật match_type cho tất cả matches
            updateAllMatchTypes();
            
            // Bước 8: Tính toán lại tournament stats chính xác
            recalculateCurrentTournamentStats();
            
            // Bước 9: Cập nhật lại tất cả player goals từ database
            updatePlayerGoalsFromDatabase();
            
            // Bước 10: Tính toán lại tournament stats lần cuối
            recalculateCurrentTournamentStats();
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong quá trình mô phỏng World Cup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Chạy chỉ vòng bảng và in kết quả
     */
    public void runGroupStageOnly() {
        try {
            // Bước 1: Tạo các đội bóng
            generateTeams();

            // Bước 2: Tạo bảng đấu và phân chia đội
            createGroupsAndAssignTeams();

            // Bước 3: Chạy vòng bảng
            runGroupStage();

            // Bước 4: In kết quả vòng bảng
            printGroupStageResults();

        } catch (Exception e) {
            System.err.println("❌ Lỗi trong quá trình mô phỏng vòng bảng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Team> generateTeams() throws Exception {
        int randomYear = DataGenerator.getRandomWorldCupYear(); // trả về 1 năm
        LocalDate[] randomDates = DataGenerator.generateTournamentDates(randomYear); // trả về yyyy/MM/dd
        String name = DataGenerator.generateTournamentName(randomYear); // trả về tên giải đấu

        System.out.println("Tạo 32 đội bóng...");
        teams = DataGenerator.generateTeams(32);
        // Reset tất cả thống kê về 0 trước khi bắt đầu tournament
        for (Team team : teams) {
            if (team.isHost()) {
                tournament = new Tournament(randomYear, team, name, randomDates[0], randomDates[1]);
                int id = repository.saveTournament(tournament);
                tournament.setId(id);
            }

            team.reset();
            
            // Set starting players và substitute players ngay khi tạo team
            team.setStartingPlayers(DataGenerator.generateStartingPlayers(team));
            team.setSubstitutePlayers(DataGenerator.generateSubstitutePlayers(team));
            
            saveTeam(team);
            savePlayer(team);
        }
        tournament.setTeamList(teams);

        return teams;
    }

    private void saveTeam(Team team) throws Exception {
        team.setTournamentId(tournament.getId());
        objectManager.saveTeam(team);
    }

    private void savePlayer(Team team) throws Exception {
        for (Player player : team.getPlayers()) {
            try {
                // Kiểm tra xem player có trong starting lineup không
                boolean isStarting = team.getStartingPlayers().contains(player);
                objectManager.savePlayer(player, team.getId(), isStarting);
            } catch (SQLException e) {
                if (e.getMessage().contains("UNIQUE constraint failed")) {
                    
                    // Skip this player and continue
                    continue;
                } else {
                    throw e; // Re-throw other SQL exceptions
                }
            }
        }
    }


    private void createGroupsAndAssignTeams() throws SQLException {
        System.out.println("Tạo bảng đấu và phân chia đội...");

        for (Group group : DataGenerator.getGroups()) {
            int groupId = repository.saveGroup(group.getName(), tournament.getId());
            group.setId(groupId);
            groups.add(group);
        }

        // đảo vị trí của team
        Collections.shuffle(teams);
        for (int i = 0; i < teams.size(); i++) {
            int groupIndex = i / 4; // 4 đội mỗi bảng
            int groupId = groupIndex + 1; // ID bảng bắt đầu từ 1
            Team team = teams.get(i);
            Group group = groups.get(groupIndex);

            // Cập nhật đội với bảng được phân - sử dụng team ID thay vì name để đảm bảo chính xác
            repository.updateTeamGroup(team.getId(), groupId);

            // Cập nhật group ID trong team object
            team.setGroupId(groupId);
            // Thêm đội vào đối tượng bảng
            group.addTeam(team);
        }

        tournament.setGroupList(groups);
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

                    // Cập nhật is_starting status cho trận đấu này
                    updatePlayersStartingStatus(teamA);
                    updatePlayersStartingStatus(teamB);

                    simulateMatch(teamA, teamB, "GROUP");
                }
            }
        }
    }

    private void simulateMatch(Team teamA, Team teamB, String matchType) throws Exception {
        // Generate match score
        int[] score = DataGenerator.generateMatchScore();
        int teamAScore = score[0];
        int teamBScore = score[1];

        String venue = DataGenerator.getRandomVenue();
        String referee = DataGenerator.getRandomReferee();
        boolean isKnockout = !matchType.equals("GROUP");

        Match match = matchService.createMatch(teamA, teamB, venue, referee, isKnockout);
        
        // Cập nhật match_type đúng format
        match.setMatchType(matchType);
        
        matchService.updateResult(match, teamAScore, teamBScore);
        matchService.generateEvents(match, teamAScore, teamBScore);

        System.out.println("  ⚽ " + teamA.getName() + " " + teamAScore + " - " + teamBScore + " " + teamB.getName());
    }


    /**
     * Lưu Match vào database
     */
    private int saveMatchToDatabase(Match match) throws SQLException {
        int matchId = repository.saveMatch(match, tournament.getId());
        match.setId(matchId);
        return matchId;
    }


    private void generateMatchEvents(Match match) throws Exception {
        Team teamA = match.getTeamA();
        Team teamB = match.getTeamB();

        int teamAScore = match.getGoalsTeamA();
        int teamBScore = match.getGoalsTeamB();

        // Tạo bàn thắng cho đội A
        generateGoalsForTeam(match, teamA, teamAScore);

        // Tạo bàn thắng cho đội B
        generateGoalsForTeam(match, teamB, teamBScore);

        // Tạo thẻ cho đội A
        generateCards(match, teamA);
        // Tạo thẻ cho đội B
        generateCards(match, teamB);

        // thay người cho đội A
        // thay người cho đội B
    }

    /**
     * Tạo bàn thắng
     */
    private void generateGoalsForTeam(Match match, Team team, int goalCount) throws Exception {
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
     * Tạo thẻ phạt
     */
    private void generateCards(Match match, Team team) throws Exception {
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
     * In ra kết quả vòng bảng với định dạng bảng xếp hạng
     */
    public void printGroupStageResults() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                        KẾT QUẢ VÒNG BẢNG");
        System.out.println("=".repeat(80));
        
        for (Group group : groups) {
            System.out.println("\n🏆 BẢNG " + group.getName());
            System.out.println("-".repeat(70));
            System.out.printf("%-20s %3s %3s %3s %3s %3s %3s %3s%n", 
                "Tên đội", "W", "D", "L", "GF", "GA", "GD", "Pts");
            System.out.println("-".repeat(70));
            
            // Sắp xếp các đội theo điểm số và hiệu số bàn thắng
            List<Team> sortedTeams = teamService.sortTeamsByStanding(group.getTeams());
            
            int position = 1;
            for (Team team : sortedTeams) {
                String positionIcon = getPositionIcon(position);
                System.out.printf("%s %-17s %3d %3d %3d %3d %3d %+3d %3d%n",
                    positionIcon,
                    team.getName(),
                    team.getWins(),
                    team.getDraws(), 
                    team.getLosses(),
                    team.getGoalsFor(),
                    team.getGoalsAgainst(),
                    team.getGoalDifference(),
                    team.getPoints()
                );
                position++;
            }
            System.out.println("-".repeat(70));
        }
        
        System.out.println("\n📋 Chú thích:");
        System.out.println("🥇 = Nhất bảng (vào vòng 16)");
        System.out.println("🥈 = Nhì bảng (vào vòng 16)");
        System.out.println("❌ = Bị loại");
        System.out.println("W=Thắng, D=Hòa, L=Thua, GF=Bàn thắng, GA=Bàn thua, GD=Hiệu số, Pts=Điểm");
    }
    
    /**
     * Lấy icon cho vị trí trong bảng
     */
    private String getPositionIcon(int position) {
        switch (position) {
            case 1: return "🥇";
            case 2: return "🥈";
            default: return "❌";
        }
    }

    private List<Team> determineQualifiedTeams() {
        // In kết quả vòng bảng trước khi xác định đội vượt qua
        printGroupStageResults();
        
        // Lấy đội nhất và nhì bảng theo thứ tự A, B, C, D, E, F, G, H
        List<Team> firstPlaceTeams = new ArrayList<>();
        List<Team> secondPlaceTeams = new ArrayList<>();

        for (Group group : groups) {
            List<Team> groupTeams = teamService.sortTeamsByStanding(group.getTeams());
            firstPlaceTeams.add(groupTeams.get(0)); // Nhất bảng
            secondPlaceTeams.add(groupTeams.get(1)); // Nhì bảng
        }

        // Tạo danh sách đội vượt qua với ghép đôi vòng 16 đội phù hợp
        List<Team> qualifiedTeams = createRoundOf16Pairings(firstPlaceTeams, secondPlaceTeams);
        return qualifiedTeams;
    }

    private List<Team> createRoundOf16Pairings(List<Team> firstPlace, List<Team> secondPlace) {
        // Kiểm tra đủ đội
        if (firstPlace.size() < 8 || secondPlace.size() < 8) {
            System.err.println("❌ Không đủ teams để tạo vòng 16 đội!");
            System.err.println("   First place: " + firstPlace.size() + "/8");
            System.err.println("   Second place: " + secondPlace.size() + "/8");
            return new ArrayList<>();
        }

        List<Team> pairings = new ArrayList<>();
        int[][] matchups = {
                {0, 1}, // Trận 1: A1 vs B2
                {1, 0}, // Trận 2: B1 vs A2
                {2, 3}, // Trận 3: C1 vs D2
                {3, 2}, // Trận 4: D1 vs C2
                {4, 5}, // Trận 5: E1 vs F2
                {5, 4}, // Trận 6: F1 vs E2
                {6, 7}, // Trận 7: G1 vs H2
                {7, 6}  // Trận 8: H1 vs G2
        };

        for (int i = 0; i < matchups.length; i++) {
            int firstIdx = matchups[i][0];
            int secondIdx = matchups[i][1];
            pairings.add(firstPlace.get(firstIdx));
            pairings.add(secondPlace.get(secondIdx));
        }

        return pairings;
    }


    private void runKnockoutStage(List<Team> qualifiedTeams) throws Exception {
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
        List<Team> finalists = runKnockoutRound(semiFinalists, "SEMI_FINAL");

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
            tournament.setChampion(champion);

            // Cập nhật KnockoutStageManager với kết quả chung kết
            Team runnerUp = finalists.stream().filter(t -> !t.equals(champion)).findFirst().orElse(null);
            knockoutManager.setFinalResult(champion.getName(), runnerUp != null ? runnerUp.getName() : null);
            tournament.setRunnerUp(runnerUp);

            // Cập nhật giải đấu với kết quả cuối cùng
            updateTournamentResults(champion, finalists, thirdPlaceTeams);
        }
        tournament.setThirdPlace(thirdPlaceTeams);
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
        // Cập nhật is_starting cho cả hai đội trước khi thi đấu
        updatePlayersStartingStatus(teamA);
        updatePlayersStartingStatus(teamB);
        
        String venue = DataGenerator.getRandomVenue();
        String referee = DataGenerator.getRandomReferee();
        Match match = new Match(teamA, teamB, venue, referee, true);
        
        // Cập nhật match_type đúng format
        match.setMatchType(matchType);

        int[] score = DataGenerator.generateMatchScore();
        int teamAScore = score[0];
        int teamBScore = score[1];

        // Trong vòng loại trực tiếp, cần có người thắng - mô phỏng hiệp phụ/penalty nếu cần
        if (teamAScore == teamBScore) {
            // Mô phỏng loạt sút penalty
            teamAScore += random.nextBoolean() ? 1 : 0;
            teamBScore += (teamAScore > teamBScore) ? 0 : 1;
        }

        // Cập nhật kết quả thông qua Match
        match.updateMatchResult(teamAScore, teamBScore);

        // Cập nhật thống kê đội bóng
        teamA.updateMatchStatistics(teamAScore, teamBScore);
        teamB.updateMatchStatistics(teamBScore, teamAScore);

        // Lấy đội thắng từ Match object
        Team winner = match.getWinnerTeam();

        // Format date as yyyy/mm/dd
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String matchDate = dateFormat.format(new java.util.Date(System.currentTimeMillis() + random.nextInt(1000000000)));

        match.setMatchDate(matchDate);

        // Lưu Match object vào database
        int matchId = saveMatchToDatabase(match);
        match.setId(matchId);
        // Tạo các sự kiện trận đấu
        generateMatchEvents(match);

        System.out.println("  " + teamA.getName() + " " + teamAScore + " - " + teamBScore + " " + teamB.getName());

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

        tournamentService.updateTournamentWinners(tournament.getId(), championId, runnerUpId, thirdPlaceId01, thirdPlaceId02);

    }


    /**
     * Lấy team ID từ tên team
     */
    private Integer getTeamId(String teamName) throws SQLException {
        return repository.getTeamIdByName(teamName, tournament.getId());
    }

    /**
     * Đảm bảo tournament_stats record tồn tại
     */
    private void ensureTournamentStatsRecord() throws SQLException {
        if (!repository.tournamentStatsExists(tournament.getId())) {
            repository.createTournamentStatsRecord(tournament.getId());
        }
    }

    private void generateTournamentStatistics() throws SQLException {
        // Sử dụng TournamentService để tính toán thống kê bằng Java
        TournamentService.TournamentStats stats = tournamentService.calculateTournamentStats(tournament.getId());

        if (!repository.tournamentStatsExists(tournament.getId())) {
            // Lưu thống kê vào database nếu chưa tồn tại
            repository.saveTournamentStats(tournament.getId(), stats);
        } else {
            // Cập nhật thống kê nếu đã tồn tại
            repository.updateTournamentStats(tournament.getId(), stats);
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
     * Tính toán lại stats cho tournament hiện tại
     */
    public void recalculateCurrentTournamentStats() {
        try {
            if (tournament.getId() > 0) {
                System.out.println("");

                String tournamentName = repository.getTournamentName(tournament.getId());
                if (tournamentName != null) {
                    tournamentService.recalculateTournamentStats(tournament.getId(), tournamentName);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tính toán stats cho tournament hiện tại: " + e.getMessage());
        }
    }
    
    /**
     * Tính toán lại tất cả tournament stats trong database
     * Method tiện ích để fix các giá trị null trong top_scorer_id và top_scorer_goals
     */
    public void recalculateAllTournamentStats() {
        System.out.println("🔄 Bắt đầu tính toán lại tất cả tournament statistics...");
        
        try {
            tournamentService.recalculateAllTournamentStats();
            System.out.println("✅ Hoàn thành tính toán lại tất cả tournament statistics!");
            
            // Hiển thị kết quả
            displayAllTournamentStats();
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tính toán tất cả tournament stats: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Hiển thị tất cả tournament stats để kiểm tra
     * Sử dụng method từ TournamentService
     */
    private void displayAllTournamentStats() {
        tournamentService.displayAllTournamentStats();
    }

    /**
     * Cập nhật trạng thái is_starting cho các cầu thủ trong danh sách starting
     */
    private void updatePlayersStartingStatus(Team team) throws Exception {
        // Reset tất cả players của team về is_starting = false
        for (Player player : team.getPlayers()) {
            objectManager.getPlayerRepository().updateStartingStatus(player.getId(), false);
        }
        
        // Cập nhật is_starting = true cho starting players
        for (Player player : team.getStartingPlayers()) {
            objectManager.getPlayerRepository().updateStartingStatus(player.getId(), true);
        }
    }
    
    /**
     * Xóa dữ liệu cũ trước khi tạo tournament mới
     */
    private void clearOldData() {
        try {
            
            // Xóa theo thứ tự để tránh foreign key constraint
            String[] deleteQueries = {
                "DELETE FROM substitutions",
                "DELETE FROM cards", 
                "DELETE FROM goals",
                "DELETE FROM matches",
                "DELETE FROM tournament_stats",
                "DELETE FROM players",
                "DELETE FROM assistant_coaches",
                "DELETE FROM teams",
                "DELETE FROM groups",
                "DELETE FROM tournaments"
            };
            
            for (String query : deleteQueries) {
                PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query);
                pstmt.executeUpdate();
                pstmt.close();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa dữ liệu cũ: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật goals cho tất cả players từ database
     */
    public void updatePlayerGoalsFromDatabase() {
        try {
            
            
            String sql = """
                UPDATE players 
                SET goals = (
                    SELECT COUNT(*) 
                    FROM goals g 
                    WHERE g.player_id = players.id
                )
                WHERE id IN (
                    SELECT DISTINCT player_id FROM goals
                )
            """;
            
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            int updatedRows = pstmt.executeUpdate();
            pstmt.close();

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi cập nhật player goals: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật match_type cho tất cả matches trong database dựa trên logic
     */
    public void updateAllMatchTypes() {
        try {
            
            
            String sql = """
                UPDATE matches 
                SET match_type = CASE 
                    WHEN (SELECT COUNT(*) FROM matches m2 WHERE m2.id <= matches.id) <= 48 THEN 'GROUP'
                    WHEN (SELECT COUNT(*) FROM matches m2 WHERE m2.id <= matches.id) <= 56 THEN 'ROUND_16'
                    WHEN (SELECT COUNT(*) FROM matches m2 WHERE m2.id <= matches.id) <= 60 THEN 'QUARTER'
                    WHEN (SELECT COUNT(*) FROM matches m2 WHERE m2.id <= matches.id) <= 62 THEN 'SEMI_FINAL'
                    ELSE 'FINAL'
                END
                WHERE match_type IS NULL OR match_type = 'KNOCKOUT'
            """;
            
            PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
            int updatedRows = pstmt.executeUpdate();
            pstmt.close();

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi cập nhật match_type: " + e.getMessage());
        }
    }

    public void close() {
        if (dbManager != null) {
            dbManager.close();
        }
    }
}