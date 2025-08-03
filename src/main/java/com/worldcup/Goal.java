package com.worldcup;

public class Goal {
    private Player scorer;
    private Team team;
    private int minute;
    private Match match;

    public Goal(Player scorer, Team team, int minute, Match match) {
        if (scorer == null || team == null || match == null) {
            throw new IllegalArgumentException("Cầu thủ, đội bóng và trận đấu không được null.");
        }
        if (minute < 0 || minute > 150) { // 2 hiệp chính + nghỉ + hiệp phụ tối đa
            throw new IllegalArgumentException("Thời điểm ghi bàn không hợp lệ.");
        }

        this.scorer = scorer;
        this.team = team;
        this.minute = minute;
        this.match = match;

        // Cập nhật số bàn thắng cho cầu thủ
        this.scorer.scoreGoal();
    }

    // Getters
    public Player getScorer() {
        return scorer;
    }

    public Player getPlayer() {
        return scorer; // bổ sung để tương thích với Match.java
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
                "scorer=" + scorer.getName() +
                ", team=" + team.getName() +
                ", minute=" + minute +
                ", match=" + match +
                '}';
    }
}
