package com.worldcup.model;

public class Substitution {
    private int id;
    private final Match match;
    private final Team team;
    private final Player playerIn;
    private final Player playerOut;
    private final int minute;


    public Substitution(Player playerIn, Player playerOut, int minute, Team team, Match match) {
        if (playerIn == null || playerOut == null || team == null || match == null) {
            throw new IllegalArgumentException("Thông tin không được null");
        }

        if (minute < 0 || minute > 150) {
            throw new IllegalArgumentException("Thời điểm thay người không hợp lệ");
        }

        // Kiểm tra cơ bản - players phải khác nhau
        if (playerIn.equals(playerOut)) {
            throw new IllegalArgumentException("Cầu thủ vào và ra phải khác nhau");
        }

        this.playerIn = playerIn;
        this.playerOut = playerOut;
        this.minute = minute;
        this.team = team;
        this.match = match;

        // Thực hiện thay người trong Match
        boolean success = match.addSubstitution(this);
        if (!success) {
            throw new IllegalArgumentException("Không thể thực hiện thay người: " + 
                                             playerIn.getName() + " vào thay " + playerOut.getName());
        }
    }

    public Player getInPlayer() {
        return playerIn;
    }

    public Player getOutPlayer() {
        return playerOut;
    }

    public int getMinute() {
        return minute;
    }

    public Team getTeam() {
        return team;
    }

    public Match getMatch() {
        return match;
    }

    @Override
    public String toString() {
        return "Substitution{" +
                "playerIn=" + playerIn.getName() +
                ", playerOut=" + playerOut.getName() +
                ", minute=" + minute +
                ", team=" + team.getName() +
                ", match between " + match.getTeamA().getName() + " and " + match.getTeamB().getName() +
                '}';
    }

    public void setId(int substitutionId) {
        id = substitutionId;
    }

    public int getId() {
        return id;
    }
}
