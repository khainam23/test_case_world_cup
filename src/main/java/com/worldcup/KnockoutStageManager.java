package com.worldcup;

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

    // Tạo bracket cho vòng 1/16 dựa vào kết quả bảng đấu
    public void generateRoundOf16Bracket(Map<String, String> groupWinners, Map<String, String> groupRunnersUp) {
        bracketInfo.clear();
        bracketInfo.put("Match 1", groupWinners.get("A") + " vs " + groupRunnersUp.get("B"));
        bracketInfo.put("Match 2", groupWinners.get("B") + " vs " + groupRunnersUp.get("A"));
        bracketInfo.put("Match 3", groupWinners.get("C") + " vs " + groupRunnersUp.get("D"));
        bracketInfo.put("Match 4", groupWinners.get("D") + " vs " + groupRunnersUp.get("C"));
        bracketInfo.put("Match 5", groupWinners.get("E") + " vs " + groupRunnersUp.get("F"));
        bracketInfo.put("Match 6", groupWinners.get("F") + " vs " + groupRunnersUp.get("E"));
        bracketInfo.put("Match 7", groupWinners.get("G") + " vs " + groupRunnersUp.get("H"));
        bracketInfo.put("Match 8", groupWinners.get("H") + " vs " + groupRunnersUp.get("G"));
    }

    // Cập nhật danh sách đội thắng vòng 1/16
    public void setRoundOf16Winners(List<String> winners) {
        if (winners.size() != 8) throw new IllegalArgumentException("Phải có đúng 8 đội thắng ở vòng 1/16.");
        this.quarterFinals = new ArrayList<>(winners);
    }

    // Cập nhật đội thắng tứ kết
    public void setQuarterFinalWinners(List<String> winners, List<String> losers) {
        if (winners.size() != 4 || losers.size() != 4)
            throw new IllegalArgumentException("Cần 4 đội thắng và 4 đội thua ở tứ kết.");
        this.semiFinals = new ArrayList<>(winners);
        this.bronzeWinners = new ArrayList<>(losers); // 2 đội thua tứ kết nhận huy chương đồng
    }

    // Cập nhật đội thắng bán kết (chung kết)
    public void setSemiFinalWinners(List<String> finalists) {
        if (finalists.size() != 2) throw new IllegalArgumentException("Cần 2 đội vào chung kết.");
        this.finals = new ArrayList<>(finalists);
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
