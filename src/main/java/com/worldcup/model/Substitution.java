package com.worldcup.model;

import java.util.Objects;

public class Substitution {
    private int id;
    private Match match;
    private Team team;
    private Player playerIn;
    private Player playerOut;
    private int minute;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Substitution)) return false;
        Substitution that = (Substitution) o;
        return id == that.id && minute == that.minute && Objects.equals(match, that.match) && Objects.equals(team, that.team) && Objects.equals(playerIn, that.playerIn) && Objects.equals(playerOut, that.playerOut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, match, team, playerIn, playerOut, minute);
    }

    public void setId(int substitutionId) {
        id = substitutionId;
    }

    public int getId() {
        return id;
    }
}
