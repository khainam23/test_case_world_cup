package com.worldcup;

import com.worldcup.model.Match;
import com.worldcup.model.Player;
import com.worldcup.model.Substitution;
import com.worldcup.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubstitutionTest {

    private Player playerIn;
    private Player playerOut;
    private Player playerSame;
    private Team team;
    private Match match;
    private Team teamA;
    private Team teamB;

    @BeforeEach
    public void setUp() {
        // Tạo các đối tượng cần thiết cho test
        playerIn = new Player("Lionel Messi", 10, "Forward");
        playerOut = new Player("Angel Di Maria", 11, "Midfielder");
        playerSame = new Player("Lionel Messi", 10, "Forward"); // Cùng tên với playerIn
        
        // Tạo danh sách cầu thủ cho teamA
        List<Player> startingPlayersA = createStartingPlayers("Argentina");
        List<Player> substitutePlayersA = createSubstitutePlayers("Argentina");
        
        teamA = new Team("Argentina", "South America", "Lionel Scaloni", 
                        List.of("Assistant Coach 1"), "Medical Staff 1", 
                        startingPlayersA, substitutePlayersA, false);
        
        // Tạo danh sách cầu thủ cho teamB
        List<Player> startingPlayersB = createStartingPlayers("Brazil");
        List<Player> substitutePlayersB = createSubstitutePlayers("Brazil");
        
        teamB = new Team("Brazil", "South America", "Tite", 
                        List.of("Assistant Coach 2"), "Medical Staff 2", 
                        startingPlayersB, substitutePlayersB, false);
        
        team = teamA;
        
        match = new Match(teamA, teamB, "Stadium A", "Referee A", false);
    }
    
    private List<Player> createStartingPlayers(String teamName) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            players.add(new Player(teamName + " Player " + i, i, "Position " + i));
        }
        return players;
    }
    
    private List<Player> createSubstitutePlayers(String teamName) {
        List<Player> players = new ArrayList<>();
        for (int i = 12; i <= 16; i++) {
            players.add(new Player(teamName + " Sub " + i, i, "Position " + i));
        }
        return players;
    }

    // ========== CONSTRUCTOR TESTS - Phân hoạch tương đương ==========

    @Test
    public void SubstitutionConstructor_TatCaThamSoHopLe_KhoiTaoThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        
        assertNotNull(substitution);
        assertEquals(playerIn, substitution.getInPlayer());
        assertEquals(playerOut, substitution.getOutPlayer());
        assertEquals(60, substitution.getMinute());
        assertEquals(team, substitution.getTeam());
        assertEquals(match, substitution.getMatch());
        assertEquals(0, substitution.getId()); // ID mặc định là 0
    }

    @Test
    public void SubstitutionConstructor_PlayerInNull_ThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Substitution(null, playerOut, 60, team, match);
        });
        assertTrue(exception.getMessage().contains("Thông tin không được null"));
    }

    @Test
    public void SubstitutionConstructor_PlayerOutNull_ThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Substitution(playerIn, null, 60, team, match);
        });
        assertTrue(exception.getMessage().contains("Thông tin không được null"));
    }

    @Test
    public void SubstitutionConstructor_TeamNull_ThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Substitution(playerIn, playerOut, 60, null, match);
        });
        assertTrue(exception.getMessage().contains("Thông tin không được null"));
    }

    @Test
    public void SubstitutionConstructor_MatchNull_ThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Substitution(playerIn, playerOut, 60, team, null);
        });
        assertTrue(exception.getMessage().contains("Thông tin không được null"));
    }

    @Test
    public void SubstitutionConstructor_TatCaThamSoNull_ThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Substitution(null, null, 60, null, null);
        });
        assertTrue(exception.getMessage().contains("Thông tin không được null"));
    }

    // ========== MINUTE VALIDATION TESTS - Giá trị biên ==========

    @Test
    public void SubstitutionConstructor_PhutAm1_ThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Substitution(playerIn, playerOut, -1, team, match);
        });
        assertTrue(exception.getMessage().contains("Thời điểm thay người không hợp lệ"));
    }

    @Test
    public void SubstitutionConstructor_Phut0_KhoiTaoThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 0, team, match);
        assertEquals(0, substitution.getMinute());
    }

    @Test
    public void SubstitutionConstructor_Phut1_KhoiTaoThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 1, team, match);
        assertEquals(1, substitution.getMinute());
    }

    @Test
    public void SubstitutionConstructor_Phut45_KhoiTaoThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 45, team, match);
        assertEquals(45, substitution.getMinute());
    }

    @Test
    public void SubstitutionConstructor_Phut90_KhoiTaoThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 90, team, match);
        assertEquals(90, substitution.getMinute());
    }

    @Test
    public void SubstitutionConstructor_Phut120_KhoiTaoThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 120, team, match);
        assertEquals(120, substitution.getMinute());
    }

    @Test
    public void SubstitutionConstructor_Phut149_KhoiTaoThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 149, team, match);
        assertEquals(149, substitution.getMinute());
    }

    @Test
    public void SubstitutionConstructor_Phut150_KhoiTaoThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 150, team, match);
        assertEquals(150, substitution.getMinute());
    }

    @Test
    public void SubstitutionConstructor_Phut151_ThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Substitution(playerIn, playerOut, 151, team, match);
        });
        assertTrue(exception.getMessage().contains("Thời điểm thay người không hợp lệ"));
    }

    @Test
    public void SubstitutionConstructor_Phut200_ThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Substitution(playerIn, playerOut, 200, team, match);
        });
        assertTrue(exception.getMessage().contains("Thời điểm thay người không hợp lệ"));
    }

    @Test
    public void SubstitutionConstructor_PhutAmLon_ThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Substitution(playerIn, playerOut, -100, team, match);
        });
        assertTrue(exception.getMessage().contains("Thời điểm thay người không hợp lệ"));
    }

    // ========== PLAYER VALIDATION TESTS - Bao phủ nhánh ==========

    @Test
    public void SubstitutionConstructor_PlayerInVaPlayerOutGiongNhau_ThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Substitution(playerIn, playerIn, 60, team, match);
        });
        assertTrue(exception.getMessage().contains("Cầu thủ vào và ra phải khác nhau"));
    }

    @Test
    public void SubstitutionConstructor_PlayerInVaPlayerOutKhacNhau_KhoiTaoThanhCong() {
        assertDoesNotThrow(() -> {
            new Substitution(playerIn, playerOut, 60, team, match);
        });
    }

    @Test
    public void SubstitutionConstructor_PlayerCungTenNhungKhacDoiTuong_ThrowIllegalArgumentException() {
        // playerSame có cùng tên với playerIn nhưng là đối tượng khác
        // Tuy nhiên equals() sẽ so sánh theo nội dung, không phải reference
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Substitution(playerIn, playerSame, 60, team, match);
        });
        assertTrue(exception.getMessage().contains("Cầu thủ vào và ra phải khác nhau"));
    }

    // ========== GETTER METHODS TESTS - Bao phủ câu lệnh ==========

    @Test
    public void GetInPlayer_SubstitutionHopLe_TraVePlayerIn() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertEquals(playerIn, substitution.getInPlayer());
    }

    @Test
    public void GetOutPlayer_SubstitutionHopLe_TraVePlayerOut() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertEquals(playerOut, substitution.getOutPlayer());
    }

    @Test
    public void GetMinute_SubstitutionHopLe_TraVeMinute() {
        Substitution substitution = new Substitution(playerIn, playerOut, 75, team, match);
        assertEquals(75, substitution.getMinute());
    }

    @Test
    public void GetTeam_SubstitutionHopLe_TraVeTeam() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertEquals(team, substitution.getTeam());
    }

    @Test
    public void GetMatch_SubstitutionHopLe_TraVeMatch() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertEquals(match, substitution.getMatch());
    }

    @Test
    public void GetId_SubstitutionMoi_TraVe0() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertEquals(0, substitution.getId());
    }

    // ========== SETTER METHODS TESTS - Bao phủ câu lệnh ==========

    @Test
    public void SetId_GiaTriDuong_SetThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        substitution.setId(123);
        assertEquals(123, substitution.getId());
    }

    @Test
    public void SetId_GiaTriAm_SetThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        substitution.setId(-1);
        assertEquals(-1, substitution.getId());
    }

    @Test
    public void SetId_GiaTri0_SetThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        substitution.setId(0);
        assertEquals(0, substitution.getId());
    }

    @Test
    public void SetId_GiaTriLon_SetThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        substitution.setId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, substitution.getId());
    }

    // ========== TOSTRING METHOD TESTS - Bao phủ câu lệnh ==========

    @Test
    public void ToString_SubstitutionHopLe_ChuaThongTinDayDu() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        String result = substitution.toString();
        
        assertTrue(result.contains("Substitution{"));
        assertTrue(result.contains("playerIn=" + playerIn.getName()));
        assertTrue(result.contains("playerOut=" + playerOut.getName()));
        assertTrue(result.contains("minute=60"));
        assertTrue(result.contains("team=" + team.getName()));
        assertTrue(result.contains("match between"));
        assertTrue(result.contains(teamA.getName()));
        assertTrue(result.contains(teamB.getName()));
    }

    @Test
    public void ToString_PhutKhacNhau_ChuaPhutChinhXac() {
        Substitution substitution1 = new Substitution(playerIn, playerOut, 45, team, match);
        Substitution substitution2 = new Substitution(playerIn, playerOut, 90, team, match);
        
        assertTrue(substitution1.toString().contains("minute=45"));
        assertTrue(substitution2.toString().contains("minute=90"));
    }

    // ========== EQUALS METHOD TESTS - Bao phủ nhánh và đường ==========

    @Test
    public void Equals_CungDoiTuong_TraVeTrue() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertTrue(substitution.equals(substitution));
    }

    @Test
    public void Equals_DoiTuongNull_TraVeFalse() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertFalse(substitution.equals(null));
    }

    @Test
    public void Equals_KhacLoaiDoiTuong_TraVeFalse() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertFalse(substitution.equals("Not a Substitution"));
    }

    @Test
    public void Equals_TatCaThuocTinhGiongNhau_TraVeTrue() {
        Substitution substitution1 = new Substitution(playerIn, playerOut, 60, team, match);
        Substitution substitution2 = new Substitution(playerIn, playerOut, 60, team, match);
        
        // Set cùng ID để equals trả về true
        substitution1.setId(1);
        substitution2.setId(1);
        
        assertTrue(substitution1.equals(substitution2));
    }

    @Test
    public void Equals_KhacId_TraVeFalse() {
        Substitution substitution1 = new Substitution(playerIn, playerOut, 60, team, match);
        Substitution substitution2 = new Substitution(playerIn, playerOut, 60, team, match);
        
        substitution1.setId(1);
        substitution2.setId(2);
        
        assertFalse(substitution1.equals(substitution2));
    }

    @Test
    public void Equals_KhacMinute_TraVeFalse() {
        Substitution substitution1 = new Substitution(playerIn, playerOut, 60, team, match);
        Substitution substitution2 = new Substitution(playerIn, playerOut, 75, team, match);
        
        substitution1.setId(1);
        substitution2.setId(1);
        
        assertFalse(substitution1.equals(substitution2));
    }

    @Test
    public void Equals_KhacPlayerIn_TraVeFalse() {
        Player anotherPlayer = new Player("Sergio Aguero", 9, "Forward");
        Substitution substitution1 = new Substitution(playerIn, playerOut, 60, team, match);
        Substitution substitution2 = new Substitution(anotherPlayer, playerOut, 60, team, match);
        
        substitution1.setId(1);
        substitution2.setId(1);
        
        assertFalse(substitution1.equals(substitution2));
    }

    @Test
    public void Equals_KhacPlayerOut_TraVeFalse() {
        Player anotherPlayer = new Player("Sergio Aguero", 9, "Forward");
        Substitution substitution1 = new Substitution(playerIn, playerOut, 60, team, match);
        Substitution substitution2 = new Substitution(playerIn, anotherPlayer, 60, team, match);
        
        substitution1.setId(1);
        substitution2.setId(1);
        
        assertFalse(substitution1.equals(substitution2));
    }

    @Test
    public void Equals_KhacTeam_TraVeFalse() {
        Substitution substitution1 = new Substitution(playerIn, playerOut, 60, teamA, match);
        Substitution substitution2 = new Substitution(playerIn, playerOut, 60, teamB, match);
        
        substitution1.setId(1);
        substitution2.setId(1);
        
        assertFalse(substitution1.equals(substitution2));
    }

    @Test
    public void Equals_KhacMatch_TraVeFalse() {
        Match anotherMatch = new Match(teamA, teamB, "Stadium B", "Referee B", true);
        Substitution substitution1 = new Substitution(playerIn, playerOut, 60, team, match);
        Substitution substitution2 = new Substitution(playerIn, playerOut, 60, team, anotherMatch);
        
        substitution1.setId(1);
        substitution2.setId(1);
        
        assertFalse(substitution1.equals(substitution2));
    }

    // ========== HASHCODE METHOD TESTS - Bao phủ câu lệnh ==========

    @Test
    public void HashCode_CungDoiTuong_CungHashCode() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertEquals(substitution.hashCode(), substitution.hashCode());
    }

    @Test
    public void HashCode_DoiTuongGiongNhau_CungHashCode() {
        Substitution substitution1 = new Substitution(playerIn, playerOut, 60, team, match);
        Substitution substitution2 = new Substitution(playerIn, playerOut, 60, team, match);
        
        substitution1.setId(1);
        substitution2.setId(1);
        
        assertEquals(substitution1.hashCode(), substitution2.hashCode());
    }

    @Test
    public void HashCode_DoiTuongKhacNhau_KhacHashCode() {
        Substitution substitution1 = new Substitution(playerIn, playerOut, 60, team, match);
        Substitution substitution2 = new Substitution(playerIn, playerOut, 75, team, match);
        
        substitution1.setId(1);
        substitution2.setId(1);
        
        assertNotEquals(substitution1.hashCode(), substitution2.hashCode());
    }

    // ========== INTEGRATION TESTS - Kiểm tra tích hợp ==========

    @Test
    public void SubstitutionWorkflow_TaoVaThayDoiId_HoatDongDungCach() {
        // Tạo substitution
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertEquals(0, substitution.getId());
        
        // Thay đổi ID
        substitution.setId(100);
        assertEquals(100, substitution.getId());
        
        // Kiểm tra các thuộc tính khác không thay đổi
        assertEquals(playerIn, substitution.getInPlayer());
        assertEquals(playerOut, substitution.getOutPlayer());
        assertEquals(60, substitution.getMinute());
        assertEquals(team, substitution.getTeam());
        assertEquals(match, substitution.getMatch());
    }

    @Test
    public void SubstitutionWorkflow_NhieuSubstitutionCungMatch_HoatDongDungCach() {
        Player player3 = new Player("Paulo Dybala", 21, "Forward");
        Player player4 = new Player("Lautaro Martinez", 22, "Forward");
        
        Substitution sub1 = new Substitution(playerIn, playerOut, 60, team, match);
        Substitution sub2 = new Substitution(player3, player4, 75, team, match);
        
        sub1.setId(1);
        sub2.setId(2);
        
        assertNotEquals(sub1, sub2);
        assertEquals(match, sub1.getMatch());
        assertEquals(match, sub2.getMatch());
        assertEquals(team, sub1.getTeam());
        assertEquals(team, sub2.getTeam());
    }
}