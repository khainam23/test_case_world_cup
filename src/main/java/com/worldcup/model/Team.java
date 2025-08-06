package com.worldcup.model;

import com.worldcup.repository.TeamRepository;

import java.util.*;

public class Team {
    // Thuộc tính tương ứng với database schema
    private int id;
    private String name;
    private String region;
    private String coach;
    private List<String> assistantCoaches;
    private String medicalStaff;
    private boolean isHost;

    // Thống kê trận đấu - tương ứng với database columns
    private int points;
    private int goalDifference;
    private int goalsFor;
    private int goalsAgainst;
    private int wins;
    private int draws;
    private int losses;

    // Thống kê thẻ và thay người - tương ứng với database columns
    private int yellowCards;
    private int redCards;
    private int substitutionCount;

    // Thông tin bảng đấu
    private int groupId;
    private int tournamentId;

    // Danh sách cầu thủ
    private List<Player> players;
    private List<Player> startingPlayers;
    private List<Player> substitutePlayers;

    private Random random = new Random();

    // Repository for persistence operations
    private static TeamRepository teamRepository;

    public Team(String name, String region, String coach, List<String> assistantCoaches,
                String medicalStaff, boolean isHost) {
        if (assistantCoaches.size() > 3) {
            throw new IllegalArgumentException("Tối đa 3 trợ lý huấn luyện viên.");
        }

        this.name = name;
        this.region = region;
        this.coach = coach;
        this.assistantCoaches = new ArrayList<>(assistantCoaches);
        this.medicalStaff = medicalStaff;
        this.isHost = isHost;

        // Khởi tạo các thống kê
        this.points = 0;
        this.goalDifference = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
        this.yellowCards = 0;
        this.redCards = 0;
        this.substitutionCount = 0;

        startingPlayers = new ArrayList<>();
        substitutePlayers = new ArrayList<>();
        players = new ArrayList<>();
    }

    public Team(String name, String region, String coach, List<String> assistantCoaches,
                String medicalStaff, List<Player> startingPlayers, List<Player> substitutePlayers, boolean isHost) {

        if (assistantCoaches.size() > 3) {
            throw new IllegalArgumentException("Tối đa 3 trợ lý huấn luyện viên.");
        }

        if (startingPlayers.size() != 11) {
            throw new IllegalArgumentException("Đội bóng phải có đúng 11 cầu thủ đá chính.");
        }

        if (substitutePlayers.size() < 5) {
            throw new IllegalArgumentException("Đội bóng phải có ít nhất 5 cầu thủ dự bị.");
        }

        this.name = name;
        this.region = region;
        this.coach = coach;
        this.assistantCoaches = new ArrayList<>(assistantCoaches);
        this.medicalStaff = medicalStaff;
        this.isHost = isHost;

        this.startingPlayers = new ArrayList<>(startingPlayers);
        this.substitutePlayers = new ArrayList<>(substitutePlayers);

        this.players = new ArrayList<>();
        this.players.addAll(startingPlayers);
        this.players.addAll(substitutePlayers);

        // Khởi tạo các thống kê
        this.points = 0;
        this.goalDifference = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
        this.yellowCards = 0;
        this.redCards = 0;
        this.substitutionCount = 0;
    }


    // Getters
    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public String getCoach() {
        return coach;
    }

    public List<String> getAssistantCoaches() {
        return Collections.unmodifiableList(assistantCoaches);
    }

    public String getMedicalStaff() {
        return medicalStaff;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getGoalDifference() {
        return goalDifference;
    }

    public void setGoalDifference(int goalDifference) {
        this.goalDifference = goalDifference;
    }

    public int getYellowCards() {
        return yellowCards;
    }

    public void setYellowCards(int yellowCards) {
        this.yellowCards = yellowCards;
    }

    public int getRedCards() {
        return redCards;
    }

    public void setRedCards(int redCards) {
        this.redCards = redCards;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public int getSubstitutionCount() {
        return substitutionCount;
    }

    public void setSubstitutionCount(int substitutionCount) {
        this.substitutionCount = substitutionCount;
    }

    public List<Player> getStartingPlayers() {
        return startingPlayers;
    }

    public List<Player> getSubstitutePlayers() {
        return substitutePlayers;
    }

    public void addStartingPlayer(Player player) {
        this.startingPlayers.add(player);
    }

    public void addSubstitutePlayer(Player player) {
        this.substitutePlayers.add(player);
    }

    /**
     * Kiểm tra xem cầu thủ có thuộc đội này không
     */
    public boolean isContainPlayer(Player player) {
        return players.contains(player);
    }

    public boolean isContainPlayerIn(Player player) {
        return startingPlayers.contains(player);
    }

    public boolean isContainPlayerOut(Player player) {
        return substitutePlayers.contains(player);
    }


    // Getters và Setters cho các thuộc tính mới
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGoalsFor() {
        return goalsFor;
    }

    public void setGoalsFor(int goalsFor) {
        this.goalsFor = goalsFor;
    }

    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    public void setGoalsAgainst(int goalsAgainst) {
        this.goalsAgainst = goalsAgainst;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(int tournamentId) {
        this.tournamentId = tournamentId;
    }

    /**
     * Cập nhật thống kê trận đấu bằng Java logic
     */
    public void updateMatchStatistics(int goalsFor, int goalsAgainst) {
        this.goalsFor += goalsFor;
        this.goalsAgainst += goalsAgainst;
        this.goalDifference = this.goalsFor - this.goalsAgainst;

        if (goalsFor > goalsAgainst) {
            this.wins++;
            this.points += 3;
        } else if (goalsFor == goalsAgainst) {
            this.draws++;
            this.points += 1;
        } else {
            this.losses++;
            // Không cộng điểm khi thua
        }
    }

    /**
     * Reset tất cả thống kê về 0
     */
    public void reset() {
        this.points = 0;
        this.goalDifference = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
        this.yellowCards = 0;
        this.redCards = 0;
        this.substitutionCount = 0;
    }

    /**
     * Thực hiện thay người
     *
     * @param playerOut Cầu thủ ra
     * @param playerIn  Cầu thủ vào
     * @return true nếu thành công, false nếu không thể thay người
     */
    public boolean performSubstitution(Player playerOut, Player playerIn) {
        // Kiểm tra điều kiện
        if (!startingPlayers.contains(playerOut)) {
            return false; // Cầu thủ ra không trong đội hình chính
        }
        if (!substitutePlayers.contains(playerIn)) {
            return false; // Cầu thủ vào không trong danh sách dự bị
        }
        if (!playerIn.isEligible() || !playerOut.isEligible()) {
            return false; // Cầu thủ không đủ điều kiện
        }

        // Thực hiện thay người
        startingPlayers.remove(playerOut);
        substitutePlayers.remove(playerIn);
        startingPlayers.add(playerIn);
        substitutePlayers.add(playerOut);

        // Tăng số lần thay người
        this.substitutionCount++;

        return true;
    }

    /**
     * Kiểm tra xem có thể thay người không
     */
    public boolean canPerformSubstitution(Player playerOut, Player playerIn) {
        return startingPlayers.contains(playerOut) &&
                substitutePlayers.contains(playerIn) &&
                playerIn.isEligible() &&
                playerOut.isEligible();
    }

    /**
     * Xóa cầu thủ khỏi danh sách đá chính (dùng khi bị thẻ đỏ)
     */
    public boolean removeStartingPlayer(Player player) {
        return startingPlayers.remove(player);
    }

    /**
     * Xóa cầu thủ khỏi danh sách dự bị
     */
    public boolean removeSubstitutePlayer(Player player) {
        return substitutePlayers.remove(player);
    }

    @Override
    public String toString() {
        return String.format("Team{name='%s', points=%d, goalDiff=%d, record=%d-%d-%d}",
                name, points, goalDifference, wins, draws, losses);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Team team = (Team) obj;
        return Objects.equals(name, team.name) &&
                Objects.equals(region, team.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, region);
    }

    public void setStartingPlayers(List<Player> startingPlayers) {
        this.startingPlayers = startingPlayers;
    }

    public void setSubstitutePlayers(List<Player> substitutePlayers) {
        this.substitutePlayers = substitutePlayers;
    }

    public static void setTeamRepository(TeamRepository repository) {
        teamRepository = repository;
    }

    /**
     * Save this team to database using repository
     */
    public void save() throws Exception {
        if (teamRepository != null) {
            teamRepository.save(this);
        }
    }

    /**
     * Update this team in database using repository
     */
    public void update() throws Exception {
        if (teamRepository != null && id > 0) {
            teamRepository.update(this);
        }
    }

    /**
     * Load team with players from database
     */
    public void loadWithPlayers() throws Exception {
        if (teamRepository != null && id > 0) {
            // Repository sẽ load players và set vào team
            // Implementation sẽ được xử lý trong repository
        }
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
}