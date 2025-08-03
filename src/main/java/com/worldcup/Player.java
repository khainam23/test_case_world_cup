package com.worldcup;

public class Player {
    private String name;
    private int number;
    private String position;
    private int goals;
    private int yellowCards;
    private boolean redCard;

    public Player(String name, int number, String position) {
        this.name = name;
        this.number = number;
        this.position = position;
        this.goals = 0;
        this.yellowCards = 0;
        this.redCard = false;
    }

    public void scoreGoal() {
        goals++;
    }

    public void receiveYellowCard() {
        yellowCards++;
    }

    public void receiveRedCard() {
        redCard = true;
    }

    public void resetCards() {
        yellowCards = 0;
        redCard = false;
    }

    /**
     * Một cầu thủ bị đuổi khỏi sân nếu:
     * - nhận 2 thẻ vàng trong một trận, hoặc
     * - nhận 1 thẻ đỏ
     */
    public boolean isSentOff() {
        return redCard || yellowCards >= 2;
    }

    public boolean isEligible() {
        return !isSentOff();
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    // Alias method for jersey number (commonly used in football)
    public int getJerseyNumber() {
        return number;
    }

    public String getPosition() {
        return position;
    }

    public int getGoals() {
        return goals;
    }

    public int getYellowCards() {
        return yellowCards;
    }

    public boolean hasRedCard() {
        return redCard;
    }

    // Method to get red card count (0 or 1)
    public int getRedCards() {
        return redCard ? 1 : 0;
    }

    // Setters for database operations
    public void setGoals(int goals) {
        this.goals = goals;
    }

    public void setYellowCards(int yellowCards) {
        this.yellowCards = yellowCards;
    }

    public void setRedCard(boolean redCard) {
        this.redCard = redCard;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", number=" + number +
                ", position='" + position + '\'' +
                ", goals=" + goals +
                ", yellowCards=" + yellowCards +
                ", redCard=" + redCard +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return number == player.number && 
               name.equals(player.name) && 
               position.equals(player.position);
    }

    @Override
    public int hashCode() {
        return name.hashCode() + number * 31 + position.hashCode();
    }
}
