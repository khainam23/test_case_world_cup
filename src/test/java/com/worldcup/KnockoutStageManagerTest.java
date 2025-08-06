package com.worldcup;

import com.worldcup.model.Group;
import com.worldcup.model.KnockoutStageManager;
import com.worldcup.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnockoutStageManagerTest {

    private KnockoutStageManager manager;
    private Map<Group, Team> groupWinners;
    private Map<Group, Team> groupRunnersUp;
    private Group groupA, groupB, groupC, groupD, groupE, groupF, groupG, groupH;
    private Team teamA1, teamA2, teamB1, teamB2, teamC1, teamC2, teamD1, teamD2;
    private Team teamE1, teamE2, teamF1, teamF2, teamG1, teamG2, teamH1, teamH2;

    @BeforeEach
    public void setUp() {
        manager = new KnockoutStageManager();
        
        // Tạo các group
        groupA = new Group("Group A");
        groupB = new Group("Group B");
        groupC = new Group("Group C");
        groupD = new Group("Group D");
        groupE = new Group("Group E");
        groupF = new Group("Group F");
        groupG = new Group("Group G");
        groupH = new Group("Group H");
        
        // Tạo các team
        teamA1 = new Team("Brazil", "South America", "Coach A1", 
                         List.of("Assistant A1"), "Medical A1", false);
        teamA2 = new Team("Colombia", "South America", "Coach A2", 
                         List.of("Assistant A2"), "Medical A2", false);
        teamB1 = new Team("Argentina", "South America", "Coach B1", 
                         List.of("Assistant B1"), "Medical B1", false);
        teamB2 = new Team("Uruguay", "South America", "Coach B2", 
                         List.of("Assistant B2"), "Medical B2", false);
        teamC1 = new Team("Germany", "Europe", "Coach C1", 
                         List.of("Assistant C1"), "Medical C1", false);
        teamC2 = new Team("Spain", "Europe", "Coach C2", 
                         List.of("Assistant C2"), "Medical C2", false);
        teamD1 = new Team("France", "Europe", "Coach D1", 
                         List.of("Assistant D1"), "Medical D1", false);
        teamD2 = new Team("Italy", "Europe", "Coach D2", 
                         List.of("Assistant D2"), "Medical D2", false);
        teamE1 = new Team("England", "Europe", "Coach E1", 
                         List.of("Assistant E1"), "Medical E1", false);
        teamE2 = new Team("Netherlands", "Europe", "Coach E2", 
                         List.of("Assistant E2"), "Medical E2", false);
        teamF1 = new Team("Portugal", "Europe", "Coach F1", 
                         List.of("Assistant F1"), "Medical F1", false);
        teamF2 = new Team("Belgium", "Europe", "Coach F2", 
                         List.of("Assistant F2"), "Medical F2", false);
        teamG1 = new Team("Croatia", "Europe", "Coach G1", 
                         List.of("Assistant G1"), "Medical G1", false);
        teamG2 = new Team("Denmark", "Europe", "Coach G2", 
                         List.of("Assistant G2"), "Medical G2", false);
        teamH1 = new Team("Japan", "Asia", "Coach H1", 
                         List.of("Assistant H1"), "Medical H1", false);
        teamH2 = new Team("South Korea", "Asia", "Coach H2", 
                         List.of("Assistant H2"), "Medical H2", false);
        
        // Tạo map winners và runners-up
        groupWinners = new HashMap<>();
        groupRunnersUp = new HashMap<>();
        
        groupWinners.put(groupA, teamA1);
        groupWinners.put(groupB, teamB1);
        groupWinners.put(groupC, teamC1);
        groupWinners.put(groupD, teamD1);
        groupWinners.put(groupE, teamE1);
        groupWinners.put(groupF, teamF1);
        groupWinners.put(groupG, teamG1);
        groupWinners.put(groupH, teamH1);
        
        groupRunnersUp.put(groupA, teamA2);
        groupRunnersUp.put(groupB, teamB2);
        groupRunnersUp.put(groupC, teamC2);
        groupRunnersUp.put(groupD, teamD2);
        groupRunnersUp.put(groupE, teamE2);
        groupRunnersUp.put(groupF, teamF2);
        groupRunnersUp.put(groupG, teamG2);
        groupRunnersUp.put(groupH, teamH2);
    }


    // ========== CONSTRUCTOR TESTS - Phân hoạch tương đương ==========

    @Test
    public void KnockoutStageManagerConstructor_KhoiTaoMacDinh_TatCaListRong() {
        KnockoutStageManager newManager = new KnockoutStageManager();

        assertNotNull(newManager.getBracketInfo());
        assertNotNull(newManager.getRoundOf16());
        assertNotNull(newManager.getQuarterFinals());
        assertNotNull(newManager.getSemiFinals());
        assertNotNull(newManager.getFinals());
        assertNotNull(newManager.getBronzeWinners());

        assertTrue(newManager.getBracketInfo().isEmpty());
        assertTrue(newManager.getRoundOf16().isEmpty());
        assertTrue(newManager.getQuarterFinals().isEmpty());
        assertTrue(newManager.getSemiFinals().isEmpty());
        assertTrue(newManager.getFinals().isEmpty());
        assertTrue(newManager.getBronzeWinners().isEmpty());

        assertNull(newManager.getChampion());
        assertNull(newManager.getRunnerUp());
    }

    // ========== GENERATE ROUND OF 16 BRACKET TESTS - Bao phủ nhánh và đường ==========

    @Test
    public void GenerateRoundOf16Bracket_DayDu8Nhom_TaoBracketThanhCong() {
        manager.generateRoundOf16Bracket(groupWinners, groupRunnersUp);
        Map<String, String> bracket = manager.getBracketInfo();

        assertEquals(8, bracket.size());
        assertTrue(bracket.containsKey("Match 1"));
        assertTrue(bracket.containsKey("Match 2"));
        assertTrue(bracket.containsKey("Match 3"));
        assertTrue(bracket.containsKey("Match 4"));
        assertTrue(bracket.containsKey("Match 5"));
        assertTrue(bracket.containsKey("Match 6"));
        assertTrue(bracket.containsKey("Match 7"));
        assertTrue(bracket.containsKey("Match 8"));

        // Kiểm tra format của bracket
        assertTrue(bracket.get("Match 1").contains(" vs "));
        assertTrue(bracket.get("Match 2").contains(" vs "));
    }

    @Test
    public void GenerateRoundOf16Bracket_WinnersNull_ThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            manager.generateRoundOf16Bracket(null, groupRunnersUp);
        });
    }

    @Test
    public void GenerateRoundOf16Bracket_RunnersUpNull_ThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            manager.generateRoundOf16Bracket(groupWinners, null);
        });
    }

    @Test
    public void GenerateRoundOf16Bracket_CaHaiMapNull_ThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            manager.generateRoundOf16Bracket(null, null);
        });
    }

    @Test
    public void GenerateRoundOf16Bracket_MapRong_TaoBracketVoiNull() {
        Map<Group, Team> emptyWinners = new HashMap<>();
        Map<Group, Team> emptyRunners = new HashMap<>();

        manager.generateRoundOf16Bracket(emptyWinners, emptyRunners);
        Map<String, String> bracket = manager.getBracketInfo();

        assertEquals(8, bracket.size());
        assertTrue(bracket.get("Match 1").contains("null"));
        assertTrue(bracket.get("Match 2").contains("null"));
    }

    @Test
    public void GenerateRoundOf16Bracket_ThieuMotSoNhom_TaoBracketVoiNull() {
        Map<Group, Team> partialWinners = new HashMap<>();
        Map<Group, Team> partialRunners = new HashMap<>();

        partialWinners.put(groupA, teamA1);
        partialRunners.put(groupB, teamB2);
        // Missing other groups

        manager.generateRoundOf16Bracket(partialWinners, partialRunners);
        Map<String, String> bracket = manager.getBracketInfo();

        assertEquals(8, bracket.size());
        assertTrue(bracket.get("Match 1").contains("Brazil"));
        assertFalse(bracket.get("Match 1").contains("Uruguay"));
        assertTrue(bracket.get("Match 3").contains("null"));
    }

    @Test
    public void GenerateRoundOf16Bracket_TeamNull_XuLyNull() {
        Map<Group, Team> winnersWithNull = new HashMap<>();
        Map<Group, Team> runnersWithNull = new HashMap<>();

        winnersWithNull.put(groupA, null);
        runnersWithNull.put(groupB, null);

        manager.generateRoundOf16Bracket(winnersWithNull, runnersWithNull);
        Map<String, String> bracket = manager.getBracketInfo();

        assertTrue(bracket.get("Match 1").contains("null"));
        assertTrue(bracket.get("Match 2").contains("null"));
    }

    @Test
    public void GenerateRoundOf16Bracket_GoiNhieuLan_GhiDeLanCuoi() {
        // Lần đầu
        manager.generateRoundOf16Bracket(groupWinners, groupRunnersUp);
        String firstMatch1 = manager.getBracketInfo().get("Match 1");

        // Tạo map mới
        Map<Group, Team> newWinners = new HashMap<>();
        Map<Group, Team> newRunners = new HashMap<>();
        newWinners.put(groupA, teamA2); // Đổi team
        newRunners.put(groupB, teamB1); // Đổi team

        // Lần thứ hai
        manager.generateRoundOf16Bracket(newWinners, newRunners);
        String secondMatch1 = manager.getBracketInfo().get("Match 1");

        assertNotEquals(firstMatch1, secondMatch1);
        assertTrue(secondMatch1.contains("Colombia"));
    }

    @Test
    public void GenerateRoundOf16Bracket_TenTeamCoKyTuDacBiet_XuLyThanhCong() {
        Team specialTeam1 = new Team("Team@#$%", "Region", "Coach",
                List.of("Assistant"), "Medical", false);
        Team specialTeam2 = new Team("Team with spaces", "Region", "Coach",
                List.of("Assistant"), "Medical", false);

        Map<Group, Team> specialWinners = new HashMap<>();
        Map<Group, Team> specialRunners = new HashMap<>();

        specialWinners.put(groupA, specialTeam1);
        specialRunners.put(groupB, specialTeam2);

        manager.generateRoundOf16Bracket(specialWinners, specialRunners);
        Map<String, String> bracket = manager.getBracketInfo();

        assertTrue(bracket.get("Match 1").contains("Team@#$%"));
        assertFalse(bracket.get("Match 1").contains("Team with spaces"));
    }

    // ========== SET ROUND OF 16 WINNERS TESTS - Giá trị biên ==========

    @Test
    public void SetRoundOf16Winners_Dung8Doi_KhoiTaoThanhCong() {
        List<String> winners = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8");

        assertDoesNotThrow(() -> manager.setRoundOf16Winners(winners));
        assertEquals(8, manager.getQuarterFinals().size());
        assertEquals("T1", manager.getQuarterFinals().get(0));
        assertEquals("T8", manager.getQuarterFinals().get(7));
    }

    @Test
    public void SetRoundOf16Winners_0Doi_ThrowIllegalArgumentException() {
        List<String> winners = new ArrayList<>();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            manager.setRoundOf16Winners(winners);
        });
        assertTrue(exception.getMessage().contains("8"));
    }

    @Test
    public void SetRoundOf16Winners_1Doi_ThrowIllegalArgumentException() {
        List<String> winners = Arrays.asList("T1");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.setRoundOf16Winners(winners);
        });
    }

    @Test
    public void SetRoundOf16Winners_7Doi_ThrowIllegalArgumentException() {
        List<String> winners = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.setRoundOf16Winners(winners);
        });
    }

    @Test
    public void SetRoundOf16Winners_9Doi_ThrowIllegalArgumentException() {
        List<String> winners = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.setRoundOf16Winners(winners);
        });
    }

    @Test
    public void SetRoundOf16Winners_100Doi_ThrowIllegalArgumentException() {
        List<String> winners = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            winners.add("Team" + i);
        }

        assertThrows(IllegalArgumentException.class, () -> {
            manager.setRoundOf16Winners(winners);
        });
    }

    @Test
    public void SetRoundOf16Winners_ListNull_ThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            manager.setRoundOf16Winners(null);
        });
    }

    @Test
    public void SetRoundOf16Winners_CoDoiNull_ChoPhepNull() {
        List<String> winners = Arrays.asList("T1", null, "T3", "T4", "T5", "T6", "T7", "T8");

        assertDoesNotThrow(() -> manager.setRoundOf16Winners(winners));
        assertTrue(manager.getQuarterFinals().contains(null));
        assertEquals(8, manager.getQuarterFinals().size());
    }

    @Test
    public void SetRoundOf16Winners_TatCaDoiNull_ChoPhepTatCaNull() {
        List<String> winners = Arrays.asList(null, null, null, null, null, null, null, null);

        assertDoesNotThrow(() -> manager.setRoundOf16Winners(winners));
        assertEquals(8, Collections.frequency(manager.getQuarterFinals(), null));
    }

    @Test
    public void SetRoundOf16Winners_DoiTrung_ChoPhepTrung() {
        List<String> winners = Arrays.asList("Brazil", "Brazil", "Brazil", "T4", "T5", "T6", "T7", "T8");

        assertDoesNotThrow(() -> manager.setRoundOf16Winners(winners));
        assertEquals(3, Collections.frequency(manager.getQuarterFinals(), "Brazil"));
    }

    @Test
    public void SetRoundOf16Winners_TenDoiDai_XuLyThanhCong() {
        String longName = "A".repeat(1000);
        List<String> winners = Arrays.asList(longName, "T2", "T3", "T4", "T5", "T6", "T7", "T8");

        assertDoesNotThrow(() -> manager.setRoundOf16Winners(winners));
        assertTrue(manager.getQuarterFinals().contains(longName));
    }

    @Test
    public void SetRoundOf16Winners_TenDoiRong_ChoPhepTenRong() {
        List<String> winners = Arrays.asList("", "T2", "T3", "T4", "T5", "T6", "T7", "T8");

        assertDoesNotThrow(() -> manager.setRoundOf16Winners(winners));
        assertTrue(manager.getQuarterFinals().contains(""));
    }

    @Test
    public void SetRoundOf16Winners_GoiNhieuLan_GhiDeLanCuoi() {
        List<String> winners1 = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8");
        List<String> winners2 = Arrays.asList("X1", "X2", "X3", "X4", "X5", "X6", "X7", "X8");

        manager.setRoundOf16Winners(winners1);
        assertEquals("T1", manager.getQuarterFinals().get(0));

        manager.setRoundOf16Winners(winners2);
        assertEquals("X1", manager.getQuarterFinals().get(0));
        assertFalse(manager.getQuarterFinals().contains("T1"));
    }

    // ========== SET QUARTER FINAL WINNERS TESTS - Giá trị biên ==========

    @Test
    public void SetQuarterFinalWinners_Dung4Doi_CapNhatThanhCong() {
        List<String> winners = Arrays.asList("W1", "W2", "W3", "W4");

        assertDoesNotThrow(() -> manager.setQuarterFinalWinners(winners));
        assertEquals(4, manager.getSemiFinals().size());
        assertEquals(winners, manager.getSemiFinals());
    }

    @Test
    public void SetQuarterFinalWinners_0Doi_ThrowIllegalArgumentException() {
        List<String> winners = new ArrayList<>();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            manager.setQuarterFinalWinners(winners);
        });
        assertTrue(exception.getMessage().contains("4"));
    }

    @Test
    public void SetQuarterFinalWinners_1Doi_ThrowIllegalArgumentException() {
        List<String> winners = Arrays.asList("W1");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.setQuarterFinalWinners(winners);
        });
    }

    @Test
    public void SetQuarterFinalWinners_3Doi_ThrowIllegalArgumentException() {
        List<String> winners = Arrays.asList("W1", "W2", "W3");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.setQuarterFinalWinners(winners);
        });
    }

    @Test
    public void SetQuarterFinalWinners_5Doi_ThrowIllegalArgumentException() {
        List<String> winners = Arrays.asList("W1", "W2", "W3", "W4", "W5");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.setQuarterFinalWinners(winners);
        });
    }

    @Test
    public void SetQuarterFinalWinners_ListNull_ThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            manager.setQuarterFinalWinners(null);
        });
    }

    @Test
    public void SetQuarterFinalWinners_CoDoiNull_ChoPhepNull() {
        List<String> winners = Arrays.asList("W1", null, "W3", "W4");

        assertDoesNotThrow(() -> manager.setQuarterFinalWinners(winners));
        assertTrue(manager.getSemiFinals().contains(null));
    }

    @Test
    public void SetQuarterFinalWinners_DoiTrung_ChoPhepTrung() {
        List<String> winners = Arrays.asList("Brazil", "Brazil", "W3", "W4");

        assertDoesNotThrow(() -> manager.setQuarterFinalWinners(winners));
        assertEquals(2, Collections.frequency(manager.getSemiFinals(), "Brazil"));
    }

    // ========== SET SEMI FINAL WINNERS TESTS - Phân hoạch tương đương ==========

    @Test
    public void SetSemiFinalWinners_Dung2DoiMoiList_CapNhatThanhCong() {
        List<String> finalists = Arrays.asList("Brazil", "Argentina");
        List<String> bronzeContenders = Arrays.asList("Germany", "France");

        assertDoesNotThrow(() -> manager.setSemiFinalWinners(finalists, bronzeContenders));
        assertEquals(2, manager.getFinals().size());
        assertEquals(2, manager.getBronzeWinners().size());
        assertEquals(finalists, manager.getFinals());
        assertEquals(bronzeContenders, manager.getBronzeWinners());
    }

    @Test
    public void SetSemiFinalWinners_0DoiFinalists_ThrowIllegalArgumentException() {
        List<String> finalists = new ArrayList<>();
        List<String> bronzeContenders = Arrays.asList("Germany", "France");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            manager.setSemiFinalWinners(finalists, bronzeContenders);
        });
        assertTrue(exception.getMessage().contains("2"));
    }

    @Test
    public void SetSemiFinalWinners_1DoiFinalists_ThrowIllegalArgumentException() {
        List<String> finalists = Arrays.asList("Brazil");
        List<String> bronzeContenders = Arrays.asList("Germany", "France");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.setSemiFinalWinners(finalists, bronzeContenders);
        });
    }

    @Test
    public void SetSemiFinalWinners_3DoiFinalists_ThrowIllegalArgumentException() {
        List<String> finalists = Arrays.asList("Brazil", "Argentina", "Germany");
        List<String> bronzeContenders = Arrays.asList("France", "Spain");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.setSemiFinalWinners(finalists, bronzeContenders);
        });
    }

    @Test
    public void SetSemiFinalWinners_0DoiBronze_ThrowIllegalArgumentException() {
        List<String> finalists = Arrays.asList("Brazil", "Argentina");
        List<String> bronzeContenders = new ArrayList<>();

        assertThrows(IllegalArgumentException.class, () -> {
            manager.setSemiFinalWinners(finalists, bronzeContenders);
        });
    }

    @Test
    public void SetSemiFinalWinners_1DoiBronze_ThrowIllegalArgumentException() {
        List<String> finalists = Arrays.asList("Brazil", "Argentina");
        List<String> bronzeContenders = Arrays.asList("Germany");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.setSemiFinalWinners(finalists, bronzeContenders);
        });
    }

    @Test
    public void SetSemiFinalWinners_3DoiBronze_ThrowIllegalArgumentException() {
        List<String> finalists = Arrays.asList("Brazil", "Argentina");
        List<String> bronzeContenders = Arrays.asList("Germany", "France", "Spain");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.setSemiFinalWinners(finalists, bronzeContenders);
        });
    }

    @Test
    public void SetSemiFinalWinners_FinalistsNull_ThrowNullPointerException() {
        List<String> bronzeContenders = Arrays.asList("Germany", "France");

        assertThrows(NullPointerException.class, () -> {
            manager.setSemiFinalWinners(null, bronzeContenders);
        });
    }

    @Test
    public void SetSemiFinalWinners_BronzeNull_ThrowNullPointerException() {
        List<String> finalists = Arrays.asList("Brazil", "Argentina");

        assertThrows(NullPointerException.class, () -> {
            manager.setSemiFinalWinners(finalists, null);
        });
    }

    @Test
    public void SetSemiFinalWinners_CaHaiListNull_ThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            manager.setSemiFinalWinners(null, null);
        });
    }

    @Test
    public void SetSemiFinalWinners_CoDoiNull_ChoPhepNull() {
        List<String> finalists = Arrays.asList("Brazil", null);
        List<String> bronzeContenders = Arrays.asList(null, "France");

        assertDoesNotThrow(() -> manager.setSemiFinalWinners(finalists, bronzeContenders));
        assertTrue(manager.getFinals().contains(null));
        assertTrue(manager.getBronzeWinners().contains(null));
    }

    @Test
    public void SetSemiFinalWinners_DoiTrung_ChoPhepTrung() {
        List<String> finalists = Arrays.asList("Brazil", "Brazil");
        List<String> bronzeContenders = Arrays.asList("Germany", "Germany");

        assertDoesNotThrow(() -> manager.setSemiFinalWinners(finalists, bronzeContenders));
        assertEquals(2, Collections.frequency(manager.getFinals(), "Brazil"));
        assertEquals(2, Collections.frequency(manager.getBronzeWinners(), "Germany"));
    }

    // ========== SET FINAL RESULT TESTS - Phân hoạch tương đương ==========

    @Test
    public void SetFinalResult_HaiDoiHopLe_GanVoVaAQuan() {
        manager.setFinalResult("Brazil", "Argentina");

        assertEquals("Brazil", manager.getChampion());
        assertEquals("Argentina", manager.getRunnerUp());
    }

    @Test
    public void SetFinalResult_VoNull_ChoPhepNull() {
        manager.setFinalResult(null, "Argentina");

        assertNull(manager.getChampion());
        assertEquals("Argentina", manager.getRunnerUp());
    }

    @Test
    public void SetFinalResult_AQuanNull_ChoPhepNull() {
        manager.setFinalResult("Brazil", null);

        assertEquals("Brazil", manager.getChampion());
        assertNull(manager.getRunnerUp());
    }

    @Test
    public void SetFinalResult_CaHaiNull_ChoPhepCaHaiNull() {
        manager.setFinalResult(null, null);

        assertNull(manager.getChampion());
        assertNull(manager.getRunnerUp());
    }

    @Test
    public void SetFinalResult_CungMotDoi_ChoPhepCungDoi() {
        manager.setFinalResult("Brazil", "Brazil");

        assertEquals("Brazil", manager.getChampion());
        assertEquals("Brazil", manager.getRunnerUp());
    }

    @Test
    public void SetFinalResult_TenRong_ChoPhepTenRong() {
        manager.setFinalResult("", "");

        assertEquals("", manager.getChampion());
        assertEquals("", manager.getRunnerUp());
    }

    @Test
    public void SetFinalResult_TenDai_XuLyThanhCong() {
        String longName1 = "A".repeat(500);
        String longName2 = "B".repeat(500);

        manager.setFinalResult(longName1, longName2);

        assertEquals(longName1, manager.getChampion());
        assertEquals(longName2, manager.getRunnerUp());
    }

    @Test
    public void SetFinalResult_TenCoKyTuDacBiet_XuLyThanhCong() {
        manager.setFinalResult("Team@#$%", "Team with spaces & symbols!");

        assertEquals("Team@#$%", manager.getChampion());
        assertEquals("Team with spaces & symbols!", manager.getRunnerUp());
    }

    @Test
    public void SetFinalResult_GoiNhieuLan_GhiDeLanCuoi() {
        manager.setFinalResult("Brazil", "Argentina");
        assertEquals("Brazil", manager.getChampion());

        manager.setFinalResult("Germany", "France");
        assertEquals("Germany", manager.getChampion());
        assertEquals("France", manager.getRunnerUp());

        // Kiểm tra giá trị cũ đã bị ghi đè
        assertNotEquals("Brazil", manager.getChampion());
        assertNotEquals("Argentina", manager.getRunnerUp());
    }

    // ========== GETTER TESTS - Bao phủ câu lệnh ==========

    @Test
    public void GetBracketInfo_TrangThaiBanDau_TraVeMapRong() {
        Map<String, String> bracket = manager.getBracketInfo();

        assertNotNull(bracket);
        assertTrue(bracket.isEmpty());
        assertEquals(0, bracket.size());
    }

    @Test
    public void GetBracketInfo_SauKhiGenerate_TraVeMapDayDu() {
        manager.generateRoundOf16Bracket(groupWinners, groupRunnersUp);
        Map<String, String> bracket = manager.getBracketInfo();

        assertNotNull(bracket);
        assertFalse(bracket.isEmpty());
        assertEquals(8, bracket.size());
    }

    @Test
    public void GetBracketInfo_GoiNhieuLan_TraVeCungThamChieu() {
        Map<String, String> bracket1 = manager.getBracketInfo();
        Map<String, String> bracket2 = manager.getBracketInfo();

        assertSame(bracket1, bracket2);
    }

    @Test
    public void GetRoundOf16_TrangThaiBanDau_TraVeListRong() {
        List<String> roundOf16 = manager.getRoundOf16();

        assertNotNull(roundOf16);
        assertTrue(roundOf16.isEmpty());
        assertEquals(0, roundOf16.size());
    }

    @Test
    public void GetQuarterFinals_TrangThaiBanDau_TraVeListRong() {
        List<String> quarterFinals = manager.getQuarterFinals();

        assertNotNull(quarterFinals);
        assertTrue(quarterFinals.isEmpty());
    }

    @Test
    public void GetQuarterFinals_SauKhiSet_TraVeListDayDu() {
        List<String> winners = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8");
        manager.setRoundOf16Winners(winners);

        List<String> quarterFinals = manager.getQuarterFinals();
        assertEquals(8, quarterFinals.size());
        assertEquals(winners, quarterFinals);
    }

    @Test
    public void GetQuarterFinals_GoiNhieuLan_TraVeCungThamChieu() {
        List<String> qf1 = manager.getQuarterFinals();
        List<String> qf2 = manager.getQuarterFinals();

        assertSame(qf1, qf2);
    }

    @Test
    public void GetSemiFinals_TrangThaiBanDau_TraVeListRong() {
        List<String> semiFinals = manager.getSemiFinals();

        assertNotNull(semiFinals);
        assertTrue(semiFinals.isEmpty());
    }

    @Test
    public void GetSemiFinals_SauKhiSet_TraVeListDayDu() {
        List<String> winners = Arrays.asList("W1", "W2", "W3", "W4");
        manager.setQuarterFinalWinners(winners);

        List<String> semiFinals = manager.getSemiFinals();
        assertEquals(4, semiFinals.size());
        assertEquals(winners, semiFinals);
    }

    @Test
    public void GetFinals_TrangThaiBanDau_TraVeListRong() {
        List<String> finals = manager.getFinals();

        assertNotNull(finals);
        assertTrue(finals.isEmpty());
    }

    @Test
    public void GetFinals_SauKhiSet_TraVeListDayDu() {
        List<String> finalists = Arrays.asList("Brazil", "Argentina");
        List<String> bronzeContenders = Arrays.asList("Germany", "France");
        manager.setSemiFinalWinners(finalists, bronzeContenders);

        List<String> finals = manager.getFinals();
        assertEquals(2, finals.size());
        assertEquals(finalists, finals);
    }

    @Test
    public void GetBronzeWinners_TrangThaiBanDau_TraVeListRong() {
        List<String> bronzeWinners = manager.getBronzeWinners();

        assertNotNull(bronzeWinners);
        assertTrue(bronzeWinners.isEmpty());
    }

    @Test
    public void GetBronzeWinners_SauKhiSet_TraVeListDayDu() {
        List<String> finalists = Arrays.asList("Brazil", "Argentina");
        List<String> bronzeContenders = Arrays.asList("Germany", "France");
        manager.setSemiFinalWinners(finalists, bronzeContenders);

        List<String> bronzeWinners = manager.getBronzeWinners();
        assertEquals(2, bronzeWinners.size());
        assertEquals(bronzeContenders, bronzeWinners);
    }

    @Test
    public void GetChampion_TrangThaiBanDau_TraVeNull() {
        assertNull(manager.getChampion());
    }

    @Test
    public void GetChampion_SauKhiSet_TraVeGiaTriDung() {
        manager.setFinalResult("Brazil", "Argentina");
        assertEquals("Brazil", manager.getChampion());
    }

    @Test
    public void GetRunnerUp_TrangThaiBanDau_TraVeNull() {
        assertNull(manager.getRunnerUp());
    }

    @Test
    public void GetRunnerUp_SauKhiSet_TraVeGiaTriDung() {
        manager.setFinalResult("Brazil", "Argentina");
        assertEquals("Argentina", manager.getRunnerUp());
    }

    // ========== INTEGRATION TESTS - Tích hợp các chức năng ==========

    @Test
    public void KnockoutFlow_TrangThaiBanDau_TatCaRongHoacNull() {
        assertTrue(manager.getRoundOf16().isEmpty());
        assertTrue(manager.getQuarterFinals().isEmpty());
        assertTrue(manager.getSemiFinals().isEmpty());
        assertTrue(manager.getFinals().isEmpty());
        assertTrue(manager.getBronzeWinners().isEmpty());
        assertTrue(manager.getBracketInfo().isEmpty());
        assertNull(manager.getChampion());
        assertNull(manager.getRunnerUp());
    }

    @Test
    public void KnockoutFlow_QuyTrinhDayDu_HoatDongDung() {
        // 1. Generate bracket
        manager.generateRoundOf16Bracket(groupWinners, groupRunnersUp);
        assertEquals(8, manager.getBracketInfo().size());

        // 2. Set Round of 16 winners
        List<String> round16Winners = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8");
        manager.setRoundOf16Winners(round16Winners);
        assertEquals(8, manager.getQuarterFinals().size());

        // 3. Set Quarter final winners
        List<String> quarterWinners = Arrays.asList("T1", "T3", "T5", "T7");
        manager.setQuarterFinalWinners(quarterWinners);
        assertEquals(4, manager.getSemiFinals().size());

        // 4. Set Semi final winners
        List<String> finalists = Arrays.asList("T1", "T5");
        List<String> bronzeContenders = Arrays.asList("T3", "T7");
        manager.setSemiFinalWinners(finalists, bronzeContenders);
        assertEquals(2, manager.getFinals().size());
        assertEquals(2, manager.getBronzeWinners().size());

        // 5. Set final result
        manager.setFinalResult("T1", "T5");
        assertEquals("T1", manager.getChampion());
        assertEquals("T5", manager.getRunnerUp());
    }

    @Test
    public void KnockoutFlow_ThayDoiGiuaQuyTrinh_CapNhatDung() {
        // Thiết lập ban đầu
        List<String> round16Winners = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8");
        manager.setRoundOf16Winners(round16Winners);

        List<String> quarterWinners = Arrays.asList("T1", "T3", "T5", "T7");
        manager.setQuarterFinalWinners(quarterWinners);

        // Thay đổi kết quả quarter finals
        List<String> newQuarterWinners = Arrays.asList("T2", "T4", "T6", "T8");
        manager.setQuarterFinalWinners(newQuarterWinners);

        assertEquals(newQuarterWinners, manager.getSemiFinals());
        assertFalse(manager.getSemiFinals().contains("T1"));
    }

    @Test
    public void KnockoutFlow_XuLyDuLieuNull_KhongGayLoi() {
        // Test với dữ liệu null ở các bước
        List<String> winnersWithNull = Arrays.asList("T1", null, "T3", "T4", "T5", "T6", "T7", "T8");
        manager.setRoundOf16Winners(winnersWithNull);
        assertTrue(manager.getQuarterFinals().contains(null));

        List<String> quarterWithNull = Arrays.asList("T1", null, "T3", "T4");
        manager.setQuarterFinalWinners(quarterWithNull);
        assertTrue(manager.getSemiFinals().contains(null));

        List<String> finalistsWithNull = Arrays.asList("T1", null);
        List<String> bronzeWithNull = Arrays.asList(null, "T3");
        manager.setSemiFinalWinners(finalistsWithNull, bronzeWithNull);
        assertTrue(manager.getFinals().contains(null));
        assertTrue(manager.getBronzeWinners().contains(null));

        manager.setFinalResult(null, "T1");
        assertNull(manager.getChampion());
        assertEquals("T1", manager.getRunnerUp());
    }

    // ========== EDGE CASE TESTS - Các trường hợp đặc biệt ==========

    @Test
    public void EdgeCase_TatCaDoiCungTen_XuLyThanhCong() {
        List<String> sameNameWinners = Arrays.asList("Brazil", "Brazil", "Brazil", "Brazil",
                "Brazil", "Brazil", "Brazil", "Brazil");
        manager.setRoundOf16Winners(sameNameWinners);
        assertEquals(8, Collections.frequency(manager.getQuarterFinals(), "Brazil"));

        List<String> sameNameQuarter = Arrays.asList("Brazil", "Brazil", "Brazil", "Brazil");
        manager.setQuarterFinalWinners(sameNameQuarter);
        assertEquals(4, Collections.frequency(manager.getSemiFinals(), "Brazil"));
    }

    @Test
    public void EdgeCase_TenDoiRatDai_XuLyThanhCong() {
        String veryLongName = "A".repeat(10000);
        List<String> longNameWinners = Arrays.asList(veryLongName, "T2", "T3", "T4", "T5", "T6", "T7", "T8");

        assertDoesNotThrow(() -> manager.setRoundOf16Winners(longNameWinners));
        assertTrue(manager.getQuarterFinals().contains(veryLongName));
    }

    @Test
    public void EdgeCase_KyTuUnicode_XuLyThanhCong() {
        List<String> unicodeWinners = Arrays.asList("🇧🇷Brazil", "🇦🇷Argentina", "🇩🇪Germany", "🇫🇷France",
                "中国", "日本", "한국", "Россия");

        assertDoesNotThrow(() -> manager.setRoundOf16Winners(unicodeWinners));
        assertTrue(manager.getQuarterFinals().contains("🇧🇷Brazil"));
        assertTrue(manager.getQuarterFinals().contains("中国"));
    }

    @Test
    public void EdgeCase_GoiSetterLienTiep_GhiDeThanhCong() {
        // Gọi setter liên tiếp nhiều lần
        for (int i = 0; i < 100; i++) {
            List<String> winners = Arrays.asList("T" + i + "1", "T" + i + "2", "T" + i + "3", "T" + i + "4",
                    "T" + i + "5", "T" + i + "6", "T" + i + "7", "T" + i + "8");
            manager.setRoundOf16Winners(winners);
        }

        // Kiểm tra chỉ có giá trị cuối cùng
        assertTrue(manager.getQuarterFinals().contains("T991"));
        assertFalse(manager.getQuarterFinals().contains("T01"));
    }

    @Test
    public void EdgeCase_ThuTuKhongTuanTu_VanHoatDong() {
        // Gọi các method không theo thứ tự thông thường
        manager.setFinalResult("Champion", "Runner");
        assertEquals("Champion", manager.getChampion());

        List<String> finalists = Arrays.asList("F1", "F2");
        List<String> bronze = Arrays.asList("B1", "B2");
        manager.setSemiFinalWinners(finalists, bronze);
        assertEquals(finalists, manager.getFinals());

        // Champion và Runner vẫn giữ nguyên
        assertEquals("Champion", manager.getChampion());
        assertEquals("Runner", manager.getRunnerUp());
    }
}
