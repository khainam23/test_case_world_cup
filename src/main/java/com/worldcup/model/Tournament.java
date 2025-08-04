package com.worldcup.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class Tournament {
    private int id;
    private int year;
    private Team host;
    private LocalDate start;
    private LocalDate end;
    private Team champion;
    private Team runnerUp;
    private Team thirdPlace;

    private List<Team> teamList;
    private List<Group> groupList;
    private List<Player> players;
    private Random random = new Random();

    public int getId() {
        return id;
    }

    public int getYear() {
        return year;
    }

    public Team getHost() {
        return host;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public Team getChampion() {
        return champion;
    }

    public Team getRunnerUp() {
        return runnerUp;
    }

    public Team getThirdPlace() {
        return thirdPlace;
    }

    public List<Team> getTeamList() {
        return teamList;
    }

    public List<Group> getGroupList() {
        return groupList;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Random getRandom() {
        return random;
    }
}

