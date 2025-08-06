package com.worldcup.model;

public class Player {
    // Thuộc tính tương ứng với database schema
    private int id;
    private String name;
    private int jerseyNumber; // jersey_number trong DB
    private String position;
    private int teamId;
    private boolean isStarting;
    private int yellowCards;
    private int redCards; // Đổi từ boolean thành int để tương ứng với DB
    private int goals;
    private int assists;
    private int minutesPlayed;
    private boolean isEligible;
    private int eligibleMatches;

    public Player(String name, int jerseyNumber, String position) {
        this.name = name;
        this.jerseyNumber = jerseyNumber;
        this.position = position;
        this.goals = 0;
        this.yellowCards = 0;
        this.redCards = 0;
        this.assists = 0;
        this.minutesPlayed = 0;
        this.isEligible = true;
        this.isStarting = false;
        this.eligibleMatches = 0;
    }

    public void scoreGoal() {
        goals++;
    }

    public void receiveYellowCard() {
        yellowCards++;
    }

    public void receiveRedCard() {
        redCards++;
        isEligible = true;
        eligibleMatches = 1;
    }

    public void resetCards() {
        yellowCards = 0;
        redCards = 0;
    }

    /**
     * Một cầu thủ bị đuổi khỏi sân nếu:
     * - nhận 2 thẻ vàng trong một trận, hoặc
     * - nhận 1 thẻ đỏ
     */
    public boolean isSentOff() {
        return redCards > 0 || yellowCards >= 2;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getJerseyNumber() {
        return jerseyNumber;
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
        return redCards > 0;
    }

    public int getRedCards() {
        return redCards;
    }

    // Setters for database operations
    public void setGoals(int goals) {
        this.goals = goals;
    }

    public void setYellowCards(int yellowCards) {
        this.yellowCards = yellowCards;
    }

    public void setRedCard(boolean hasRedCard) {
        this.redCards = hasRedCard ? 1 : 0;
    }

    public void setRedCards(int redCards) {
        this.redCards = redCards;
    }

    // Getters và Setters cho các thuộc tính mới
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public boolean isStarting() {
        return isStarting;
    }

    public void setStarting(boolean starting) {
        isStarting = starting;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public int getMinutesPlayed() {
        return minutesPlayed;
    }

    public void setMinutesPlayed(int minutesPlayed) {
        this.minutesPlayed = minutesPlayed;
    }


    public void setEligible(boolean eligible) {
        isEligible = eligible;
    }

    public void clearEligible() {
        isEligible = false;
        eligibleMatches = 0;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", jerseyNumber=" + jerseyNumber +
                ", position='" + position + '\'' +
                ", goals=" + goals +
                ", yellowCards=" + yellowCards +
                ", redCards=" + redCards +
                ", isEligible=" + isEligible +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return jerseyNumber == player.jerseyNumber &&
                name.equals(player.name) &&
                position.equals(player.position);
    }

    @Override
    public int hashCode() {
        return name.hashCode() + jerseyNumber * 31 + position.hashCode();
    }

    public int getNumber() {
        return this.jerseyNumber;
    }

    public void setJerseyNumber(int jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public boolean isEligible() {
        return isEligible;
    }

    public int getEligibleMatches() {
        return eligibleMatches;
    }

    public void setEligibleMatches(int eligibleMatches) {
        this.eligibleMatches = eligibleMatches;
    }
}
