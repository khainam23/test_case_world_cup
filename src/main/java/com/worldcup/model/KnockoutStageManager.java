package com.worldcup.model;

import java.util.*;

public class KnockoutStageManager {
    // Thông tin các cặp đấu theo thứ tự của BR12
    private Map<String, String> bracketInfo;

    // Danh sách các đội lọt vào từng vòng
    private List<String> roundOf16;
    private List<String> quarterFinals;
    private List<String> semiFinals;
    private List<String> finals;

    // Kết quả chung cuộc
    private String champion;
    private String runnerUp;
    private List<String> bronzeWinners;

    public KnockoutStageManager() {
        this.bracketInfo = new LinkedHashMap<>();
        this.roundOf16 = new ArrayList<>();
        this.quarterFinals = new ArrayList<>();
        this.semiFinals = new ArrayList<>();
        this.finals = new ArrayList<>();
        this.bronzeWinners = new ArrayList<>();
    }

    // Helper method để tìm Group theo tên
    private Group findGroupByName(Map<Group, Team> groupMap, String groupName) {
        for (Group group : groupMap.keySet()) {
            if (group.getName().contains(groupName)) {
                return group;
            }
        }
        return null;
    }

    // Tạo bracket cho vòng 1/16 dựa vào kết quả bảng đấu
    public void generateRoundOf16Bracket(Map<Group, Team> groupWinners, Map<Group, Team> groupRunnersUp) {
        bracketInfo.clear();
        
        Group groupA = findGroupByName(groupWinners, "A");
        Group groupB = findGroupByName(groupWinners, "B");
        Group groupC = findGroupByName(groupWinners, "C");
        Group groupD = findGroupByName(groupWinners, "D");
        Group groupE = findGroupByName(groupWinners, "E");
        Group groupF = findGroupByName(groupWinners, "F");
        Group groupG = findGroupByName(groupWinners, "G");
        Group groupH = findGroupByName(groupWinners, "H");
        
        Team winnerA = groupWinners.get(groupA);
        Team winnerB = groupWinners.get(groupB);
        Team winnerC = groupWinners.get(groupC);
        Team winnerD = groupWinners.get(groupD);
        Team winnerE = groupWinners.get(groupE);
        Team winnerF = groupWinners.get(groupF);
        Team winnerG = groupWinners.get(groupG);
        Team winnerH = groupWinners.get(groupH);
        
        Team runnerB = groupRunnersUp.get(groupB);
        Team runnerA = groupRunnersUp.get(groupA);
        Team runnerD = groupRunnersUp.get(groupD);
        Team runnerC = groupRunnersUp.get(groupC);
        Team runnerF = groupRunnersUp.get(groupF);
        Team runnerE = groupRunnersUp.get(groupE);
        Team runnerH = groupRunnersUp.get(groupH);
        Team runnerG = groupRunnersUp.get(groupG);
        
        bracketInfo.put("Match 1", winnerA + " vs " + runnerB);
        bracketInfo.put("Match 2", winnerB + " vs " + runnerA);
        bracketInfo.put("Match 3", winnerC + " vs " + runnerD);
        bracketInfo.put("Match 4", winnerD + " vs " + runnerC);
        bracketInfo.put("Match 5", winnerE + " vs " + runnerF);
        bracketInfo.put("Match 6", winnerF + " vs " + runnerE);
        bracketInfo.put("Match 7", winnerG + " vs " + runnerH);
        bracketInfo.put("Match 8", winnerH + " vs " + runnerG);
    }

    // Cập nhật danh sách đội thắng vòng 1/16
    public void setRoundOf16Winners(List<String> winners) {
        if (winners.size() != 8) throw new IllegalArgumentException("Phải có đúng 8 đội thắng ở vòng 1/16.");
        this.quarterFinals = new ArrayList<>(winners);
    }

    // Cập nhật đội thắng tứ kết
    public void setQuarterFinalWinners(List<String> winners) {
        if (winners.size() != 4)
            throw new IllegalArgumentException("Cần 4 đội thắng ở tứ kết.");
        this.semiFinals = new ArrayList<>(winners);
    }

    // Cập nhật đội thắng bán kết (chung kết) và đội thua bán kết (đồng hạng 3)
    public void setSemiFinalWinners(List<String> finalists, List<String> semiFinalLosers) {
        if (finalists.size() != 2) throw new IllegalArgumentException("Cần 2 đội vào chung kết.");
        if (semiFinalLosers.size() != 2) throw new IllegalArgumentException("Cần 2 đội thua bán kết.");
        this.finals = new ArrayList<>(finalists);
        this.bronzeWinners = new ArrayList<>(semiFinalLosers); // 2 đội thua bán kết đồng hạng 3
    }

    // Cập nhật kết quả chung kết
    public void setFinalResult(String winner, String loser) {
        this.champion = winner;
        this.runnerUp = loser;
    }

    // Getters
    public Map<String, String> getBracketInfo() {
        return bracketInfo;
    }

    public List<String> getRoundOf16() {
        return roundOf16;
    }

    public List<String> getQuarterFinals() {
        return quarterFinals;
    }

    public List<String> getSemiFinals() {
        return semiFinals;
    }

    public List<String> getFinals() {
        return finals;
    }

    public String getChampion() {
        return champion;
    }

    public String getRunnerUp() {
        return runnerUp;
    }

    public List<String> getBronzeWinners() {
        return bronzeWinners;
    }
}
