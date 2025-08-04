package com.worldcup;

import com.worldcup.model.Team;
import com.worldcup.model.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class TeamTest {

    private List<Player> players22, players11, players5;
    private List<String> assistants1, assistants2, assistants3, assistants4;

    @BeforeEach
    void setUp() {
        // Tạo danh sách cầu thủ với số lượng khác nhau
        players22 = createPlayerList("Player", 22);
        players11 = createPlayerList("Starting", 11);
        players5 = createPlayerList("Substitute", 5);

        // Tạo danh sách trợ lý với số lượng khác nhau
        assistants1 = List.of("Assistant1");
        assistants2 = List.of("Assistant1", "Assistant2");
        assistants3 = List.of("Assistant1", "Assistant2", "Assistant3");
        assistants4 = List.of("Assistant1", "Assistant2", "Assistant3", "Assistant4");
    }

    private List<Player> createPlayerList(String baseName, int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            players.add(new Player(baseName + i, i, "Position" + (i % 4)));
        }
        return players;
    }

    // ========== CONSTRUCTOR 1 TESTS (with all players list) ==========

    @Test
    void TeamConstructor1_ThamSoHopLe_KhoiTaoThanhCong() {
        Team team = new Team("Brazil", "South America", "Coach", assistants2, 
                           "Medical Staff", players22, false);
        
        assertEquals("Brazil", team.getName());
        assertEquals("South America", team.getRegion());
        assertEquals("Coach", team.getCoach());
        assertEquals(2, team.getAssistantCoaches().size());
        assertEquals("Medical Staff", team.getMedicalStaff());
        assertEquals(22, team.getPlayers().size());
        assertFalse(team.isHost());
        
        // Kiểm tra thống kê ban đầu
        assertEquals(0, team.getPoints());
        assertEquals(0, team.getGoalDifference());
        assertEquals(0, team.getGoalsFor());
        assertEquals(0, team.getGoalsAgainst());
        assertEquals(0, team.getWins());
        assertEquals(0, team.getDraws());
        assertEquals(0, team.getLosses());
        assertEquals(0, team.getYellowCards());
        assertEquals(0, team.getRedCards());
        assertEquals(0, team.getSubstitutionCount());
    }

    @Test
    void TeamConstructor1_TeamChuNha_KhoiTaoThanhCong() {
        Team team = new Team("Qatar", "Asia", "Coach", assistants1, 
                           "Medical", players22, true);
        assertTrue(team.isHost());
    }

    // Test phân hoạch giá trị biên cho số lượng trợ lý
    @Test
    void TeamConstructor1_0TroLy_KhoiTaoThanhCong() {
        Team team = new Team("Team", "Region", "Coach", new ArrayList<>(), 
                           "Medical", players22, false);
        assertEquals(0, team.getAssistantCoaches().size());
    }

    @Test
    void TeamConstructor1_1TroLy_KhoiTaoThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        assertEquals(1, team.getAssistantCoaches().size());
    }

    @Test
    void TeamConstructor1_3TroLy_KhoiTaoThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants3, 
                           "Medical", players22, false);
        assertEquals(3, team.getAssistantCoaches().size());
    }

    @Test
    void TeamConstructor1_4TroLy_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Team("Team", "Region", "Coach", assistants4, 
                    "Medical", players22, false);
        });
    }

    @Test
    void TeamConstructor1_5TroLy_ThrowException() {
        List<String> assistants5 = List.of("A1", "A2", "A3", "A4", "A5");
        assertThrows(IllegalArgumentException.class, () -> {
            new Team("Team", "Region", "Coach", assistants5, 
                    "Medical", players22, false);
        });
    }

    // Test phân hoạch giá trị biên cho số lượng cầu thủ
    @Test
    void TeamConstructor1_0CauThu_KhoiTaoThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", new ArrayList<>(), false);
        assertEquals(0, team.getPlayers().size());
    }

    @Test
    void TeamConstructor1_1CauThu_KhoiTaoThanhCong() {
        List<Player> players1 = createPlayerList("Player", 1);
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players1, false);
        assertEquals(1, team.getPlayers().size());
    }

    @Test
    void TeamConstructor1_22CauThu_KhoiTaoThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        assertEquals(22, team.getPlayers().size());
    }

    @Test
    void TeamConstructor1_23CauThu_ThrowException() {
        List<Player> players23 = createPlayerList("Player", 23);
        assertThrows(IllegalArgumentException.class, () -> {
            new Team("Team", "Region", "Coach", assistants1, 
                    "Medical", players23, false);
        });
    }

    @Test
    void TeamConstructor1_50CauThu_ThrowException() {
        List<Player> players50 = createPlayerList("Player", 50);
        assertThrows(IllegalArgumentException.class, () -> {
            new Team("Team", "Region", "Coach", assistants1, 
                    "Medical", players50, false);
        });
    }

    // ========== CONSTRUCTOR 2 TESTS (with starting and substitute players) ==========

    @Test
    void TeamConstructor2_ThamSoHopLe_KhoiTaoThanhCong() {
        Team team = new Team("Argentina", "South America", "Coach", assistants2, 
                           "Medical", players11, players5, false);
        
        assertEquals("Argentina", team.getName());
        assertEquals(11, team.getStartingPlayers().size());
        assertEquals(5, team.getSubstitutePlayers().size());
        assertEquals(16, team.getPlayers().size()); // 11 + 5
    }

    // Test phân hoạch giá trị biên cho cầu thủ đá chính
    @Test
    void TeamConstructor2_10CauThuDaChinh_ThrowException() {
        List<Player> starting10 = createPlayerList("Starting", 10);
        assertThrows(IllegalArgumentException.class, () -> {
            new Team("Team", "Region", "Coach", assistants1, 
                    "Medical", starting10, players5, false);
        });
    }

    @Test
    void TeamConstructor2_11CauThuDaChinh_KhoiTaoThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players11, players5, false);
        assertEquals(11, team.getStartingPlayers().size());
    }

    @Test
    void TeamConstructor2_12CauThuDaChinh_ThrowException() {
        List<Player> starting12 = createPlayerList("Starting", 12);
        assertThrows(IllegalArgumentException.class, () -> {
            new Team("Team", "Region", "Coach", assistants1, 
                    "Medical", starting12, players5, false);
        });
    }

    // Test phân hoạch giá trị biên cho cầu thủ dự bị
    @Test
    void TeamConstructor2_4CauThuDuBi_ThrowException() {
        List<Player> substitute4 = createPlayerList("Substitute", 4);
        assertThrows(IllegalArgumentException.class, () -> {
            new Team("Team", "Region", "Coach", assistants1, 
                    "Medical", players11, substitute4, false);
        });
    }

    @Test
    void TeamConstructor2_5CauThuDuBi_KhoiTaoThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players11, players5, false);
        assertEquals(5, team.getSubstitutePlayers().size());
    }

    @Test
    void TeamConstructor2_10CauThuDuBi_KhoiTaoThanhCong() {
        List<Player> substitute10 = createPlayerList("Substitute", 10);
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players11, substitute10, false);
        assertEquals(10, team.getSubstitutePlayers().size());
    }

    // ========== PLAYER MANAGEMENT TESTS ==========

    @Test
    void AddStartingPlayer_PlayerHopLe_ThemThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        Player newPlayer = new Player("NewPlayer", 99, "Forward");
        
        team.addStartingPlayer(newPlayer);
        assertTrue(team.getStartingPlayers().contains(newPlayer));
    }

    @Test
    void AddSubstitutePlayer_PlayerHopLe_ThemThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        Player newPlayer = new Player("NewSub", 98, "Midfielder");
        
        team.addSubstitutePlayer(newPlayer);
        assertTrue(team.getSubstitutePlayers().contains(newPlayer));
    }

    @Test
    void IsContainPlayer_PlayerTrongDoi_TraVeTrue() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        assertTrue(team.isContainPlayer(players22.get(0)));
    }

    @Test
    void IsContainPlayer_PlayerKhongTrongDoi_TraVeFalse() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        Player outsidePlayer = new Player("Outside", 99, "Forward");
        assertFalse(team.isContainPlayer(outsidePlayer));
    }

    // ========== STATISTICS TESTS ==========

    @Test
    void UpdateMatchStatistics_TeamThang_CapNhatDung() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        
        team.updateMatchStatistics(3, 1); // Thắng 3-1
        
        assertEquals(3, team.getGoalsFor());
        assertEquals(1, team.getGoalsAgainst());
        assertEquals(2, team.getGoalDifference());
        assertEquals(1, team.getWins());
        assertEquals(0, team.getDraws());
        assertEquals(0, team.getLosses());
        assertEquals(3, team.getPoints());
    }

    @Test
    void UpdateMatchStatistics_TeamHoa_CapNhatDung() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        
        team.updateMatchStatistics(2, 2); // Hòa 2-2
        
        assertEquals(2, team.getGoalsFor());
        assertEquals(2, team.getGoalsAgainst());
        assertEquals(0, team.getGoalDifference());
        assertEquals(0, team.getWins());
        assertEquals(1, team.getDraws());
        assertEquals(0, team.getLosses());
        assertEquals(1, team.getPoints());
    }

    @Test
    void UpdateMatchStatistics_TeamThua_CapNhatDung() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        
        team.updateMatchStatistics(0, 2); // Thua 0-2
        
        assertEquals(0, team.getGoalsFor());
        assertEquals(2, team.getGoalsAgainst());
        assertEquals(-2, team.getGoalDifference());
        assertEquals(0, team.getWins());
        assertEquals(0, team.getDraws());
        assertEquals(1, team.getLosses());
        assertEquals(0, team.getPoints());
    }

    @Test
    void UpdateMatchStatistics_NhieuTran_TichLuyDung() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        
        team.updateMatchStatistics(2, 0); // Thắng 2-0
        team.updateMatchStatistics(1, 1); // Hòa 1-1
        team.updateMatchStatistics(0, 3); // Thua 0-3
        
        assertEquals(3, team.getGoalsFor());
        assertEquals(4, team.getGoalsAgainst());
        assertEquals(-1, team.getGoalDifference());
        assertEquals(1, team.getWins());
        assertEquals(1, team.getDraws());
        assertEquals(1, team.getLosses());
        assertEquals(4, team.getPoints()); // 3 + 1 + 0
    }

    // Test phân hoạch giá trị biên cho thống kê
    @Test
    void UpdateMatchStatistics_TySoBang0_CapNhatDung() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        
        team.updateMatchStatistics(0, 0);
        
        assertEquals(0, team.getGoalsFor());
        assertEquals(0, team.getGoalsAgainst());
        assertEquals(0, team.getGoalDifference());
        assertEquals(1, team.getDraws());
        assertEquals(1, team.getPoints());
    }

    @Test
    void UpdateMatchStatistics_TySoLon_CapNhatDung() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        
        team.updateMatchStatistics(10, 8);
        
        assertEquals(10, team.getGoalsFor());
        assertEquals(8, team.getGoalsAgainst());
        assertEquals(2, team.getGoalDifference());
        assertEquals(1, team.getWins());
        assertEquals(3, team.getPoints());
    }

    @Test
    void ResetStatistics_ResetTatCa_ThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        
        // Cập nhật một số thống kê
        team.updateMatchStatistics(3, 1);
        team.setYellowCards(5);
        team.setRedCards(2);
        team.setSubstitutionCount(3);
        
        // Reset
        team.resetStatistics();
        
        assertEquals(0, team.getPoints());
        assertEquals(0, team.getGoalDifference());
        assertEquals(0, team.getGoalsFor());
        assertEquals(0, team.getGoalsAgainst());
        assertEquals(0, team.getWins());
        assertEquals(0, team.getDraws());
        assertEquals(0, team.getLosses());
        assertEquals(0, team.getYellowCards());
        assertEquals(0, team.getRedCards());
        assertEquals(0, team.getSubstitutionCount());
    }

    // ========== SETTER/GETTER TESTS ==========

    @Test
    void SetPoints_GiaTriHopLe_SetThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        team.setPoints(9);
        assertEquals(9, team.getPoints());
    }

    @Test
    void SetGoalDifference_GiaTriAm_SetThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        team.setGoalDifference(-5);
        assertEquals(-5, team.getGoalDifference());
    }

    @Test
    void SetYellowCards_GiaTriHopLe_SetThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        team.setYellowCards(10);
        assertEquals(10, team.getYellowCards());
    }

    @Test
    void SetRedCards_GiaTriHopLe_SetThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        team.setRedCards(3);
        assertEquals(3, team.getRedCards());
    }

    // Test phân hoạch giá trị biên cho setters
    @Test
    void SetStatistics_GiaTriBang0_SetThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        
        team.setPoints(0);
        team.setGoalsFor(0);
        team.setGoalsAgainst(0);
        team.setWins(0);
        team.setDraws(0);
        team.setLosses(0);
        
        assertEquals(0, team.getPoints());
        assertEquals(0, team.getGoalsFor());
        assertEquals(0, team.getGoalsAgainst());
        assertEquals(0, team.getWins());
        assertEquals(0, team.getDraws());
        assertEquals(0, team.getLosses());
    }

    @Test
    void SetStatistics_GiaTriAm_SetThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        
        team.setPoints(-1);
        team.setGoalDifference(-10);
        
        assertEquals(-1, team.getPoints());
        assertEquals(-10, team.getGoalDifference());
    }

    @Test
    void SetStatistics_GiaTriLon_SetThanhCong() {
        Team team = new Team("Team", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        
        team.setPoints(999);
        team.setGoalsFor(100);
        team.setWins(50);
        
        assertEquals(999, team.getPoints());
        assertEquals(100, team.getGoalsFor());
        assertEquals(50, team.getWins());
    }

    // ========== TOSTRING TESTS ==========

    @Test
    void ToString_TeamDayDu_ChuoiHopLe() {
        Team team = new Team("Brazil", "South America", "Coach", assistants1, 
                           "Medical", players22, false);
        team.updateMatchStatistics(6, 2);
        team.updateMatchStatistics(3, 1);
        
        String result = team.toString();
        
        assertTrue(result.contains("Brazil"));
        assertTrue(result.contains("points=6"));
        assertTrue(result.contains("goalDiff=6"));
        assertTrue(result.contains("record=2-0-0"));
    }

    @Test
    void ToString_TeamChuaThiDau_ChuoiHopLe() {
        Team team = new Team("NewTeam", "Region", "Coach", assistants1, 
                           "Medical", players22, false);
        
        String result = team.toString();
        
        assertTrue(result.contains("NewTeam"));
        assertTrue(result.contains("points=0"));
        assertTrue(result.contains("goalDiff=0"));
        assertTrue(result.contains("record=0-0-0"));
    }

    // ========== EQUALS AND HASHCODE TESTS ==========

    @Test
    void Equals_CungTeam_TraVeTrue() {
        Team team1 = new Team("Brazil", "South America", "Coach", assistants1, 
                            "Medical", players22, false);
        Team team2 = new Team("Brazil", "South America", "Different Coach", assistants2, 
                            "Different Medical", players11, true);
        
        assertTrue(team1.equals(team2)); // Chỉ so sánh name và region
    }

    @Test
    void Equals_KhacTen_TraVeFalse() {
        Team team1 = new Team("Brazil", "South America", "Coach", assistants1, 
                            "Medical", players22, false);
        Team team2 = new Team("Argentina", "South America", "Coach", assistants1, 
                            "Medical", players22, false);
        
        assertFalse(team1.equals(team2));
    }

    @Test
    void Equals_KhacRegion_TraVeFalse() {
        Team team1 = new Team("Brazil", "South America", "Coach", assistants1, 
                            "Medical", players22, false);
        Team team2 = new Team("Brazil", "Europe", "Coach", assistants1, 
                            "Medical", players22, false);
        
        assertFalse(team1.equals(team2));
    }

    @Test
    void Equals_Null_TraVeFalse() {
        Team team = new Team("Brazil", "South America", "Coach", assistants1, 
                           "Medical", players22, false);
        assertFalse(team.equals(null));
    }

    @Test
    void Equals_KhacClass_TraVeFalse() {
        Team team = new Team("Brazil", "South America", "Coach", assistants1, 
                           "Medical", players22, false);
        assertFalse(team.equals("Not a team"));
    }

    @Test
    void HashCode_CungTeam_CungHashCode() {
        Team team1 = new Team("Brazil", "South America", "Coach1", assistants1, 
                            "Medical1", players22, false);
        Team team2 = new Team("Brazil", "South America", "Coach2", assistants2, 
                            "Medical2", players11, true);
        
        assertEquals(team1.hashCode(), team2.hashCode());
    }

    @Test
    void HashCode_KhacTeam_KhacHashCode() {
        Team team1 = new Team("Brazil", "South America", "Coach", assistants1, 
                            "Medical", players22, false);
        Team team2 = new Team("Argentina", "South America", "Coach", assistants1, 
                            "Medical", players22, false);
        
        assertNotEquals(team1.hashCode(), team2.hashCode());
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    void Team_TinhNangTichHop_HoatDongDung() {
        Team team = new Team("Integration Team", "Test Region", "Coach", assistants2, 
                           "Medical", players11, players5, false);
        
        // Kiểm tra trạng thái ban đầu
        assertEquals(16, team.getPlayers().size());
        assertEquals(11, team.getStartingPlayers().size());
        assertEquals(5, team.getSubstitutePlayers().size());
        
        // Thêm cầu thủ
        Player newPlayer = new Player("NewPlayer", 99, "Forward");
        team.addStartingPlayer(newPlayer);
        assertTrue(team.getStartingPlayers().contains(newPlayer));
        
        // Cập nhật thống kê
        team.updateMatchStatistics(2, 1);
        team.updateMatchStatistics(1, 1);
        team.updateMatchStatistics(0, 2);
        
        assertEquals(3, team.getGoalsFor());
        assertEquals(4, team.getGoalsAgainst());
        assertEquals(-1, team.getGoalDifference());
        assertEquals(1, team.getWins());
        assertEquals(1, team.getDraws());
        assertEquals(1, team.getLosses());
        assertEquals(4, team.getPoints());
        
        // Reset và kiểm tra
        team.resetStatistics();
        assertEquals(0, team.getPoints());
        assertEquals(0, team.getGoalDifference());
    }

    @Test
    void Team_BienGioiHan_XuLyDung() {
        // Test với giá trị biên tối đa
        Team team = new Team("Max Team", "Max Region", "Max Coach", assistants3, 
                           "Max Medical", players22, true);
        
        // Cập nhật thống kê với giá trị lớn
        for (int i = 0; i < 100; i++) {
            team.updateMatchStatistics(1, 0); // 100 trận thắng
        }
        
        assertEquals(100, team.getGoalsFor());
        assertEquals(0, team.getGoalsAgainst());
        assertEquals(100, team.getGoalDifference());
        assertEquals(100, team.getWins());
        assertEquals(300, team.getPoints());
        
        // Test với giá trị âm
        team.setGoalDifference(-999);
        team.setPoints(-100);
        assertEquals(-999, team.getGoalDifference());
        assertEquals(-100, team.getPoints());
    }
}