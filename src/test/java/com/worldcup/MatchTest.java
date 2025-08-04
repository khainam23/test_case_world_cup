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
                        List.of("Assistant A"), "Medical A", playersA, false);
        teamB = new Team("Argentina", "South America", "Coach B", 
                        List.of("Assistant B"), "Medical B", playersB, false);

        // Tạo trận đấu
        match = new Match(teamA, teamB, startingA, substituteA, startingB, substituteB, false);
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
        Match newMatch = new Match(teamA, teamB, startingA, substituteA, startingB, substituteB, false);
        
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
        Match knockoutMatch = new Match(teamA, teamB, startingA, substituteA, startingB, substituteB, true);
        
        assertEquals("KNOCKOUT", knockoutMatch.getMatchType());
        assertTrue(knockoutMatch.isKnockout());
    }

    // Test phân hoạch tương đương - Invalid inputs
    @Test
    void MatchConstructor_TeamANull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(null, teamB, startingA, substituteA, startingB, substituteB, false);
        });
    }

    @Test
    void MatchConstructor_TeamBNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(teamA, null, startingA, substituteA, startingB, substituteB, false);
        });
    }

    @Test
    void MatchConstructor_CaHaiTeamNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(null, null, startingA, substituteA, startingB, substituteB, false);
        });
    }

    @Test
    void MatchConstructor_CungMotTeam_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(teamA, teamA, startingA, substituteA, startingB, substituteB, false);
        });
    }

    // Test phân hoạch giá trị biên cho số lượng cầu thủ đá chính
    @Test
    void MatchConstructor_TeamAStarting10_ThrowException() {
        List<Player> starting10 = startingA.subList(0, 10);
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(teamA, teamB, starting10, substituteA, startingB, substituteB, false);
        });
    }

    @Test
    void MatchConstructor_TeamAStarting12_ThrowException() {
        List<Player> starting12 = new ArrayList<>(startingA);
        starting12.add(playersA.get(16));
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(teamA, teamB, starting12, substituteA, startingB, substituteB, false);
        });
    }

    @Test
    void MatchConstructor_TeamBStarting0_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(teamA, teamB, startingA, substituteA, new ArrayList<>(), substituteB, false);
        });
    }

    // Test phân hoạch giá trị biên cho số lượng cầu thủ dự bị
    @Test
    void MatchConstructor_TeamASubstitute4_ThrowException() {
        List<Player> substitute4 = substituteA.subList(0, 4);
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(teamA, teamB, startingA, substitute4, startingB, substituteB, false);
        });
    }

    @Test
    void MatchConstructor_TeamBSubstitute0_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Match(teamA, teamB, startingA, substituteA, startingB, new ArrayList<>(), false);
        });
    }

    @Test
    void MatchConstructor_TeamASubstitute5_KhoiTaoThanhCong() {
        // Đúng 5 cầu thủ dự bị - biên dưới hợp lệ
        Match validMatch = new Match(teamA, teamB, startingA, substituteA, startingB, substituteB, false);
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
        Team teamC = new Team("Germany", "Europe", "Coach C", 
                             List.of("Assistant C"), "Medical C", playersC, false);
        
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

    // ========== SUBSTITUTION TESTS ==========

    @Test
    void AddSubstitution_SubstitutionHopLe_ThemThanhCong() {
        Player playerOut = startingA.get(0);
        Player playerIn = substituteA.get(0);
        Substitution substitution = new Substitution(playerIn, playerOut, 60, teamA, match);
        
        boolean result = match.addSubstitution(substitution);
        
        assertTrue(result);
        assertEquals(1, match.getSubstitutions().size());
    }

    @Test
    void AddSubstitution_SubstitutionNull_TraVeFalse() {
        boolean result = match.addSubstitution(null);
        assertFalse(result);
    }

    @Test
    void AddSubstitution_PlayerOutKhongThamGia_TraVeFalse() {
        Player outsidePlayer = new Player("Outside", 99, "Forward");
        Player playerIn = substituteA.get(0);
        Substitution substitution = new Substitution(playerIn, outsidePlayer, 60, teamA, match);
        
        boolean result = match.addSubstitution(substitution);
        assertFalse(result);
    }

    @Test
    void AddSubstitution_PlayerInDaThamGia_TraVeFalse() {
        Player playerOut = startingA.get(0);
        Player playerIn = startingA.get(1); // Player đã trong đội hình chính
        Substitution substitution = new Substitution(playerIn, playerOut, 60, teamA, match);
        
        boolean result = match.addSubstitution(substitution);
        assertFalse(result);
    }

    // ========== MAKE SUBSTITUTION TESTS ==========

    @Test
    void MakeSubstitution_SubstitutionHopLe_ThayNguoiThanhCong() {
        Player playerOut = startingA.get(0);
        Player playerIn = substituteA.get(0);
        Substitution substitution = new Substitution(playerIn, playerOut, 60, teamA, match);
        
        // Reset lại trạng thái vì constructor đã thực hiện thay người
        // Tạo match mới để test makeSubstitution
        Match newMatch = new Match(teamA, teamB, new ArrayList<>(startingA), 
                                  new ArrayList<>(substituteA), new ArrayList<>(startingB), 
                                  new ArrayList<>(substituteB), false);
        
        Substitution newSubstitution = new Substitution(playerIn, playerOut, 60, teamA, newMatch);
        
        assertEquals(1, newMatch.getSubstitutions().size());
    }

    @Test
    void MakeSubstitution_TeamKhongThamGia_ThrowException() {
        // Tạo team thứ 3
        List<Player> playersC = createPlayerList("PlayerC", 22);
        Team teamC = new Team("Germany", "Europe", "Coach C", 
                             List.of("Assistant C"), "Medical C", playersC, false);
        
        Player playerOut = playersC.get(0);
        Player playerIn = playersC.get(11);
        
        assertThrows(IllegalArgumentException.class, () -> {
            Substitution substitution = new Substitution(playerIn, playerOut, 60, teamC, match);
        });
    }

    @Test
    void MakeSubstitution_QuaSoLuongThayNguoi_ThrowException() {
        // Thực hiện 3 lần thay người hợp lệ trước
        for (int i = 0; i < 3; i++) {
            Player playerOut = startingA.get(i);
            Player playerIn = substituteA.get(i);
            match.makeSubstitution(new Substitution(playerIn, playerOut, 60 + i, teamA, match));
        }
        
        // Lần thay người thứ 4 sẽ throw exception
        Player playerOut = startingA.get(3);
        Player playerIn = substituteA.get(3);
        
        assertThrows(IllegalArgumentException.class, () -> {
            match.makeSubstitution(new Substitution(playerIn, playerOut, 90, teamA, match));
        });
    }

    // ========== MATCH STATE TESTS ==========

    @Test
    void IsPlayerInMatch_PlayerTrongStarting_TraVeTrue() {
        assertTrue(match.isPlayerInMatch(startingA.get(0)));
        assertTrue(match.isPlayerInMatch(startingB.get(0)));
    }

    @Test
    void IsPlayerInMatch_PlayerTrongSubstitute_TraVeTrue() {
        assertTrue(match.isPlayerInMatch(substituteA.get(0)));
        assertTrue(match.isPlayerInMatch(substituteB.get(0)));
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
    void GetWinnerTeam_TeamBThang_TraVeTeamB() {
        match.updateMatchResult(1, 2);
        assertEquals(teamB, match.getWinnerTeam());
    }

    @Test
    void GetWinnerTeam_Hoa_TraVeNull() {
        match.updateMatchResult(1, 1);
        assertNull(match.getWinnerTeam());
    }

    // ========== GETTER/SETTER TESTS ==========

    @Test
    void GetSubstitutionCount_TeamA_DemDung() {
        assertEquals(0, match.getSubstitutionCount(teamA));
        
        // Thực hiện 2 lần thay người
        Player playerOut1 = startingA.get(0);
        Player playerIn1 = substituteA.get(0);
        match.makeSubstitution(new Substitution(playerIn1, playerOut1, 60, teamA, match));
        
        Player playerOut2 = startingA.get(1);
        Player playerIn2 = substituteA.get(1);
        match.makeSubstitution(new Substitution(playerIn2, playerOut2, 70, teamA, match));
        
        assertEquals(2, match.getSubstitutionCount(teamA));
        assertEquals(0, match.getSubstitutionCount(teamB));
    }

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

    @Test
    void Match_TinhNangTichHop_HoatDongDung() {
        // Thêm bàn thắng
        Goal goal1 = new Goal(startingA.get(0), teamA, 30, match);
        match.addGoal(goal1);
        
        // Thêm thẻ
        match.addCard(startingB.get(0), teamB, "YELLOW");
        
        // Thay người
        Player playerOut = startingA.get(1);
        Player playerIn = substituteA.get(0);
        match.makeSubstitution(new Substitution(playerIn, playerOut, 60, teamA, match));
        
        // Kết thúc trận đấu
        match.updateMatchResult(1, 0);
        
        // Kiểm tra trạng thái cuối
        assertEquals(1, match.getGoals().size());
        assertEquals(1, match.getGoalsTeamA());
        assertEquals(1, teamB.getYellowCards());
        assertEquals(1, match.getSubstitutionCount(teamA));
        assertTrue(match.isFinished());
        assertEquals(teamA, match.getWinnerTeam());
    }

    @Test
    void Match_BienGioiHan_XuLyDung() {
        // Test với số lượng tối đa thay người
        for (int i = 0; i < 3; i++) {
            Player playerOut = startingA.get(i);
            Player playerIn = substituteA.get(i);
            match.makeSubstitution(new Substitution(playerIn, playerOut, 60 + i, teamA, match));
        }
        
        assertEquals(3, match.getSubstitutionCount(teamA));
        
        // Test với nhiều bàn thắng
        for (int i = 0; i < 10; i++) {
            Goal goal = new Goal(startingA.get(i % 11), teamA, 10 + i, match);
            match.addGoal(goal);
        }
        
        assertEquals(10, match.getGoalsTeamA());
    }
}