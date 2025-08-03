package com.worldcup;

public class Substitution {
    private final Player playerIn;
    private final Player playerOut;
    private final int minute;
    private final Team team;
    private final Match match;


    public Substitution(Player playerIn, Player playerOut, int minute, Team team, Match match) {
        if (playerIn == null || playerOut == null || team == null || match == null) {
            throw new IllegalArgumentException("Thông tin không được null");
        }

        if (minute < 0 || minute > 150) {
            throw new IllegalArgumentException("Thời điểm thay người không hợp lệ");
        }

        if (!team.getSubstitutePlayers().contains(playerIn)) {
            throw new IllegalArgumentException("Cầu thủ vào không thuộc danh sách dự bị");
        }

        if (!team.getStartingPlayers().contains(playerOut)) {
            throw new IllegalArgumentException("Cầu thủ ra không thuộc danh sách thi đấu chính");
        }

        if (!team.isContainPlayer(playerIn) || !team.isContainPlayer(playerOut)) {
            throw new IllegalArgumentException("Cầu thủ không thuộc đội bóng");
        }

        if (!playerIn.isEligible() || !playerOut.isEligible()) {
            throw new IllegalArgumentException("Cầu thủ không đủ điều kiện thi đấu");
        }

        if (team.getSubstitutionCount() >= 3) {
            throw new IllegalStateException("Đội bóng đã vượt quá số lần thay người cho phép");
        }

        this.playerIn = playerIn;
        this.playerOut = playerOut;
        this.minute = minute;
        this.team = team;
        this.match = match;

        team.incrementSubstitutionCount();

        team.getStartingPlayers().remove(playerOut);
        team.getSubstitutePlayers().remove(playerIn);

        team.getStartingPlayers().add(playerIn);
        team.getSubstitutePlayers().add(playerOut);

        match.addSubstitutionDirect(this);
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
}
