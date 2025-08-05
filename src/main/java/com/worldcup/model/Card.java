package com.worldcup.model;

public class Card {
    private int id;
    private Player player;
    private Team team;
    private Match match;
    private int minutes;
    private CardType type;

    public Card(Player player, Team team, Match match, int minutes, CardType type) {
        if (player == null || team == null || match == null || type == null) {
            throw new IllegalArgumentException("Player, team, match và card type không được null.");
        }
        if (minutes < 0 || minutes > 150) {
            throw new IllegalArgumentException("Thời điểm nhận thẻ không hợp lệ.");
        }

        this.player = player;
        this.team = team;
        this.match = match;
        this.minutes = minutes;
        this.type = type;
    }

    // Getters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public Team getTeam() {
        return team;
    }

    public Match getMatch() {
        return match;
    }

    public int getMinutes() {
        return minutes;
    }

    public CardType getType() {
        return type;
    }

    public enum CardType {
        RED("Red"),
        YELLOW("Yellow");

        private final String label;

        CardType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static CardType fromLabel(String label) {
            for (CardType type : values()) {
                if (type.label.equalsIgnoreCase(label)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No card type for label: " + label);
        }
    }

    @Override
    public String toString() {
        return "Card{" +
                "player=" + player.getName() +
                ", team=" + team.getName() +
                ", minutes=" + minutes +
                ", type=" + type.getLabel() +
                '}';
    }
}
