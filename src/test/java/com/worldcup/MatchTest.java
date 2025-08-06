package com.worldcup;

import com.worldcup.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class MatchTest {

    private Team teamA, teamB;
    private List<Player> playersA, playersB;
    private List<Player> startingA, substituteA, startingB, substituteB;
    private Match match;

    @BeforeEach
    void setUp() {
        // Tạo cầu thủ cho đội A
        playersA = createPlayerList("PlayerA", 22);
        startingA = playersA.subList(0, 11);
        substituteA = playersA.subList(11, 16);

        // Tạo cầu thủ cho đội B
        playersB = createPlayerList("PlayerB", 22);
        startingB = playersB.subList(0, 11);
        substituteB = playersB.subList(11, 16);

        // Tạo các đội
        teamA = new Team("Brazil", "South America", "Coach A", 
                        List.of("Assistant A"), "Medical A", startingA, substituteA, false);
        teamB = new Team("Argentina", "South America", "Coach B", 
                        List.of("Assistant B"), "Medical B", startingB, substituteB, false);

        // Tạo trận đấu
        match = new Match(teamA, teamB, "Stadium A", "Referee A", false);
    }

    private List<Player> createPlayerList(String baseName, int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            players.add(new Player(baseName + i, i, "Position" + (i % 4)));
        }
        return players;
    }

    // ========== CONSTRUCTOR TESTS ==========

    @Test
    void MatchConstructor_ThamSoHopLe_KhoiTaoThanhCong() {
        Match newMatch = new Match(teamA, teamB, "Stadium A", "Referee A", false);
        
        assertEquals(teamA, newMatch.getTeamA());
        assertEquals(teamB, newMatch.getTeamB());
        assertEquals(0, newMatch.getTeamAScore());
        assertEquals(0, newMatch.getTeamBScore());
        assertEquals("GROUP", newMatch.getMatchType());
        assertFalse(newMatch.isKnockout());
        assertFalse(newMatch.isFinished());
    }

    @Test
    void MatchConstructor_KnockoutMatch_KhoiTaoThanhCong() {
        Match knockoutMatch = new Match(teamA, teamB, "Stadium B", "Referee B", true);
        
        assertEquals("KNOCKOUT", knockoutMatch.getMatchType());
        assertTrue(knockoutMatch.isKnockout());
    }

    // Test phân hoạch tương đương - Invalid inputs
    @Test
    void MatchConstructor_TeamANull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(null, teamB, "Stadium", "Referee", false);
        });
    }

    @Test
    void MatchConstructor_TeamBNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(teamA, null, "Stadium", "Referee", false);
        });
    }

    @Test
    void MatchConstructor_CaHaiTeamNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(null, null, "Stadium", "Referee", false);
        });
    }

    @Test
    void MatchConstructor_CungMotTeam_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(teamA, teamA, "Stadium", "Referee", false);
        });
    }

    // Test phân hoạch giá trị biên cho số lượng cầu thủ dự bị
    @Test
    void MatchConstructor_TeamASubstitute6_ThrowException() {
        List<Player> substitute6 = new ArrayList<>(substituteA);
        substitute6.add(playersA.get(16)); // Add 6th substitute
        Team teamWith6Substitutes = new Team("Brazil6Sub", "South America", "Coach A", 
                                           List.of("Assistant A"), "Medical A", startingA, substitute6, false);
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(teamWith6Substitutes, teamB, "Stadium", "Referee", false);
        });
    }

    @Test
    void MatchConstructor_TeamBSubstitute6_ThrowException() {
        List<Player> substitute6 = new ArrayList<>(substituteB);
        substitute6.add(playersB.get(16)); // Add 6th substitute
        Team teamWith6Substitutes = new Team("Argentina6Sub", "South America", "Coach B", 
                                           List.of("Assistant B"), "Medical B", startingB, substitute6, false);
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(teamA, teamWith6Substitutes, "Stadium", "Referee", false);
        });
    }

    @Test
    void MatchConstructor_TeamASubstitute5_KhoiTaoThanhCong() {
        // Đúng 5 cầu thủ dự bị - biên dưới hợp lệ
        Match validMatch = new Match(teamA, teamB, "Stadium", "Referee", false);
        assertNotNull(validMatch);
    }

    // ========== ADD GOAL TESTS ==========

    @Test
    void AddGoal_GoalHopLe_ThemThanhCong() {
        Goal goal = new Goal(startingA.get(0), teamA, 30, match);
        match.addGoal(goal);
        
        assertEquals(1, match.getGoals().size());
        assertEquals(1, match.getGoalsTeamA());
        assertEquals(0, match.getGoalsTeamB());
        assertTrue(match.getGoals().contains(goal));
    }

    @Test
    void AddGoal_GoalTeamB_ThemThanhCong() {
        Goal goal = new Goal(startingB.get(0), teamB, 45, match);
        match.addGoal(goal);
        
        assertEquals(1, match.getGoals().size());
        assertEquals(0, match.getGoalsTeamA());
        assertEquals(1, match.getGoalsTeamB());
    }

    @Test
    void AddGoal_NhieuGoal_ThemThanhCong() {
        Goal goal1 = new Goal(startingA.get(0), teamA, 10, match);
        Goal goal2 = new Goal(startingB.get(0), teamB, 20, match);
        Goal goal3 = new Goal(startingA.get(1), teamA, 30, match);
        
        match.addGoal(goal1);
        match.addGoal(goal2);
        match.addGoal(goal3);
        
        assertEquals(3, match.getGoals().size());
        assertEquals(2, match.getGoalsTeamA());
        assertEquals(1, match.getGoalsTeamB());
    }

    @Test
    void AddGoal_GoalNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            match.addGoal(null);
        });
    }

    @Test
    void AddGoal_TeamKhongThamGia_ThrowException() {
        // Tạo team thứ 3 không tham gia trận đấu
        List<Player> playersC = createPlayerList("PlayerC", 22);
        List<Player> startingC = playersC.subList(0, 11);
        List<Player> substituteC = playersC.subList(11, 16);
        Team teamC = new Team("Germany", "Europe", "Coach C", 
                             List.of("Assistant C"), "Medical C", startingC, substituteC, false);
        
        Goal invalidGoal = new Goal(playersC.get(0), teamC, 30, match);
        
        assertThrows(IllegalArgumentException.class, () -> {
            match.addGoal(invalidGoal);
        });
    }

    // ========== ADD CARD TESTS ==========

    @Test
    void AddCard_YellowCard_ThemThanhCong() {
        Player player = startingA.get(0);
        int yellowCardsBefore = player.getYellowCards();
        
        match.addCard(player, teamA, "YELLOW");
        
        assertEquals(yellowCardsBefore + 1, player.getYellowCards());
        assertEquals(1, teamA.getYellowCards());
    }

    @Test
    void AddCard_RedCard_ThemThanhCong() {
        Player player = startingA.get(0);
        
        match.addCard(player, teamA, "RED");
        
        assertTrue(player.hasRedCard());
        assertEquals(1, teamA.getRedCards());
        assertTrue(match.isSentOff(player));
    }

    @Test
    void AddCard_2YellowCards_PlayerSentOff() {
        Player player = startingA.get(0);
        
        match.addCard(player, teamA, "YELLOW");
        assertFalse(match.isSentOff(player));
        
        match.addCard(player, teamA, "YELLOW");
        assertTrue(match.isSentOff(player));
        assertTrue(player.isSentOff());
    }

    // Test phân hoạch tương đương cho card type
    @Test
    void AddCard_YellowChuThuong_ThemThanhCong() {
        Player player = startingA.get(0);
        match.addCard(player, teamA, "yellow");
        assertEquals(1, player.getYellowCards());
    }

    @Test
    void AddCard_RedChuThuong_ThemThanhCong() {
        Player player = startingA.get(0);
        match.addCard(player, teamA, "red");
        assertTrue(player.hasRedCard());
    }

    @Test
    void AddCard_PlayerNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            match.addCard(null, teamA, "YELLOW");
        });
    }

    @Test
    void AddCard_TeamNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            match.addCard(startingA.get(0), null, "YELLOW");
        });
    }

    @Test
    void AddCard_CardTypeNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            match.addCard(startingA.get(0), teamA, null);
        });
    }

    @Test
    void AddCard_CardTypeKhongHopLe_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            match.addCard(startingA.get(0), teamA, "BLUE");
        });
    }

    @Test
    void AddCard_PlayerKhongThamGia_ThrowException() {
        // Tạo player không tham gia trận đấu
        Player outsidePlayer = new Player("Outside", 99, "Forward");
        
        assertThrows(IllegalArgumentException.class, () -> {
            match.addCard(outsidePlayer, teamA, "YELLOW");
        });
    }


    @Test
    void MakeSubstitution_TeamKhongThamGia_ThrowException() {
        // Tạo team thứ 3
        List<Player> playersC = createPlayerList("PlayerC", 22);
        List<Player> startingC = playersC.subList(0, 11);
        List<Player> substituteC = playersC.subList(11, 16);
        Team teamC = new Team("Germany", "Europe", "Coach C", 
                             List.of("Assistant C"), "Medical C", startingC, substituteC, false);
        
        Player playerOut = playersC.get(0);
        Player playerIn = playersC.get(11);
        
        assertThrows(IllegalArgumentException.class, () -> {
            Substitution substitution = new Substitution(playerIn, playerOut, 60, teamC, match);
            match.makeSubstitution(substitution);
        });
    }



    // ========== MATCH STATE TESTS ==========

    @Test
    void IsPlayerInMatch_PlayerTrongStarting_TraVeTrue() {
        assertTrue(match.isPlayerInMatch(startingA.get(0)));
        assertTrue(match.isPlayerInMatch(startingB.get(0)));
    }

    @Test
    void IsPlayerInMatch_PlayerKhongThamGia_TraVeFalse() {
        Player outsidePlayer = new Player("Outside", 99, "Forward");
        assertFalse(match.isPlayerInMatch(outsidePlayer));
    }

    @Test
    void EndMatch_KetThucTranDau_TrangThaiThayDoi() {
        assertFalse(match.isFinished());
        match.endMatch();
        assertTrue(match.isFinished());
    }

    @Test
    void UpdateMatchResult_KetQuaHopLe_CapNhatThanhCong() {
        match.updateMatchResult(2, 1);
        
        assertEquals(2, match.getTeamAScore());
        assertEquals(1, match.getTeamBScore());
        assertEquals(teamA.getId(), match.getWinnerId());
        assertTrue(match.isFinished());
    }

    @Test
    void UpdateMatchResult_KetQuaHoa_WinnerNull() {
        match.updateMatchResult(1, 1);
        
        assertEquals(1, match.getTeamAScore());
        assertEquals(1, match.getTeamBScore());
        assertNull(match.getWinnerId());
        assertTrue(match.isFinished());
    }

    @Test
    void UpdateMatchResult_TeamBThang_WinnerLaTeamB() {
        match.updateMatchResult(0, 3);
        
        assertEquals(0, match.getTeamAScore());
        assertEquals(3, match.getTeamBScore());
        assertEquals(teamB.getId(), match.getWinnerId());
    }

    @Test
    void GetWinnerTeam_TeamAThang_TraVeTeamA() {
        match.updateMatchResult(2, 1);
        assertEquals(teamA, match.getWinnerTeam());
    }



    @Test
    void GetWinnerTeam_Hoa_TraVeNull() {
        match.updateMatchResult(1, 1);
        assertNull(match.getWinnerTeam());
    }

    // ========== GETTER/SETTER TESTS ==========


    @Test
    void SetTeamAScore_GiaTriHopLe_SetThanhCong() {
        match.setTeamAScore(3);
        assertEquals(3, match.getTeamAScore());
        assertEquals(3, match.getGoalsTeamA()); // Sync với legacy field
    }

    @Test
    void SetTeamBScore_GiaTriHopLe_SetThanhCong() {
        match.setTeamBScore(2);
        assertEquals(2, match.getTeamBScore());
        assertEquals(2, match.getGoalsTeamB()); // Sync với legacy field
    }

    // Test phân hoạch giá trị biên cho score
    @Test
    void SetScore_GiaTriBang0_SetThanhCong() {
        match.setTeamAScore(0);
        match.setTeamBScore(0);
        assertEquals(0, match.getTeamAScore());
        assertEquals(0, match.getTeamBScore());
    }

    @Test
    void SetScore_GiaTriAm_SetThanhCong() {
        match.setTeamAScore(-1);
        match.setTeamBScore(-2);
        assertEquals(-1, match.getTeamAScore());
        assertEquals(-2, match.getTeamBScore());
    }

    @Test
    void SetScore_GiaTriLon_SetThanhCong() {
        match.setTeamAScore(999);
        match.setTeamBScore(888);
        assertEquals(999, match.getTeamAScore());
        assertEquals(888, match.getTeamBScore());
    }

    // ========== TOSTRING TESTS ==========

    @Test
    void ToString_TranDauChuaKetThuc_ChuoiHopLe() {
        String result = match.toString();
        
        assertTrue(result.contains(teamA.getName()));
        assertTrue(result.contains(teamB.getName()));
        assertTrue(result.contains("0 - 0"));
        assertTrue(result.contains("finished=false"));
    }

    @Test
    void ToString_TranDauDaKetThuc_ChuoiHopLe() {
        match.updateMatchResult(2, 1);
        String result = match.toString();
        
        assertTrue(result.contains("2 - 1"));
        assertTrue(result.contains("finished=true"));
    }

    // ========== INTEGRATION TESTS ==========

}