package com.worldcup;

import com.worldcup.model.Goal;
import com.worldcup.model.Goal.GoalType;
import com.worldcup.model.Player;
import com.worldcup.model.Team;
import com.worldcup.model.Match;
import com.worldcup.repository.GoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

public class GoalTest {

    private Player validPlayer;
    private Team validTeam;
    private Match validMatch;
    private List<Player> startingPlayers;
    private List<Player> substitutePlayers;
    private List<String> assistants;
    private GoalRepository mockRepository;

    @BeforeEach
    void setUp() {
        // Tạo player hợp lệ
        validPlayer = new Player("Cristiano Ronaldo", 7, "Forward");
        
        // Tạo danh sách cầu thủ cho team
        startingPlayers = new ArrayList<>();
        substitutePlayers = new ArrayList<>();
        
        for (int i = 1; i <= 11; i++) {
            startingPlayers.add(new Player("Starting" + i, i, "Position" + (i % 4)));
        }
        
        for (int i = 12; i <= 16; i++) {
            substitutePlayers.add(new Player("Sub" + i, i, "Position" + (i % 4)));
        }
        
        // Tạo danh sách trợ lý
        assistants = List.of("Assistant1", "Assistant2");
        
        // Tạo team hợp lệ
        validTeam = new Team("Portugal", "Europe", "Coach", assistants, 
                           "Medical Staff", startingPlayers, substitutePlayers, false);
        
        // Tạo match hợp lệ
        Team teamB = new Team("Spain", "Europe", "Coach B", assistants, 
                            "Medical B", startingPlayers, substitutePlayers, false);
        validMatch = new Match(validTeam, teamB, "2024/12/15", "Stadium", false);
        
        // Tạo mock repository
        mockRepository = mock(GoalRepository.class);
        Goal.setGoalRepository(mockRepository);
    }

    @AfterEach
    void tearDown() {
        // Reset repository sau mỗi test
        Goal.setGoalRepository(null);
    }

    // ========== CONSTRUCTOR TESTS ==========

    @Test
    void GoalConstructor_ThamSoHopLe_KhoiTaoThanhCong() {
        int initialGoals = validPlayer.getGoals();
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        
        assertEquals(validPlayer, goal.getPlayer());
        assertEquals(validTeam, goal.getTeam());
        assertEquals(validMatch, goal.getMatch());
        assertEquals(45, goal.getMinute());
        assertEquals(0, goal.getId()); // ID mặc định là 0
        assertEquals(initialGoals + 1, validPlayer.getGoals()); // Kiểm tra cập nhật số bàn thắng
    }

    @Test
    void GoalConstructor_BanThangBinhThuong_KhoiTaoThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 30, validMatch);
        assertEquals(30, goal.getMinute());
    }

    @Test
    void GoalConstructor_BanThangPhutCuoi_KhoiTaoThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 90, validMatch);
        assertEquals(90, goal.getMinute());
    }

    // Test phân hoạch giá trị biên cho minute
    @Test
    void GoalConstructor_Phut0_KhoiTaoThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 0, validMatch);
        assertEquals(0, goal.getMinute());
    }

    @Test
    void GoalConstructor_Phut1_KhoiTaoThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 1, validMatch);
        assertEquals(1, goal.getMinute());
    }

    @Test
    void GoalConstructor_Phut45_KhoiTaoThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        assertEquals(45, goal.getMinute());
    }

    @Test
    void GoalConstructor_Phut90_KhoiTaoThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 90, validMatch);
        assertEquals(90, goal.getMinute());
    }

    @Test
    void GoalConstructor_Phut120_KhoiTaoThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 120, validMatch);
        assertEquals(120, goal.getMinute());
    }

    @Test
    void GoalConstructor_Phut149_KhoiTaoThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 149, validMatch);
        assertEquals(149, goal.getMinute());
    }

    @Test
    void GoalConstructor_Phut150_KhoiTaoThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 150, validMatch);
        assertEquals(150, goal.getMinute());
    }

    @Test
    void GoalConstructor_PhutAm1_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Goal(validPlayer, validTeam, -1, validMatch);
        });
    }

    @Test
    void GoalConstructor_PhutAm10_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Goal(validPlayer, validTeam, -10, validMatch);
        });
    }

    @Test
    void GoalConstructor_Phut151_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Goal(validPlayer, validTeam, 151, validMatch);
        });
    }

    @Test
    void GoalConstructor_Phut200_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Goal(validPlayer, validTeam, 200, validMatch);
        });
    }

    // Test null parameters
    @Test
    void GoalConstructor_PlayerNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Goal(null, validTeam, 45, validMatch);
        });
    }

    @Test
    void GoalConstructor_TeamNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Goal(validPlayer, null, 45, validMatch);
        });
    }

    @Test
    void GoalConstructor_MatchNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Goal(validPlayer, validTeam, 45, null);
        });
    }

    @Test
    void GoalConstructor_TatCaThamSoNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Goal(null, null, 45, null);
        });
    }

    // ========== GETTER TESTS ==========

    @Test
    void GetPlayer_GoalHopLe_TraVePlayer() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        assertEquals(validPlayer, goal.getPlayer());
        assertEquals("Cristiano Ronaldo", goal.getPlayer().getName());
    }

    @Test
    void SetPlayer_GoalHopLe_TraVePlayer() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        assertEquals(validPlayer, goal.setPlayer()); // Test method setPlayer (có vẻ như bug trong code gốc)
    }

    @Test
    void GetTeam_GoalHopLe_TraVeTeam() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        assertEquals(validTeam, goal.getTeam());
        assertEquals("Portugal", goal.getTeam().getName());
    }

    @Test
    void GetMatch_GoalHopLe_TraVeMatch() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        assertEquals(validMatch, goal.getMatch());
    }

    @Test
    void GetMinute_GoalHopLe_TraVeMinute() {
        Goal goal = new Goal(validPlayer, validTeam, 75, validMatch);
        assertEquals(75, goal.getMinute());
    }

    @Test
    void GetId_MacDinh_TraVe0() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        assertEquals(0, goal.getId());
    }

    @Test
    void GetType_MacDinh_TraVeNull() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        assertNull(goal.getType());
    }

    // ========== SETTER TESTS ==========

    @Test
    void SetId_GiaTriDuong_SetThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setId(100);
        assertEquals(100, goal.getId());
    }

    @Test
    void SetId_GiaTriAm_SetThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setId(-1);
        assertEquals(-1, goal.getId());
    }

    @Test
    void SetId_GiaTri0_SetThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setId(0);
        assertEquals(0, goal.getId());
    }

    @Test
    void SetType_Normal_SetThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setType(GoalType.NORMAL);
        assertEquals(GoalType.NORMAL, goal.getType());
    }

    @Test
    void SetType_Penalty_SetThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setType(GoalType.PENALTY);
        assertEquals(GoalType.PENALTY, goal.getType());
    }

    @Test
    void SetType_OwnGoal_SetThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setType(GoalType.OWN_GOAL);
        assertEquals(GoalType.OWN_GOAL, goal.getType());
    }

    @Test
    void SetType_Header_SetThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setType(GoalType.HEADER);
        assertEquals(GoalType.HEADER, goal.getType());
    }

    @Test
    void SetType_FreeKick_SetThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setType(GoalType.FREE_KICK);
        assertEquals(GoalType.FREE_KICK, goal.getType());
    }

    @Test
    void SetType_LongShot_SetThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setType(GoalType.LONG_SHOT);
        assertEquals(GoalType.LONG_SHOT, goal.getType());
    }

    @Test
    void SetType_Null_SetThanhCong() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setType(null);
        assertNull(goal.getType());
    }

    // ========== GOALTYPE ENUM TESTS ==========

    @Test
    void GoalType_Normal_CoLabelDung() {
        assertEquals("Normal", GoalType.NORMAL.getLabel());
    }

    @Test
    void GoalType_Penalty_CoLabelDung() {
        assertEquals("Penalty", GoalType.PENALTY.getLabel());
    }

    @Test
    void GoalType_OwnGoal_CoLabelDung() {
        assertEquals("Own Goal", GoalType.OWN_GOAL.getLabel());
    }

    @Test
    void GoalType_Header_CoLabelDung() {
        assertEquals("Header", GoalType.HEADER.getLabel());
    }

    @Test
    void GoalType_FreeKick_CoLabelDung() {
        assertEquals("Free Kick", GoalType.FREE_KICK.getLabel());
    }

    @Test
    void GoalType_LongShot_CoLabelDung() {
        assertEquals("Long Shot", GoalType.LONG_SHOT.getLabel());
    }

    @Test
    void GoalTypeFromLabel_Normal_TraVeNormal() {
        assertEquals(GoalType.NORMAL, GoalType.fromLabel("Normal"));
    }

    @Test
    void GoalTypeFromLabel_Penalty_TraVePenalty() {
        assertEquals(GoalType.PENALTY, GoalType.fromLabel("Penalty"));
    }

    @Test
    void GoalTypeFromLabel_OwnGoal_TraVeOwnGoal() {
        assertEquals(GoalType.OWN_GOAL, GoalType.fromLabel("Own Goal"));
    }

    @Test
    void GoalTypeFromLabel_Header_TraVeHeader() {
        assertEquals(GoalType.HEADER, GoalType.fromLabel("Header"));
    }

    @Test
    void GoalTypeFromLabel_FreeKick_TraVeFreeKick() {
        assertEquals(GoalType.FREE_KICK, GoalType.fromLabel("Free Kick"));
    }

    @Test
    void GoalTypeFromLabel_LongShot_TraVeLongShot() {
        assertEquals(GoalType.LONG_SHOT, GoalType.fromLabel("Long Shot"));
    }

    @Test
    void GoalTypeFromLabel_NormalKhongPhanBietHoaThuong_TraVeNormal() {
        assertEquals(GoalType.NORMAL, GoalType.fromLabel("normal"));
        assertEquals(GoalType.NORMAL, GoalType.fromLabel("NORMAL"));
        assertEquals(GoalType.NORMAL, GoalType.fromLabel("NoRmAl"));
    }

    @Test
    void GoalTypeFromLabel_PenaltyKhongPhanBietHoaThuong_TraVePenalty() {
        assertEquals(GoalType.PENALTY, GoalType.fromLabel("penalty"));
        assertEquals(GoalType.PENALTY, GoalType.fromLabel("PENALTY"));
        assertEquals(GoalType.PENALTY, GoalType.fromLabel("PeNaLtY"));
    }

    @Test
    void GoalTypeFromLabel_LabelKhongHopLe_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            GoalType.fromLabel("Invalid");
        });
    }

    @Test
    void GoalTypeFromLabel_LabelRong_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            GoalType.fromLabel("");
        });
    }

    @Test
    void GoalTypeFromLabel_LabelNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            GoalType.fromLabel(null);
        });
    }

    @Test
    void GoalTypeFromLabel_LabelKhoangTrang_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            GoalType.fromLabel("   ");
        });
    }

    // ========== REPOSITORY TESTS ==========

    @Test
    void Save_CoRepository_GoiSaveMethod() throws Exception {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.save();
        
        verify(mockRepository, times(1)).save(goal);
    }

    @Test
    void Save_KhongCoRepository_KhongGoiSaveMethod() throws Exception {
        Goal.setGoalRepository(null);
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.save();
        
        // Không có exception được throw, method chạy thành công
        assertNotNull(goal);
    }

    @Test
    void Update_CoRepositoryVaCoId_GoiUpdateMethod() throws Exception {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setId(1);
        goal.update();
        
        verify(mockRepository, times(1)).update(goal);
    }

    @Test
    void Update_CoRepositoryNhungKhongCoId_KhongGoiUpdateMethod() throws Exception {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setId(0); // ID = 0 không hợp lệ
        goal.update();
        
        verify(mockRepository, never()).update(goal);
    }

    @Test
    void Update_KhongCoRepository_KhongGoiUpdateMethod() throws Exception {
        Goal.setGoalRepository(null);
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        goal.setId(1);
        goal.update();
        
        // Không có exception được throw, method chạy thành công
        assertNotNull(goal);
    }

    // ========== TOSTRING TESTS ==========

    @Test
    void ToString_GoalBinhThuong_ChuoiDungDinhDang() {
        Goal goal = new Goal(validPlayer, validTeam, 45, validMatch);
        String result = goal.toString();
        
        assertTrue(result.contains("Goal{"));
        assertTrue(result.contains("scorer=Cristiano Ronaldo"));
        assertTrue(result.contains("team=Portugal"));
        assertTrue(result.contains("minute=45"));
        assertTrue(result.contains("match="));
    }

    @Test
    void ToString_GoalPhut0_ChuoiDungDinhDang() {
        Goal goal = new Goal(validPlayer, validTeam, 0, validMatch);
        String result = goal.toString();
        
        assertTrue(result.contains("minute=0"));
    }

    @Test
    void ToString_GoalPhut150_ChuoiDungDinhDang() {
        Goal goal = new Goal(validPlayer, validTeam, 150, validMatch);
        String result = goal.toString();
        
        assertTrue(result.contains("minute=150"));
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    void Goal_ToanBoChucNang_HoatDongDung() throws Exception {
        int initialGoals = validPlayer.getGoals();
        
        // Tạo goal
        Goal goal = new Goal(validPlayer, validTeam, 75, validMatch);
        
        // Kiểm tra constructor
        assertEquals(validPlayer, goal.getPlayer());
        assertEquals(validTeam, goal.getTeam());
        assertEquals(validMatch, goal.getMatch());
        assertEquals(75, goal.getMinute());
        assertEquals(0, goal.getId());
        assertEquals(initialGoals + 1, validPlayer.getGoals());
        
        // Kiểm tra setters
        goal.setId(999);
        goal.setType(GoalType.PENALTY);
        assertEquals(999, goal.getId());
        assertEquals(GoalType.PENALTY, goal.getType());
        
        // Kiểm tra repository operations
        goal.save();
        goal.update();
        verify(mockRepository, times(1)).save(goal);
        verify(mockRepository, times(1)).update(goal);
        
        // Kiểm tra toString
        String result = goal.toString();
        assertTrue(result.contains("Cristiano Ronaldo"));
        assertTrue(result.contains("Portugal"));
        assertTrue(result.contains("75"));
    }

    @Test
    void Goal_NhieuGoalCungPlayer_TaoThanhCong() {
        int initialGoals = validPlayer.getGoals();
        
        Goal goal1 = new Goal(validPlayer, validTeam, 30, validMatch);
        Goal goal2 = new Goal(validPlayer, validTeam, 60, validMatch);
        
        assertEquals(validPlayer, goal1.getPlayer());
        assertEquals(validPlayer, goal2.getPlayer());
        assertEquals(30, goal1.getMinute());
        assertEquals(60, goal2.getMinute());
        assertEquals(initialGoals + 2, validPlayer.getGoals()); // Cập nhật 2 bàn thắng
    }

    // ========== BOUNDARY VALUE ANALYSIS TESTS ==========

    @Test
    void GoalConstructor_GiaTriBienDuoiCuaMinute_KhoiTaoThanhCong() {
        // Giá trị biên dưới: 0
        Goal goal = new Goal(validPlayer, validTeam, 0, validMatch);
        assertEquals(0, goal.getMinute());
    }

    @Test
    void GoalConstructor_GiaTriBienTrenCuaMinute_KhoiTaoThanhCong() {
        // Giá trị biên trên: 150
        Goal goal = new Goal(validPlayer, validTeam, 150, validMatch);
        assertEquals(150, goal.getMinute());
    }

    @Test
    void GoalConstructor_GiaTriNgoaiBienDuoi_ThrowException() {
        // Giá trị ngoài biên dưới: -1
        assertThrows(IllegalArgumentException.class, () -> {
            new Goal(validPlayer, validTeam, -1, validMatch);
        });
    }

    @Test
    void GoalConstructor_GiaTriNgoaiBienTren_ThrowException() {
        // Giá trị ngoài biên trên: 151
        assertThrows(IllegalArgumentException.class, () -> {
            new Goal(validPlayer, validTeam, 151, validMatch);
        });
    }

    // ========== EQUIVALENCE PARTITIONING TESTS ==========

    @Test
    void GoalConstructor_PhutHopLe0Den150_KhoiTaoThanhCong() {
        // Phân hoạch tương đương: giá trị hợp lệ [0, 150]
        int[] validMinutes = {0, 1, 45, 90, 120, 149, 150};
        
        for (int minute : validMinutes) {
            Goal goal = new Goal(validPlayer, validTeam, minute, validMatch);
            assertEquals(minute, goal.getMinute());
        }
    }

    @Test
    void GoalConstructor_PhutKhongHopLeNhoHon0_ThrowException() {
        // Phân hoạch tương đương: giá trị không hợp lệ < 0
        int[] invalidMinutes = {-1, -5, -10, -100};
        
        for (int minute : invalidMinutes) {
            assertThrows(IllegalArgumentException.class, () -> {
                new Goal(validPlayer, validTeam, minute, validMatch);
            });
        }
    }

    @Test
    void GoalConstructor_PhutKhongHopLeLonHon150_ThrowException() {
        // Phân hoạch tương đương: giá trị không hợp lệ > 150
        int[] invalidMinutes = {151, 155, 200, 1000};
        
        for (int minute : invalidMinutes) {
            assertThrows(IllegalArgumentException.class, () -> {
                new Goal(validPlayer, validTeam, minute, validMatch);
            });
        }
    }
}