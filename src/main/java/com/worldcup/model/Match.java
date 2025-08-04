package com.worldcup.model;

import java.time.LocalDate;
import java.util.*;

// import static com.worldcup.constant.Constant.*;

public class Match {
    // Thuộc tính tương ứng với database schema
    private int id;
    private int teamAId;
    private int teamBId;
    private final Team teamA;
    private final Team teamB;
    private int teamAScore; // team_a_score trong DB
    private int teamBScore; // team_b_score trong DB
    private String matchType; // match_type trong DB
    private String matchDate; // match_date trong DB (format: yyyy/mm/dd)
    private String venue;
    private String referee;

    private Integer winnerId; // winner_id trong DB
    
    // Legacy fields for backward compatibility
    private int goalsTeamA;
    private int goalsTeamB;
    private int yellowCards;
    private int redCards;
    private final boolean isKnockout;
    private boolean isFinished;
    private LocalDate date;
    private Type type;

    private final List<Goal> goals;
    private final List<Substitution> substitutions;
    private final List<Player> startingPlayersTeamA;
    private final List<Player> startingPlayersTeamB;
    private final List<Player> substitutePlayersTeamA;
    private final List<Player> substitutePlayersTeamB;
    private final Set<Player> sentOffPlayers;

    // Constants
    private final int regularHalfMinutes = 45;
    private final int halfTimeBreakMinutes = 15;
    private Random random = new Random();

    public enum Type {
        GROUP("GROUP"),
        ROUND_OF_16("1/16"),
        QUARTER("QUARTER"),
        SEMI_FINAL("SEMI-FINAL"),
        FINAL("FINAL");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }


    public Match(Team teamA, Team teamB,
                 List<Player> startingPlayersTeamA, List<Player> substitutePlayersTeamA,
                 List<Player> startingPlayersTeamB, List<Player> substitutePlayersTeamB,
                 boolean isKnockout) {

        if (teamA == null || teamB == null) {
            throw new IllegalArgumentException("Hai đội bóng không được null.");
        }

        if (teamA.equals(teamB)) {
            throw new IllegalArgumentException("Một trận đấu phải có hai đội bóng khác nhau.");
        }

        if (startingPlayersTeamA.size() != 11 || startingPlayersTeamB.size() != 11) {
            throw new IllegalArgumentException("Mỗi đội phải có đúng 11 cầu thủ đá chính.");
        }

        if (substitutePlayersTeamA.size() < 5 || substitutePlayersTeamB.size() < 5) {
            throw new IllegalArgumentException("Mỗi đội phải có ít nhất 5 cầu thủ dự bị.");
        }

        this.teamA = teamA;
        this.teamB = teamB;
        this.teamAId = teamA.getId();
        this.teamBId = teamB.getId();
        
        // Khởi tạo database fields
        this.teamAScore = 0;
        this.teamBScore = 0;
        this.matchType = isKnockout ? "KNOCKOUT" : "GROUP";
        
        // Legacy fields
        this.goalsTeamA = 0;
        this.goalsTeamB = 0;
        this.goals = new ArrayList<>();
        this.yellowCards = 0;
        this.redCards = 0;
        this.substitutions = new ArrayList<>();
        this.isKnockout = isKnockout;
        this.isFinished = false;

        this.startingPlayersTeamA = new ArrayList<>(startingPlayersTeamA);
        this.substitutePlayersTeamA = new ArrayList<>(substitutePlayersTeamA);
        this.startingPlayersTeamB = new ArrayList<>(startingPlayersTeamB);
        this.substitutePlayersTeamB = new ArrayList<>(substitutePlayersTeamB);

        this.sentOffPlayers = new HashSet<>();
    }

    public void addGoal(Goal goal) {
        if (goal == null) {
            throw new IllegalArgumentException("Bàn thắng không được null.");
        }

        Team scoringTeam = goal.getTeam();
        goals.add(goal);

        if (scoringTeam.equals(teamA)) {
            goalsTeamA++;
        } else if (scoringTeam.equals(teamB)) {
            goalsTeamB++;
        } else {
            throw new IllegalArgumentException("Đội ghi bàn không tham gia trận đấu này.");
        }
    }

    public void addCard(Player player, Team team, String cardType) {
        if (player == null || team == null || cardType == null) {
            throw new IllegalArgumentException("Thông tin thẻ không được null.");
        }

        if (!isPlayerInMatch(player)) {
            throw new IllegalArgumentException("Cầu thủ nhận thẻ không thuộc trận đấu này.");
        }

        if ("YELLOW".equalsIgnoreCase(cardType)) {
            player.receiveYellowCard();
            team.setYellowCards(team.getYellowCards() + 1);
        } else if ("RED".equalsIgnoreCase(cardType)) {
            player.receiveRedCard();
            team.setRedCards(team.getRedCards() + 1);
        } else {
            throw new IllegalArgumentException("Loại thẻ không hợp lệ. Chỉ nhận 'YELLOW' hoặc 'RED'.");
        }

        if (player.isSentOff()) {
            removePlayerFromMatch(player, team);
            sentOffPlayers.add(player);
        }
    }

    public boolean addSubstitution(Substitution substitution) {
        if (substitution == null) {
//            throw new IllegalArgumentException("Thông tin thay người không được null.");
            return false;
        }

        Player outPlayer = substitution.getOutPlayer();
        Player inPlayer = substitution.getInPlayer();

        if (!isPlayerInMatch(outPlayer)) {
            return false;
//            throw new IllegalArgumentException("Cầu thủ bị thay ra không thuộc trận đấu.");
        }

        if (isPlayerInMatch(inPlayer)) {
            return false;
//            throw new IllegalArgumentException("Cầu thủ vào sân đã có mặt trong trận.");
        }

        if (startingPlayersTeamA.contains(outPlayer)) {
            startingPlayersTeamA.remove(outPlayer);
            startingPlayersTeamA.add(inPlayer);
            substitutePlayersTeamA.remove(inPlayer);
        } else if (startingPlayersTeamB.contains(outPlayer)) {
            startingPlayersTeamB.remove(outPlayer);
            startingPlayersTeamB.add(inPlayer);
            substitutePlayersTeamB.remove(inPlayer);
        } else {
            return false;
//            throw new IllegalArgumentException("Không tìm thấy cầu thủ bị thay ra trong đội hình chính.");
        }

        substitutions.add(substitution);
        return true;
    }

    //***
    public void makeSubstitution(Substitution substitution) {
        Team team = substitution.getTeam();

        List<Player> startingPlayers;
        List<Player> substitutePlayers;

        if (team.equals(teamA)) {
            startingPlayers = startingPlayersTeamA;
            substitutePlayers = substitutePlayersTeamA;
        } else if (team.equals(teamB)) {
            startingPlayers = startingPlayersTeamB;
            substitutePlayers = substitutePlayersTeamB;
        } else {
            throw new IllegalArgumentException("Đội không tham gia trận đấu.");
        }

        // Kiểm tra đúng 11 cầu thủ đá chính TRƯỚC khi thay người
        if (startingPlayers.size() != 11) {
            throw new IllegalStateException("Đội " + team.getName() + " phải có đúng 11 cầu thủ đá chính trước khi thay người.");
        }

        // Các kiểm tra hợp lệ khác
        if (!startingPlayers.contains(substitution.getOutPlayer())) {
            throw new IllegalArgumentException("Cầu thủ bị thay ra không thuộc danh sách đá chính.");
        }

        if (!substitutePlayers.contains(substitution.getInPlayer())) {
            throw new IllegalArgumentException("Cầu thủ vào sân không thuộc danh sách dự bị.");
        }

        if (getSubstitutionCount(team) >= 3) {
            throw new IllegalArgumentException("Mỗi đội chỉ được thay tối đa 3 cầu thủ.");
        }

        // Thực hiện thay người
        startingPlayers.remove(substitution.getOutPlayer());
        startingPlayers.add(substitution.getInPlayer());
        substitutions.add(substitution);
    }


    public boolean isPlayerInMatch(Player player) {
        return startingPlayersTeamA.contains(player) ||
                substitutePlayersTeamA.contains(player) ||
                startingPlayersTeamB.contains(player) ||
                substitutePlayersTeamB.contains(player);
    }

    public void removePlayerFromMatch(Player player, Team team) {
        if (team.equals(teamA)) {
            startingPlayersTeamA.remove(player);
        } else if (team.equals(teamB)) {
            startingPlayersTeamB.remove(player);
        }
    }

    public void endMatch() {
        isFinished = true;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isKnockout() {
        return isKnockout;
    }

    public int getGoalsTeamA() {
        return goalsTeamA;
    }

    public int getGoalsTeamB() {
        return goalsTeamB;
    }

    public List<Goal> getGoals() {
        return Collections.unmodifiableList(goals);
    }

    public void setYellowCards(int yellowCards) {
        this.yellowCards = yellowCards;
    }

    public int getSubstitutionCount(Team team) {
        int count = 0;
        for (Substitution s : substitutions) {
            if (s.getTeam().equals(team)) {
                count++;
            }
        }
        return count;
    }

    public List<Substitution> getSubstitutions() {
        return Collections.unmodifiableList(substitutions);
    }

    public void addSubstitutionDirect(Substitution substitution) {
        substitutions.add(substitution);
    }

    public int getRegularHalfMinutes() {
        return regularHalfMinutes;
    }

    public int getHalfTimeBreakMinutes() {
        return halfTimeBreakMinutes;
    }

    public boolean isSentOff(Player player) {
        return sentOffPlayers.contains(player);
    }

    public Team getTeamA() {
        return teamA;
    }

    public Team getTeamB() {
        return teamB;
    }
    
    // Getters và Setters cho database fields
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getTeamAId() {
        return teamAId;
    }
    
    public void setTeamAId(int teamAId) {
        this.teamAId = teamAId;
    }
    
    public int getTeamBId() {
        return teamBId;
    }
    
    public void setTeamBId(int teamBId) {
        this.teamBId = teamBId;
    }
    
    public int getTeamAScore() {
        return teamAScore;
    }
    
    public void setTeamAScore(int teamAScore) {
        this.teamAScore = teamAScore;
        this.goalsTeamA = teamAScore; // Sync với legacy field
    }
    
    public int getTeamBScore() {
        return teamBScore;
    }
    
    public void setTeamBScore(int teamBScore) {
        this.teamBScore = teamBScore;
        this.goalsTeamB = teamBScore; // Sync với legacy field
    }
    
    public String getMatchType() {
        return matchType;
    }
    
    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }
    
    public String getMatchDate() {
        return matchDate;
    }
    
    public void setMatchDate(String matchDate) {
        this.matchDate = matchDate;
    }
    

    
    public Integer getWinnerId() {
        return winnerId;
    }
    
    public void setWinnerId(Integer winnerId) {
        this.winnerId = winnerId;
    }
    
    // Phương thức getGroupId và setGroupId đã bị xóa vì cột group_id đã bị loại bỏ
    // Phương thức getRoundNumber và setRoundNumber đã bị xóa vì cột round_number đã bị loại bỏ
    
    /**
     * Cập nhật kết quả trận đấu và tính toán winner bằng Java
     */
    public void updateMatchResult(int teamAScore, int teamBScore) {
        this.teamAScore = teamAScore;
        this.teamBScore = teamBScore;
        this.goalsTeamA = teamAScore;
        this.goalsTeamB = teamBScore;
        
        // Tính toán winner bằng Java logic
        if (teamAScore > teamBScore) {
            this.winnerId = this.teamAId;
        } else if (teamBScore > teamAScore) {
            this.winnerId = this.teamBId;
        } else {
            this.winnerId = null; // Draw
        }
        
        this.isFinished = true;
    }
    
    /**
     * Lấy team thắng cuộc bằng Java logic
     */
    public Team getWinnerTeam() {
        if (winnerId == null) {
            return null; // Draw
        }
        
        if (winnerId == teamAId) {
            return teamA;
        } else if (winnerId == teamBId) {
            return teamB;
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("Match{%s %d - %d %s, finished=%s}", 
                           teamA.getName(), teamAScore, teamBScore, teamB.getName(), isFinished);
    }
}
