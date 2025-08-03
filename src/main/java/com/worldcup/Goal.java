package com.worldcup;

public class Goal {
    private Player player;
    private Team team;
    private int minute;
    private Match match;

    public Goal(Player player, Team team, int minute, Match match) {
        if (player == null || team == null || match == null) {
            throw new IllegalArgumentException("Cầu thủ, đội bóng và trận đấu không được null.");
        }
        if (minute < 0 || minute > 150) { // 2 hiệp chính + nghỉ + hiệp phụ tối đa
            throw new IllegalArgumentException("Thời điểm ghi bàn không hợp lệ.");
        }

        this.player = player;
        this.team = team;
        this.minute = minute;
        this.match = match;

        // Cập nhật số bàn thắng cho cầu thủ
        this.player.scoreGoal();
    }

    // Getters
    public Player getPlayer() {
        return player;
    }

    public Player setPlayer() {
        return player; // bổ sung để tương thích với Match.java
    }

    public Team getTeam() {
        return team;
    }

    public int getMinute() {
        return minute;
    }

    public Match getMatch() {
        return match;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "scorer=" + player.getName() +
                ", team=" + team.getName() +
                ", minute=" + minute +
                ", match=" + match +
                '}';
    }
}
