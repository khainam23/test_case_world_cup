package com.worldcup;

public class Player {

    private String name;
    private int number;
    private String position;
    private String teamName;
    private int goals;
    private int yellowCards;
    private boolean redCard;

    public Player(String name, int number, String position, String teamName) {
        this.name = name;
        this.number = number;
        this.position = position;
        this.teamName = teamName;
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

    public String getPosition() {
        return position;
    }

    public String getTeamName() {
        return teamName;
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
    
}
