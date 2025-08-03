package com.worldcup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    private List<Player> validPlayers;
    private List<String> validAssistants;

    @BeforeEach
    void setUp() {
        validPlayers = new ArrayList<>();
        for (int i = 1; i <= 22; i++) {
            validPlayers.add(new Player("Player" + i, i, "Midfielder", "Japan"));
        }
        validAssistants = Arrays.asList("A1", "A2", "A3");
    }

    // --- EP: Tên đội là quốc gia ---
    @Test
    void TeamConstructor_TenDoiHopLe_KhoiTaoThanhCong() {
        Team team = new Team("Germany", "Europe", "Coach G", validAssistants, "Medic G", validPlayers, false);
        assertEquals("Germany", team.getName());
    }

    // --- BVA: Số cầu thủ = 21 (biên dưới) ---
    @Test
    void TeamConstructor_SoCauThuBienDuoi21_ThrowException() {
        List<Player> players21 = validPlayers.subList(0, 21);
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Team("Brazil", "South America", "Coach B", validAssistants, "Medic B", players21, false));
        assertEquals("Đội bóng phải có ít nhất 22 cầu thủ.", ex.getMessage());
    }

    // --- BVA: Số cầu thủ = 22 (biên hợp lệ) ---
    @Test
    void TeamConstructor_SoCauThuBienHopLe22_KhoiTaoThanhCong() {
        assertDoesNotThrow(() -> new Team("France", "Europe", "Coach F", validAssistants, "Medic F", validPlayers, false));
    }

    // --- EP + BVA: Trợ lý = 4 (vượt biên) ---
    @Test
    void TeamConstructor_SoTroLyVuotBien4_ThrowException() {
        List<String> assistants = Arrays.asList("A1", "A2", "A3", "A4");
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Team("Argentina", "South America", "Coach A", assistants, "Medic A", validPlayers, false));
        assertEquals("Tối đa 3 trợ lý huấn luyện viên.", ex.getMessage());
    }

    // --- EP: Trợ lý = 3 (hợp lệ) ---
    @Test
    void TeamConstructor_SoTroLyHopLe3_KhoiTaoThanhCong() {
        assertDoesNotThrow(() ->
                new Team("Italy", "Europe", "Coach I", validAssistants, "Medic I", validPlayers, false));
    }

    // --- Path Coverage: Gán điểm, hiệu số, thẻ ---
    @Test
    void TeamSetters_GanDiemHieuSoThe_ThucHienThanhCong() {
        Team team = new Team("Japan", "Asia", "Coach J", validAssistants, "Medic J", validPlayers, false);
        team.setPoints(9);
        team.setGoalDifference(5);
        team.setYellowCards(3);
        team.setRedCards(1);

        assertAll(
                () -> assertEquals(9, team.getPoints()),
                () -> assertEquals(5, team.getGoalDifference()),
                () -> assertEquals(3, team.getYellowCards()),
                () -> assertEquals(1, team.getRedCards())
        );
    }

    // --- Branch Coverage: Là chủ nhà ---
    @Test
    void TeamConstructor_LaChuNhaTrue_TraVeTrue() {
        Team host = new Team("Qatar", "Asia", "Coach Q", validAssistants, "Medic Q", validPlayers, true);
        assertTrue(host.isHost());
    }

    @Test
    void TeamConstructor_KhongLaChuNhaFalse_TraVeFalse() {
        Team notHost = new Team("Uruguay", "South America", "Coach U", validAssistants, "Medic U", validPlayers, false);
        assertFalse(notHost.isHost());
    }

    // --- Statement Coverage: Kiểm tra getters ---
    @Test
    void TeamGetters_TatCaGetters_TraVeGiaTriDung() {
        Team team = new Team("USA", "North America", "Coach US", validAssistants, "Medic US", validPlayers, false);
        assertAll(
                () -> assertEquals("USA", team.getName()),
                () -> assertEquals("North America", team.getRegion()),
                () -> assertEquals("Coach US", team.getCoach()),
                () -> assertEquals("Medic US", team.getMedicalStaff()),
                () -> assertEquals(22, team.getPlayers().size()),
                () -> assertEquals(3, team.getAssistantCoaches().size())
        );
    }

    // --- BVA: Trợ lý = 0 ---
    @Test
    void TeamConstructor_SoTroLyBienDuoi0_KhoiTaoThanhCong() {
        List<String> noAssistants = new ArrayList<>();
        assertDoesNotThrow(() ->
                new Team("Chile", "South America", "Coach C", noAssistants, "Medic C", validPlayers, false));
    }

    // --- EP: Region thuộc nhóm hợp lệ ---
    @Test
    void TeamConstructor_RegionHopLe_KhoiTaoThanhCong() {
        String[] regions = {"Asia", "Africa", "Europe", "South America", "North America", "Oceania"};
        for (String region : regions) {
            assertDoesNotThrow(() ->
                    new Team("TestTeam", region, "Coach", validAssistants, "Medic", validPlayers, false));
        }
    }

    // --- Negative Test: Region không hợp lệ (nếu cần validate sau này) ---
    // Nếu bạn định bổ sung rule kiểm tra Region thì test này sẽ hữu ích
    /*
    @Test
    void InvalidRegionShouldFail() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Team("Test", "InvalidRegion", "Coach", validAssistants, "Medic", validPlayers, false));
        assertEquals("Khu vực không hợp lệ", ex.getMessage());
    }
    */

    // ========== ADDITIONAL 30 TEST CASES ==========

    @Test
    void TeamConstructor_CauThuChinhVaDuBi_KhoiTaoThanhCong() {
        List<Player> starting = validPlayers.subList(0, 11);
        List<Player> substitutes = validPlayers.subList(11, 22);
        
        Team team = new Team("Brazil", "South America", "Coach B", validAssistants, 
                           "Medic B", starting, substitutes, false);
        
        assertEquals("Brazil", team.getName());
        assertEquals(11, team.getStartingPlayers().size());
        assertEquals(11, team.getSubstitutePlayers().size());
    }

    @Test
    void TeamSetPoints_GiaTriAm_ChoPhepGan() {
        Team team = new Team("Germany", "Europe", "Coach G", validAssistants, "Medic G", validPlayers, false);
        team.setPoints(-3);
        assertEquals(-3, team.getPoints());
    }

    @Test
    void TeamSetPoints_GiaTriLon_ChoPhepGan() {
        Team team = new Team("Brazil", "South America", "Coach B", validAssistants, "Medic B", validPlayers, false);
        team.setPoints(999);
        assertEquals(999, team.getPoints());
    }

    @Test
    void TeamSetGoalDifference_GiaTriAm_ChoPhepGan() {
        Team team = new Team("Argentina", "South America", "Coach A", validAssistants, "Medic A", validPlayers, false);
        team.setGoalDifference(-10);
        assertEquals(-10, team.getGoalDifference());
    }

    @Test
    void TeamSetGoalDifference_GiaTriDuong_ChoPhepGan() {
        Team team = new Team("France", "Europe", "Coach F", validAssistants, "Medic F", validPlayers, false);
        team.setGoalDifference(15);
        assertEquals(15, team.getGoalDifference());
    }

    @Test
    void TeamSetYellowCards_GiaTriKhong_ChoPhepGan() {
        Team team = new Team("Spain", "Europe", "Coach S", validAssistants, "Medic S", validPlayers, false);
        team.setYellowCards(0);
        assertEquals(0, team.getYellowCards());
    }

    @Test
    void TeamSetYellowCards_GiaTriLon_ChoPhepGan() {
        Team team = new Team("Italy", "Europe", "Coach I", validAssistants, "Medic I", validPlayers, false);
        team.setYellowCards(50);
        assertEquals(50, team.getYellowCards());
    }

    @Test
    void TeamSetRedCards_GiaTriKhong_ChoPhepGan() {
        Team team = new Team("England", "Europe", "Coach E", validAssistants, "Medic E", validPlayers, false);
        team.setRedCards(0);
        assertEquals(0, team.getRedCards());
    }

    @Test
    void TeamSetRedCards_GiaTriNhieu_ChoPhepGan() {
        Team team = new Team("Portugal", "Europe", "Coach P", validAssistants, "Medic P", validPlayers, false);
        team.setRedCards(5);
        assertEquals(5, team.getRedCards());
    }

    @Test
    void TeamIncrementSubstitution_TuKhong_TangLen1() {
        Team team = new Team("Netherlands", "Europe", "Coach N", validAssistants, "Medic N", validPlayers, false);
        assertEquals(0, team.getSubstitutionCount());
        
        team.incrementSubstitutionCount();
        assertEquals(1, team.getSubstitutionCount());
    }

    @Test
    void TeamIncrementSubstitution_NhieuLan_TangDung() {
        Team team = new Team("Belgium", "Europe", "Coach B", validAssistants, "Medic B", validPlayers, false);
        
        team.incrementSubstitutionCount();
        team.incrementSubstitutionCount();
        team.incrementSubstitutionCount();
        
        assertEquals(3, team.getSubstitutionCount());
    }

    @Test
    void TeamSetSubstitutionCount_GiaTriTrucTiep_GanThanhCong() {
        Team team = new Team("Croatia", "Europe", "Coach C", validAssistants, "Medic C", validPlayers, false);
        team.setSubstitutionCount(2);
        assertEquals(2, team.getSubstitutionCount());
    }

    @Test
    void TeamAddStartingPlayer_ThemCauThuChinh_TangKichThuoc() {
        Team team = new Team("Denmark", "Europe", "Coach D", validAssistants, "Medic D", validPlayers, false);
        Player newPlayer = new Player("New Player", 99, "FW", "Denmark");
        
        int initialSize = team.getStartingPlayers().size();
        team.addStartingPlayer(newPlayer);
        
        assertEquals(initialSize + 1, team.getStartingPlayers().size());
        assertTrue(team.getStartingPlayers().contains(newPlayer));
    }

    @Test
    void TeamAddSubstitutePlayer_ThemCauThuDuBi_TangKichThuoc() {
        Team team = new Team("Sweden", "Europe", "Coach S", validAssistants, "Medic S", validPlayers, false);
        Player newPlayer = new Player("Sub Player", 88, "MF", "Sweden");
        
        int initialSize = team.getSubstitutePlayers().size();
        team.addSubstitutePlayer(newPlayer);
        
        assertEquals(initialSize + 1, team.getSubstitutePlayers().size());
        assertTrue(team.getSubstitutePlayers().contains(newPlayer));
    }

    @Test
    void testSetStartingPlayers_NewList_ShouldReplace() {
        Team team = new Team("Norway", "Europe", "Coach N", validAssistants, "Medic N", validPlayers, false);
        List<Player> newStarting = Arrays.asList(
            new Player("Player A", 1, "GK", "Norway"),
            new Player("Player B", 2, "DF", "Norway")
        );
        
        team.setStartingPlayers(newStarting);
        assertEquals(2, team.getStartingPlayers().size());
        assertEquals("Player A", team.getStartingPlayers().get(0).getName());
    }

    @Test
    void testSetSubstitutePlayers_NewList_ShouldReplace() {
        Team team = new Team("Finland", "Europe", "Coach F", validAssistants, "Medic F", validPlayers, false);
        List<Player> newSubs = Arrays.asList(
            new Player("Sub A", 12, "MF", "Finland"),
            new Player("Sub B", 13, "FW", "Finland")
        );
        
        team.setSubstitutePlayers(newSubs);
        assertEquals(2, team.getSubstitutePlayers().size());
        assertEquals("Sub A", team.getSubstitutePlayers().get(0).getName());
    }

    @Test
    void TeamConstructor_CoachNull_ChoPhepKhoiTao() {
        Team team = new Team("Iceland", "Europe", null, validAssistants, "Medic I", validPlayers, false);
        assertNull(team.getCoach());
    }

    @Test
    void TeamConstructor_MedicalStaffNull_ChoPhepKhoiTao() {
        Team team = new Team("Switzerland", "Europe", "Coach S", validAssistants, null, validPlayers, false);
        assertNull(team.getMedicalStaff());
    }

    @Test
    void TeamConstructor_TenDoiRong_ChoPhepKhoiTao() {
        Team team = new Team("", "Europe", "Coach", validAssistants, "Medic", validPlayers, false);
        assertEquals("", team.getName());
    }

    @Test
    void TeamConstructor_TenDoiKyTuDacBiet_ChoPhepKhoiTao() {
        Team team = new Team("Côte d'Ivoire", "Africa", "Coach", validAssistants, "Medic", validPlayers, false);
        assertEquals("Côte d'Ivoire", team.getName());
    }

    @Test
    void TeamConstructor_TenDoiDai_ChoPhepKhoiTao() {
        String longName = "Very Long Team Name For Testing Purposes That Exceeds Normal Length";
        Team team = new Team(longName, "Asia", "Coach", validAssistants, "Medic", validPlayers, false);
        assertEquals(longName, team.getName());
    }

    @Test
    void TeamConstructor_Dung22CauThu_KhoiTaoThanhCong() {
        List<Player> exactly22 = new ArrayList<>();
        for (int i = 1; i <= 22; i++) {
            exactly22.add(new Player("Player" + i, i, "MF", "TestTeam"));
        }
        
        Team team = new Team("TestTeam", "Asia", "Coach", validAssistants, "Medic", exactly22, false);
        assertEquals(22, team.getPlayers().size());
    }

    @Test
    void TeamConstructor_NhieuCauThu30_KhoiTaoThanhCong() {
        List<Player> manyPlayers = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            manyPlayers.add(new Player("Player" + i, i, "MF", "BigTeam"));
        }
        
        Team team = new Team("BigTeam", "Asia", "Coach", validAssistants, "Medic", manyPlayers, false);
        assertEquals(30, team.getPlayers().size());
    }

    @Test
    void testTeamWithExactly3Assistants_ShouldSucceed() {
        List<String> threeAssistants = Arrays.asList("A1", "A2", "A3");
        Team team = new Team("MaxAssistants", "Asia", "Coach", threeAssistants, "Medic", validPlayers, false);
        assertEquals(3, team.getAssistantCoaches().size());
    }

    @Test
    void testTeamWith1Assistant_ShouldSucceed() {
        List<String> oneAssistant = Arrays.asList("A1");
        Team team = new Team("OneAssistant", "Asia", "Coach", oneAssistant, "Medic", validPlayers, false);
        assertEquals(1, team.getAssistantCoaches().size());
    }

    @Test
    void testTeamWith2Assistants_ShouldSucceed() {
        List<String> twoAssistants = Arrays.asList("A1", "A2");
        Team team = new Team("TwoAssistants", "Asia", "Coach", twoAssistants, "Medic", validPlayers, false);
        assertEquals(2, team.getAssistantCoaches().size());
    }

    @Test
    void testTeamWithDuplicateAssistantNames_ShouldAllow() {
        List<String> duplicateAssistants = Arrays.asList("Assistant", "Assistant", "Assistant");
        Team team = new Team("DuplicateTeam", "Asia", "Coach", duplicateAssistants, "Medic", validPlayers, false);
        assertEquals(3, team.getAssistantCoaches().size());
    }

    @Test
    void testTeamWithNullAssistantName_ShouldAllow() {
        List<String> assistantsWithNull = Arrays.asList("A1", null, "A3");
        Team team = new Team("NullAssistant", "Asia", "Coach", assistantsWithNull, "Medic", validPlayers, false);
        assertEquals(3, team.getAssistantCoaches().size());
        assertTrue(team.getAssistantCoaches().contains(null));
    }

    @Test
    void testTeamWithEmptyAssistantName_ShouldAllow() {
        List<String> assistantsWithEmpty = Arrays.asList("A1", "", "A3");
        Team team = new Team("EmptyAssistant", "Asia", "Coach", assistantsWithEmpty, "Medic", validPlayers, false);
        assertEquals(3, team.getAssistantCoaches().size());
        assertTrue(team.getAssistantCoaches().contains(""));
    }

    @Test
    void testTeamInitialValues_ShouldBeZero() {
        Team team = new Team("InitialTest", "Asia", "Coach", validAssistants, "Medic", validPlayers, false);
        
        assertEquals(0, team.getPoints());
        assertEquals(0, team.getGoalDifference());
        assertEquals(0, team.getYellowCards());
        assertEquals(0, team.getRedCards());
        assertEquals(0, team.getSubstitutionCount());
        assertFalse(team.isHost());
    }

    @Test
    void testTeamAsHost_ShouldReturnTrue() {
        Team hostTeam = new Team("HostTeam", "Asia", "Coach", validAssistants, "Medic", validPlayers, true);
        assertTrue(hostTeam.isHost());
    }

    @Test
    void testTeamListsImmutability_ModifyingOriginalShouldNotAffectTeam() {
        List<String> originalAssistants = new ArrayList<>(Arrays.asList("A1", "A2"));
        List<Player> originalPlayers = new ArrayList<>(validPlayers);
        
        Team team = new Team("ImmutableTest", "Asia", "Coach", originalAssistants, "Medic", originalPlayers, false);
        
        // Modify original lists
        originalAssistants.add("A3");
        originalPlayers.add(new Player("Extra", 99, "FW", "ImmutableTest"));
        
        // Team should not be affected
        assertEquals(2, team.getAssistantCoaches().size());
        assertEquals(22, team.getPlayers().size());
    }

    @Test
    void testMultipleOperationsOnSameTeam_ShouldMaintainState() {
        Team team = new Team("MultiOp", "Asia", "Coach", validAssistants, "Medic", validPlayers, false);
        
        // Multiple operations
        team.setPoints(6);
        team.setGoalDifference(3);
        team.setYellowCards(2);
        team.setRedCards(1);
        team.incrementSubstitutionCount();
        team.incrementSubstitutionCount();
        
        // Verify all states
        assertEquals(6, team.getPoints());
        assertEquals(3, team.getGoalDifference());
        assertEquals(2, team.getYellowCards());
        assertEquals(1, team.getRedCards());
        assertEquals(2, team.getSubstitutionCount());
    }

    @Test
    void testTeamWithAllDifferentRegions_ShouldSucceed() {
        String[] regions = {"Asia", "Africa", "Europe", "South America", "North America", "Oceania"};
        
        for (String region : regions) {
            Team team = new Team("Team" + region, region, "Coach", validAssistants, "Medic", validPlayers, false);
            assertEquals(region, team.getRegion());
        }
    }

    @Test
    void testTeamWithCustomRegion_ShouldAllow() {
        Team team = new Team("CustomTeam", "Antarctica", "Coach", validAssistants, "Medic", validPlayers, false);
        assertEquals("Antarctica", team.getRegion());
    }

    @Test
    void testTeamWithNullRegion_ShouldAllow() {
        Team team = new Team("NullRegion", null, "Coach", validAssistants, "Medic", validPlayers, false);
        assertNull(team.getRegion());
    }

    @Test
    void testTeamWithEmptyRegion_ShouldAllow() {
        Team team = new Team("EmptyRegion", "", "Coach", validAssistants, "Medic", validPlayers, false);
        assertEquals("", team.getRegion());
    }

    @Test
    void testPlayerListsIndependence_ModifyingOneShouldNotAffectOther() {
        Team team = new Team("Independence", "Asia", "Coach", validAssistants, "Medic", validPlayers, false);
        
        Player startingPlayer = new Player("Starting", 1, "GK", "Independence");
        Player subPlayer = new Player("Sub", 12, "FW", "Independence");
        
        team.addStartingPlayer(startingPlayer);
        team.addSubstitutePlayer(subPlayer);
        
        int startingSize = team.getStartingPlayers().size();
        int subSize = team.getSubstitutePlayers().size();
        
        // Adding to one list shouldn't affect the other
        team.addStartingPlayer(new Player("Another Starting", 2, "DF", "Independence"));
        
        assertEquals(startingSize + 1, team.getStartingPlayers().size());
        assertEquals(subSize, team.getSubstitutePlayers().size());
    }

    @Test
    void testTeamStatisticsReset_ShouldAllowZeroValues() {
        Team team = new Team("ResetTest", "Asia", "Coach", validAssistants, "Medic", validPlayers, false);
        
        // Set some values
        team.setPoints(10);
        team.setGoalDifference(5);
        team.setYellowCards(3);
        team.setRedCards(2);
        team.setSubstitutionCount(3);
        
        // Reset to zero
        team.setPoints(0);
        team.setGoalDifference(0);
        team.setYellowCards(0);
        team.setRedCards(0);
        team.setSubstitutionCount(0);
        
        // Verify reset
        assertEquals(0, team.getPoints());
        assertEquals(0, team.getGoalDifference());
        assertEquals(0, team.getYellowCards());
        assertEquals(0, team.getRedCards());
        assertEquals(0, team.getSubstitutionCount());
    }
}
