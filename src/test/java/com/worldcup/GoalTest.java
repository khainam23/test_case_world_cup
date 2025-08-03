package com.worldcup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GoalTest {

    private Team teamA, teamB;
    private Match match;
    private Player p1, p2, p3, p4, p5;
    private List<Player> startingPlayersA, substitutePlayersA;
    private List<Player> startingPlayersB, substitutePlayersB;

    @BeforeEach
    public void setup() {
        // Tạo cầu thủ
        p1 = new Player("Mbappe", 10, "Forward", "France");
        p2 = new Player("Griezmann", 7, "Midfielder", "France");
        p3 = new Player("Giroud", 9, "Forward", "France");
        p4 = new Player("Benzema", 19, "Forward", "France");
        p5 = new Player("Pogba", 6, "Midfielder", "France");

        // Tạo danh sách cầu thủ cho đội
        List<Player> playersA = generatePlayers(22, "France");
        List<Player> playersB = generatePlayers(22, "Brazil");
        
        teamA = new Team("France", "Europe", "Coach A", Arrays.asList("A1", "A2"), "Doctor A", playersA, false);
        teamB = new Team("Brazil", "South America", "Coach B", Arrays.asList("B1", "B2"), "Doctor B", playersB, false);
        
        // Tạo danh sách cầu thủ đá chính và dự bị
        startingPlayersA = playersA.subList(0, 11);
        substitutePlayersA = playersA.subList(11, 22);
        startingPlayersB = playersB.subList(0, 11);
        substitutePlayersB = playersB.subList(11, 22);
        
        match = new Match(teamA, teamB, startingPlayersA, substitutePlayersA, startingPlayersB, substitutePlayersB, false);
    }

    private List<Player> generatePlayers(int count, String teamName) {
        List<Player> players = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            players.add(new Player("Player" + i, i, "Position" + (i % 4), teamName));
        }
        return players;
    }

    // ========== BÀO PHỦ PHÂN HOẠCH TƯƠNG ĐƯƠNG ==========

    // Phân hoạch 1: Constructor với tham số hợp lệ
    @Test
    public void GoalConstructor_TatCaThamSoHopLe_KhoiTaoThanhCong() {
        Goal goal = new Goal(p1, teamA, 45, match);
        
        assertEquals(p1, goal.getScorer());
        assertEquals(teamA, goal.getTeam());
        assertEquals(45, goal.getMinute());
        assertEquals(match, goal.getMatch());
    }

    // Phân hoạch 2: Constructor với Player null
    @Test
    public void GoalConstructor_PlayerNull_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(null, teamA, 45, match);
        });
        assertEquals("Cầu thủ, đội bóng và trận đấu không được null.", thrown.getMessage());
    }

    // Phân hoạch 3: Constructor với Team null
    @Test
    public void GoalConstructor_TeamNull_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(p1, null, 45, match);
        });
        assertEquals("Cầu thủ, đội bóng và trận đấu không được null.", thrown.getMessage());
    }

    // Phân hoạch 4: Constructor với Match null
    @Test
    public void GoalConstructor_MatchNull_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(p1, teamA, 45, null);
        });
        assertEquals("Cầu thủ, đội bóng và trận đấu không được null.", thrown.getMessage());
    }

    // Phân hoạch 5: Constructor với nhiều tham số null
    @Test
    public void GoalConstructor_NhieuThamSoNull_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(null, null, 45, null);
        });
        assertEquals("Cầu thủ, đội bóng và trận đấu không được null.", thrown.getMessage());
    }

    // Phân hoạch 6: Minute trong khoảng hợp lệ [0, 150]
    @Test
    public void GoalConstructor_MinuteTrongKhoangHopLe_KhoiTaoThanhCong() {
        Goal goal = new Goal(p1, teamA, 75, match);
        assertEquals(75, goal.getMinute());
    }

    // Phân hoạch 7: Minute ngoài khoảng hợp lệ (< 0)
    @Test
    public void GoalConstructor_MinuteNhoHonKhong_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(p1, teamA, -1, match);
        });
        assertEquals("Thời điểm ghi bàn không hợp lệ.", thrown.getMessage());
    }

    // Phân hoạch 8: Minute ngoài khoảng hợp lệ (> 150)
    @Test
    public void GoalConstructor_MinuteLonHon150_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(p1, teamA, 151, match);
        });
        assertEquals("Thời điểm ghi bàn không hợp lệ.", thrown.getMessage());
    }

    // ========== BÀO PHỦ PHÂN HOẠCH GIÁ TRỊ BIÊN ==========

    // Giá trị biên dưới: minute = 0
    @Test
    public void GoalConstructor_MinuteBangKhong_KhoiTaoThanhCong() {
        Goal goal = new Goal(p1, teamA, 0, match);
        assertEquals(0, goal.getMinute());
    }

    // Giá trị biên trên: minute = 150
    @Test
    public void GoalConstructor_MinuteBang150_KhoiTaoThanhCong() {
        Goal goal = new Goal(p1, teamA, 150, match);
        assertEquals(150, goal.getMinute());
    }

    // Giá trị biên dưới - 1: minute = -1
    @Test
    public void GoalConstructor_MinuteBangAmMot_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(p1, teamA, -1, match);
        });
        assertEquals("Thời điểm ghi bàn không hợp lệ.", thrown.getMessage());
    }

    // Giá trị biên trên + 1: minute = 151
    @Test
    public void GoalConstructor_MinuteBang151_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(p1, teamA, 151, match);
        });
        assertEquals("Thời điểm ghi bàn không hợp lệ.", thrown.getMessage());
    }

    // Giá trị biên dưới + 1: minute = 1
    @Test
    public void GoalConstructor_MinuteBangMot_KhoiTaoThanhCong() {
        Goal goal = new Goal(p1, teamA, 1, match);
        assertEquals(1, goal.getMinute());
    }

    // Giá trị biên trên - 1: minute = 149
    @Test
    public void GoalConstructor_MinuteBang149_KhoiTaoThanhCong() {
        Goal goal = new Goal(p1, teamA, 149, match);
        assertEquals(149, goal.getMinute());
    }

    // ========== BÀO PHỦ CÂU LỆNH VÀ NHÁNH ==========

    // Test nhánh if đầu tiên: scorer == null
    @Test
    public void GoalConstructor_ScorerNull_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(null, teamA, 45, match);
        });
        assertEquals("Cầu thủ, đội bóng và trận đấu không được null.", thrown.getMessage());
    }

    // Test nhánh if đầu tiên: team == null
    @Test
    public void GoalConstructor_TeamNullTrongNhanh_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(p1, null, 45, match);
        });
        assertEquals("Cầu thủ, đội bóng và trận đấu không được null.", thrown.getMessage());
    }

    // Test nhánh if đầu tiên: match == null
    @Test
    public void GoalConstructor_MatchNullTrongNhanh_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(p1, teamA, 45, null);
        });
        assertEquals("Cầu thủ, đội bóng và trận đấu không được null.", thrown.getMessage());
    }

    // Test nhánh if thứ hai: minute < 0
    @Test
    public void GoalConstructor_MinuteNhoHonKhongTrongNhanh_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(p1, teamA, -5, match);
        });
        assertEquals("Thời điểm ghi bàn không hợp lệ.", thrown.getMessage());
    }

    // Test nhánh if thứ hai: minute > 150
    @Test
    public void GoalConstructor_MinuteLonHon150TrongNhanh_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(p1, teamA, 200, match);
        });
        assertEquals("Thời điểm ghi bàn không hợp lệ.", thrown.getMessage());
    }

    // Test đường thành công (không có exception)
    @Test
    public void GoalConstructor_DuongThanhCong_KhoiTaoThanhCong() {
        int initialGoals = p1.getGoals();
        Goal goal = new Goal(p1, teamA, 45, match);
        
        // Kiểm tra tất cả thuộc tính được gán đúng
        assertEquals(p1, goal.getScorer());
        assertEquals(teamA, goal.getTeam());
        assertEquals(45, goal.getMinute());
        assertEquals(match, goal.getMatch());
        
        // Kiểm tra scorer.scoreGoal() được gọi
        assertEquals(initialGoals + 1, p1.getGoals());
    }

    // ========== TEST CÁC PHƯƠNG THỨC GETTER ==========

    @Test
    public void GetScorer_GoiTrenGoalHopLe_TraVePlayer() {
        Goal goal = new Goal(p2, teamA, 30, match);
        assertEquals(p2, goal.getScorer());
    }

    @Test
    public void GetPlayer_GoiTrenGoalHopLe_TraVePlayer() {
        Goal goal = new Goal(p3, teamA, 60, match);
        assertEquals(p3, goal.getPlayer());
    }

    @Test
    public void GetTeam_GoiTrenGoalHopLe_TraVeTeam() {
        Goal goal = new Goal(p1, teamB, 90, match);
        assertEquals(teamB, goal.getTeam());
    }

    @Test
    public void GetMinute_GoiTrenGoalHopLe_TraVeMinute() {
        Goal goal = new Goal(p1, teamA, 120, match);
        assertEquals(120, goal.getMinute());
    }

    @Test
    public void GetMatch_GoiTrenGoalHopLe_TraVeMatch() {
        Goal goal = new Goal(p1, teamA, 45, match);
        assertEquals(match, goal.getMatch());
    }

    // ========== TEST PHƯƠNG THỨC toString ==========

    @Test
    public void ToString_GoiTrenGoalHopLe_ChuaCacThongTinCanThiet() {
        Goal goal = new Goal(p1, teamA, 45, match);
        String result = goal.toString();
        
        assertTrue(result.contains("Goal{"));
        assertTrue(result.contains("scorer=Mbappe"));
        assertTrue(result.contains("team=France"));
        assertTrue(result.contains("minute=45"));
        assertTrue(result.contains("}"));
    }

    // ========== TEST TƯƠNG TÁC VỚI PLAYER ==========

    @Test
    public void GoalConstructor_TaoGoal_TangSoBanThangCuaPlayer() {
        int initialGoals = p1.getGoals();
        new Goal(p1, teamA, 45, match);
        assertEquals(initialGoals + 1, p1.getGoals());
    }

    @Test
    public void GoalConstructor_TaoNhieuGoal_TangSoBanThangDungCach() {
        int initialGoals = p1.getGoals();
        new Goal(p1, teamA, 10, match);
        new Goal(p1, teamA, 20, match);
        new Goal(p1, teamA, 30, match);
        assertEquals(initialGoals + 3, p1.getGoals());
    }

    // ========== TEST CÁC TRƯỜNG HỢP ĐẶC BIỆT ==========

    @Test
    public void GoalConstructor_MinuteGiuaKhoang_KhoiTaoThanhCong() {
        Goal goal = new Goal(p1, teamA, 75, match);
        assertEquals(75, goal.getMinute());
    }

    @Test
    public void GoalConstructor_PlayerKhacNhau_KhoiTaoThanhCong() {
        Goal goal1 = new Goal(p1, teamA, 10, match);
        Goal goal2 = new Goal(p2, teamA, 20, match);
        
        assertEquals(p1, goal1.getScorer());
        assertEquals(p2, goal2.getScorer());
    }

    @Test
    public void GoalConstructor_TeamKhacNhau_KhoiTaoThanhCong() {
        Goal goal1 = new Goal(p1, teamA, 10, match);
        Goal goal2 = new Goal(p2, teamB, 20, match);
        
        assertEquals(teamA, goal1.getTeam());
        assertEquals(teamB, goal2.getTeam());
    }

    // ========== TEST TÍNH NHẤT QUÁN CỦA GETTER VÀ CONSTRUCTOR ==========

    @Test
    public void GetPlayerVaGetScorer_CungGoal_TraVeCungKetQua() {
        Goal goal = new Goal(p1, teamA, 45, match);
        assertEquals(goal.getScorer(), goal.getPlayer());
        assertSame(goal.getScorer(), goal.getPlayer());
    }

    // ========== TEST CÁC GIÁ TRỊ BIÊN KHÁC ==========

    @Test
    public void GoalConstructor_MinuteAmLon_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(p1, teamA, -999, match);
        });
        assertEquals("Thời điểm ghi bàn không hợp lệ.", thrown.getMessage());
    }

    @Test
    public void GoalConstructor_MinuteDuongLon_ThrowException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new Goal(p1, teamA, 999, match);
        });
        assertEquals("Thời điểm ghi bàn không hợp lệ.", thrown.getMessage());
    }

    // ========== TEST IMMUTABILITY ==========

    @Test
    public void GoalConstructor_SauKhoiTao_KhongThayDoiDuoc() {
        Goal goal = new Goal(p1, teamA, 45, match);
        
        // Các getter phải trả về cùng giá trị
        Player originalScorer = goal.getScorer();
        Team originalTeam = goal.getTeam();
        int originalMinute = goal.getMinute();
        Match originalMatch = goal.getMatch();
        
        assertEquals(originalScorer, goal.getScorer());
        assertEquals(originalTeam, goal.getTeam());
        assertEquals(originalMinute, goal.getMinute());
        assertEquals(originalMatch, goal.getMatch());
    }

    // ========== TEST EDGE CASES ==========

    @Test
    public void GoalConstructor_TatCaCacThamSoGioiHan_KhoiTaoThanhCong() {
        // Test với các giá trị ở giới hạn
        assertDoesNotThrow(() -> new Goal(p1, teamA, 0, match));
        assertDoesNotThrow(() -> new Goal(p2, teamB, 150, match));
        assertDoesNotThrow(() -> new Goal(p3, teamA, 1, match));
        assertDoesNotThrow(() -> new Goal(p4, teamB, 149, match));
    }

    @Test
    public void GoalConstructor_TatCaCacThamSoNgoaiGioiHan_ThrowException() {
        // Test với các giá trị ngoài giới hạn
        assertThrows(IllegalArgumentException.class, () -> new Goal(p1, teamA, -1, match));
        assertThrows(IllegalArgumentException.class, () -> new Goal(p2, teamB, 151, match));
        assertThrows(IllegalArgumentException.class, () -> new Goal(null, teamA, 45, match));
        assertThrows(IllegalArgumentException.class, () -> new Goal(p1, null, 45, match));
        assertThrows(IllegalArgumentException.class, () -> new Goal(p1, teamA, 45, null));
    }
}