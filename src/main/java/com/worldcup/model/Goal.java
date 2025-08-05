package com.worldcup.model;

import com.worldcup.repository.GoalRepository;

public class Goal {
    private int id;
    private Player player;
    private Team team;
    private int minute;
    private Match match;
    private GoalType type;
    
    // Repository for persistence operations
    private static GoalRepository goalRepository;

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

    public enum GoalType {
        NORMAL("Normal"),
        PENALTY("Penalty"),
        OWN_GOAL("Own Goal"),
        HEADER("Header"),
        FREE_KICK("Free Kick"),
        LONG_SHOT("Long Shot");

        private final String label;

        GoalType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static GoalType fromLabel(String label) {
            for (GoalType type : values()) {
                if (type.label.equalsIgnoreCase(label)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No goal type for label: " + label);
        }
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
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public GoalType getType() {
        return type;
    }
    
    public void setType(GoalType type) {
        this.type = type;
    }
    
    /**
     * Set repository for persistence operations
     */
    public static void setGoalRepository(GoalRepository repository) {
        goalRepository = repository;
    }
    
    /**
     * Save this goal to database using repository
     */
    public void save() throws Exception {
        if (goalRepository != null) {
            goalRepository.save(this);
        }
    }
    
    /**
     * Update this goal in database using repository
     */
    public void update() throws Exception {
        if (goalRepository != null && id > 0) {
            goalRepository.update(this);
        }
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
