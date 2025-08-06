package com.worldcup.model;

// import com.worldcup.constant.Constant;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private int id;
    private String name;
    private List<Team> teams;
    private List<Match> matches;

    public Group(String name, List<Team> teams, List<Match> matches) {
        this.name = name;
        this.teams = teams;
        this.matches = matches;
    }

    public Group(String name) {
        this.name = name;
        this.teams = new ArrayList<>();
        this.matches = new ArrayList<>();
    }


    public void addTeam(Team team) {
        if (team != null && !teams.contains(team)) {
            teams.add(team);
        }
    }

    public void addMatch(Match match) {
        if (match != null && !matches.contains(match)) {
            matches.add(match);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams != null ? teams : new ArrayList<>();
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches != null ? matches : new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public List<Match> getMatches() {
        return matches;
    }

    @Override
    public String toString() {
        return "Group{" +
                "name='" + name + '\'' +
                ", teamList=" + teams +
                ", matches=" + matches +
                '}';
    }

    public int getId() {
        return this.id;
    }

    public void setId(int groupId) {
        id = groupId;
    }
}
