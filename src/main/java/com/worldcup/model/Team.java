package com.worldcup.model;

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
    private List<Player> startingPlayers = new ArrayList<>();
    private List<Player> substitutePlayers = new ArrayList<>();
    
    private Random random = new Random();

    public Team(String name, String region, String coach, List<String> assistantCoaches,
                String medicalStaff, List<Player> players, boolean isHost) {

        if (assistantCoaches.size() > 3) {
            throw new IllegalArgumentException("Tối đa 3 trợ lý huấn luyện viên.");
        }

        if (players.size() > 22) {
            throw new IllegalArgumentException("Đội bóng có tối đa 22 cầu thủ.");
        }

        this.name = name;
        this.region = region;
        this.coach = coach;
        this.assistantCoaches = new ArrayList<>(assistantCoaches);
        this.medicalStaff = medicalStaff;
        this.players = new ArrayList<>(players);
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
        return Collections.unmodifiableList(startingPlayers);
    }

    public List<Player> getSubstitutePlayers() {
        return Collections.unmodifiableList(substitutePlayers);
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
    public void resetStatistics() {
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
}