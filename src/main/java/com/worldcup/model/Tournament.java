package com.worldcup.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class Tournament {
    private int id;
    private int year;
    private String name;
    private Team host;
    private LocalDate start;
    private LocalDate end;
    private Team champion;
    private Team runnerUp;
    private List<Team> thirdPlace;

    private List<Team> teamList;
    private List<Group> groupList;
    private List<Player> players;

    public Tournament(int year, Team host, String name, LocalDate start, LocalDate end) {
        this.year = year;
        this.host = host;
        this.name = name;
        this.start = start;
        this.end = end;
    }

    public Tournament() {

    }

    @Override
    public String toString() {
        return "Tournament{" +
                "id=" + id +
                ", year=" + year +
                ", name='" + name + '\'' +
                ", host=" + host +
                ", start=" + start +
                ", end=" + end +
                ", champion=" + champion +
                ", runnerUp=" + runnerUp +
                ", thirdPlace=" + thirdPlace +
                ", teamList=" + teamList +
                ", groupList=" + groupList +
                ", players=" + players +
                '}';
    }

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

    public List<Team> getThirdPlace() {
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

    public void setId(int id) {
        this.id = id;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHost(Team host) {
        this.host = host;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public void setChampion(Team champion) {
        this.champion = champion;
    }

    public void setRunnerUp(Team runnerUp) {
        this.runnerUp = runnerUp;
    }

    public void setThirdPlace(List<Team> thirdPlace) {
        this.thirdPlace = thirdPlace;
    }

    public void setTeamList(List<Team> teamList) {
        this.teamList = teamList;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Random getRandom() {
        return new Random();
    }
}

