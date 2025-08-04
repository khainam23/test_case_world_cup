package com.worldcup.model;

// import com.worldcup.constant.Constant;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private int id;
    private String name;
    private List<Team> teamList;
    private List<Match> matches;

    public Group(int id, String name, List<Team> teamList, List<Match> matches) {
        this.id = id;
        this.name = name;
        this.teamList = teamList;
        this.matches = matches;
    }

    public Group(int id, String name) {
        this.id = id;
        this.name = name;
        this.teamList = new ArrayList<>();
        this.matches = new ArrayList<>();
    }


    public void addTeam(Team team) {
        if (team != null && !teamList.contains(team)) {
            teamList.add(team);
        }
    }

    public void addMatch(Match match) {
        if (match != null && !matches.contains(match)) {
            matches.add(match);
        }
    }

    public List<Team> getTeams() {
        return teamList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTeamList(List<Team> teamList) {
        this.teamList = teamList != null ? teamList : new ArrayList<>();
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches != null ? matches : new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Team> getTeamList() {
        return teamList;
    }

    public List<Match> getMatches() {
        return matches;
    }

    @Override
    public String toString() {
        return "Group{" +
                "name='" + name + '\'' +
                ", teamList=" + teamList +
                ", matches=" + matches +
                '}';
    }

    public int getId() {
        return this.id;
    }
}
