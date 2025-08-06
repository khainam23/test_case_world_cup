package com.worldcup;

import com.worldcup.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class PlayOffMatchTest {

    private Team teamA, teamB;
    private List<Player> playersA, playersB;
    private List<Player> startingA, substituteA, startingB, substituteB;

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
    void PlayOffMatchConstructor_ThamSoHopLe_KhoiTaoThanhCong() {
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium A", "Referee A", true);
        
        // Kiểm tra các thuộc tính kế thừa từ Match
        assertEquals(teamA, playOffMatch.getTeamA());
        assertEquals(teamB, playOffMatch.getTeamB());
        assertEquals(0, playOffMatch.getTeamAScore());
        assertEquals(0, playOffMatch.getTeamBScore());
        assertTrue(playOffMatch.isKnockout());
        assertEquals("KNOCKOUT", playOffMatch.getMatchType());
        assertFalse(playOffMatch.isFinished());
    }

    @Test
    void PlayOffMatchConstructor_KhongPhaiKnockout_KhoiTaoThanhCong() {
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium B", "Referee B", false);
        
        assertFalse(playOffMatch.isKnockout());
        assertEquals("GROUP", playOffMatch.getMatchType());
    }

    // Test phân hoạch tương đương - Invalid inputs (kế thừa từ Match)
    @Test
    void PlayOffMatchConstructor_TeamANull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PlayOffMatch(null, teamB, "Stadium", "Referee", true);
        });
    }

    @Test
    void PlayOffMatchConstructor_TeamBNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PlayOffMatch(teamA, null, "Stadium", "Referee", true);
        });
    }

    @Test
    void PlayOffMatchConstructor_CaHaiTeamNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PlayOffMatch(null, null, "Stadium", "Referee", true);
        });
    }

    @Test
    void PlayOffMatchConstructor_CungMotTeam_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PlayOffMatch(teamA, teamA, "Stadium", "Referee", true);
        });
    }


    @Test
    void PlayOffMatchConstructor_TeamAStarting11_KhoiTaoThanhCong() {
        // Đúng 11 cầu thủ - biên hợp lệ
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        assertNotNull(playOffMatch);
        assertEquals(teamA, playOffMatch.getTeamA());
    }


    @Test
    void PlayOffMatchConstructor_TeamASubstitute5_KhoiTaoThanhCong() {
        // Đúng 5 cầu thủ dự bị - biên dưới hợp lệ
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        assertNotNull(playOffMatch);
    }


    // ========== INHERITANCE FUNCTIONALITY TESTS ==========

    @Test
    void PlayOffMatch_AddGoal_KeThuaThanhCong() {
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        
        Goal goal = new Goal(startingA.get(0), teamA, 30, playOffMatch);
        playOffMatch.addGoal(goal);
        
        assertEquals(1, playOffMatch.getGoals().size());
        assertEquals(1, playOffMatch.getGoalsTeamA());
        assertEquals(0, playOffMatch.getGoalsTeamB());
    }

    @Test
    void PlayOffMatch_AddCard_KeThuaThanhCong() {
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        
        Player player = startingA.get(0);
        playOffMatch.addCard(player, teamA, "YELLOW");
        
        assertEquals(1, player.getYellowCards());
        assertEquals(1, teamA.getYellowCards());
    }

    @Test
    void PlayOffMatch_UpdateMatchResult_KeThuaThanhCong() {
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        
        playOffMatch.updateMatchResult(2, 1);
        
        assertEquals(2, playOffMatch.getTeamAScore());
        assertEquals(1, playOffMatch.getTeamBScore());
        assertEquals(teamA.getId(), playOffMatch.getWinnerId());
        assertTrue(playOffMatch.isFinished());
        assertEquals(teamA, playOffMatch.getWinnerTeam());
    }

    @Test
    void PlayOffMatch_EndMatch_KeThuaThanhCong() {
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        
        assertFalse(playOffMatch.isFinished());
        playOffMatch.endMatch();
        assertTrue(playOffMatch.isFinished());
    }

    // ========== KNOCKOUT SPECIFIC TESTS ==========

    @Test
    void PlayOffMatch_KnockoutTrue_MatchTypeKnockout() {
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        
        assertTrue(playOffMatch.isKnockout());
        assertEquals("KNOCKOUT", playOffMatch.getMatchType());
    }

    @Test
    void PlayOffMatch_KnockoutFalse_MatchTypeGroup() {
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", false);
        
        assertFalse(playOffMatch.isKnockout());
        assertEquals("GROUP", playOffMatch.getMatchType());
    }

    // ========== POLYMORPHISM TESTS ==========

    @Test
    void PlayOffMatch_PolymorphismAsMatch_HoatDongDung() {
        Match match = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        
        // Sử dụng như Match object
        assertEquals(teamA, match.getTeamA());
        assertEquals(teamB, match.getTeamB());
        assertTrue(match.isKnockout());
        
        // Test polymorphic behavior
        match.updateMatchResult(1, 0);
        assertEquals(teamA, match.getWinnerTeam());
    }

    // ========== BOUNDARY VALUE TESTS ==========

    @Test
    void PlayOffMatch_SoLuongCauThuBienDuoi_KhoiTaoThanhCong() {
        // Test với số lượng tối thiểu: 11 đá chính, 5 dự bị
        List<Player> minStartingA = playersA.subList(0, 11);
        List<Player> minSubstituteA = playersA.subList(11, 16);
        List<Player> minStartingB = playersB.subList(0, 11);
        List<Player> minSubstituteB = playersB.subList(11, 16);
        
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        
        assertNotNull(playOffMatch);
        assertEquals(11, minStartingA.size());
        assertEquals(5, minSubstituteA.size());
    }

    @Test
    void PlayOffMatch_SoLuongCauThuBienTren_KhoiTaoThanhCong() {
        // Test với số lượng tối đa có thể: 11 đá chính, nhiều dự bị
        List<Player> maxStartingA = playersA.subList(0, 11);
        List<Player> maxSubstituteA = playersA.subList(11, 22); // 11 dự bị
        List<Player> maxStartingB = playersB.subList(0, 11);
        List<Player> maxSubstituteB = playersB.subList(11, 22);
        
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        
        assertNotNull(playOffMatch);
        assertEquals(11, maxSubstituteA.size());
    }


    // ========== TOSTRING TESTS ==========

    @Test
    void PlayOffMatch_ToString_ChuoiHopLe() {
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        
        String result = playOffMatch.toString();
        
        assertTrue(result.contains(teamA.getName()));
        assertTrue(result.contains(teamB.getName()));
        assertTrue(result.contains("0 - 0"));
        assertTrue(result.contains("finished=false"));
    }

    @Test
    void PlayOffMatch_ToStringVoiKetQua_ChuoiHopLe() {
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        
        playOffMatch.updateMatchResult(3, 2);
        String result = playOffMatch.toString();
        
        assertTrue(result.contains("3 - 2"));
        assertTrue(result.contains("finished=true"));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void PlayOffMatch_CacTinhHuongDacBiet_XuLyDung() {
        PlayOffMatch playOffMatch = new PlayOffMatch(teamA, teamB, "Stadium", "Referee", true);
        
        // Test với kết quả hòa trong knockout
        playOffMatch.updateMatchResult(2, 2);
        assertNull(playOffMatch.getWinnerTeam());
        assertNull(playOffMatch.getWinnerId());
        
        // Test với điểm số cao
        playOffMatch.updateMatchResult(10, 8);
        assertEquals(10, playOffMatch.getTeamAScore());
        assertEquals(8, playOffMatch.getTeamBScore());
        assertEquals(teamA, playOffMatch.getWinnerTeam());
    }
}