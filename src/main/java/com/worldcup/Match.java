package com.worldcup;

import java.util.*;

public class Match {

    private final Team teamA;
    private final Team teamB;
    private int goalsTeamA;
    private int goalsTeamB;
    private int yellowCards;
    private int redCards;
    private final List<Goal> goals;
    private final List<Substitution> substitutions;
    private final boolean isKnockout;
    private boolean isFinished;
    private boolean redCard;

    private final List<Player> startingPlayersTeamA;
    private final List<Player> startingPlayersTeamB;
    private final List<Player> substitutePlayersTeamA;
    private final List<Player> substitutePlayersTeamB;

    private final Set<Player> sentOffPlayers;

    // Constants
    private final int regularHalfMinutes = 45;
    private final int halfTimeBreakMinutes = 15;

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

        // if (startingPlayersTeamA.size() != 11 || startingPlayersTeamB.size() != 11) {
        //     throw new IllegalArgumentException("Mỗi đội phải có đúng 11 cầu thủ đá chính.");
        // }

        if (substitutePlayersTeamA.size() < 5 || substitutePlayersTeamB.size() < 5) {
            throw new IllegalArgumentException("Mỗi đội phải có ít nhất 5 cầu thủ dự bị.");
        }

        this.teamA = teamA;
        this.teamB = teamB;
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

        Player scorer = goal.getPlayer();
        Team scoringTeam = goal.getTeam();

        if (!isPlayerInMatch(scorer)) {
            throw new IllegalArgumentException("Cầu thủ ghi bàn không thuộc trận đấu này.");
        }

        goals.add(goal);
        scorer.scoreGoal();

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

    public void setRedCard(boolean redCard) {
        this.redCard = redCard;
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
}
