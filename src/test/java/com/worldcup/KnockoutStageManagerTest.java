package com.worldcup;

import com.worldcup.model.KnockoutStageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnockoutStageManagerTest {

    private KnockoutStageManager manager;

    @BeforeEach
    public void setUp() {
        manager = new KnockoutStageManager();
    }

    // ========== TESTS FOR generateRoundOf16Bracket METHOD ==========

    @Test
    public void GenerateRoundOf16Bracket_DayDu8Nhom_TaoBracketThanhCong() {
        Map<String, String> winners = new HashMap<>();
        Map<String, String> runners = new HashMap<>();

        winners.put("A", "A1");
        winners.put("B", "B1");
        winners.put("C", "C1");
        winners.put("D", "D1");
        winners.put("E", "E1");
        winners.put("F", "F1");
        winners.put("G", "G1");
        winners.put("H", "H1");

        runners.put("A", "A2");
        runners.put("B", "B2");
        runners.put("C", "C2");
        runners.put("D", "D2");
        runners.put("E", "E2");
        runners.put("F", "F2");
        runners.put("G", "G2");
        runners.put("H", "H2");

        manager.generateRoundOf16Bracket(winners, runners);
        Map<String, String> bracket = manager.getBracketInfo();

        assertEquals("A1 vs B2", bracket.get("Match 1"));
        assertEquals("B1 vs A2", bracket.get("Match 2"));
        assertEquals("C1 vs D2", bracket.get("Match 3"));
        assertEquals("D1 vs C2", bracket.get("Match 4"));
        assertEquals("E1 vs F2", bracket.get("Match 5"));
        assertEquals("F1 vs E2", bracket.get("Match 6"));
        assertEquals("G1 vs H2", bracket.get("Match 7"));
        assertEquals("H1 vs G2", bracket.get("Match 8"));
    }

    @Test
    public void GenerateRoundOf16Bracket_WinnersNull_ThrowNullPointerException() {
        Map<String, String> winners = null;
        Map<String, String> runners = new HashMap<>();
        runners.put("A", "A2");

        assertThrows(NullPointerException.class, () -> manager.generateRoundOf16Bracket(winners, runners));
    }

    @Test
    public void GenerateRoundOf16Bracket_RunnersNull_ThrowNullPointerException() {
        Map<String, String> winners = new HashMap<>();
        winners.put("A", "A1");
        Map<String, String> runners = null;

        assertThrows(NullPointerException.class, () -> manager.generateRoundOf16Bracket(winners, runners));
    }

    @Test
    public void GenerateRoundOf16Bracket_MapRong_TaoBracketVoiNull() {
        Map<String, String> winners = new HashMap<>();
        Map<String, String> runners = new HashMap<>();

        manager.generateRoundOf16Bracket(winners, runners);
        Map<String, String> bracket = manager.getBracketInfo();

        assertEquals(8, bracket.size());
        assertTrue(bracket.get("Match 1").contains("null"));
    }

    @Test
    public void GenerateRoundOf16Bracket_ThieuNhom_TaoBracketVoiNull() {
        Map<String, String> winners = new HashMap<>();
        Map<String, String> runners = new HashMap<>();

        winners.put("A", "TeamA1");
        runners.put("B", "TeamB2");
        // Missing other groups

        manager.generateRoundOf16Bracket(winners, runners);
        Map<String, String> bracket = manager.getBracketInfo();

        assertEquals("TeamA1 vs TeamB2", bracket.get("Match 1"));
        assertTrue(bracket.get("Match 2").contains("null"));
    }

    @Test
    public void GenerateRoundOf16Bracket_TenDoiTrung_ChoPhepTrung() {
        Map<String, String> winners = new HashMap<>();
        Map<String, String> runners = new HashMap<>();

        winners.put("A", "Brazil");
        winners.put("B", "Brazil"); // Duplicate name
        runners.put("A", "Argentina");
        runners.put("B", "Argentina"); // Duplicate name

        manager.generateRoundOf16Bracket(winners, runners);
        Map<String, String> bracket = manager.getBracketInfo();

        assertEquals("Brazil vs Argentina", bracket.get("Match 1"));
        assertEquals("Brazil vs Argentina", bracket.get("Match 2"));
    }

    @Test
    public void GenerateRoundOf16Bracket_KyTuDacBiet_XuLyThanhCong() {
        Map<String, String> winners = new HashMap<>();
        Map<String, String> runners = new HashMap<>();

        winners.put("A", "Team@#$%");
        winners.put("B", "Team with spaces");
        runners.put("A", "Tëam-Ñamé");
        runners.put("B", "123Team456");

        manager.generateRoundOf16Bracket(winners, runners);
        Map<String, String> bracket = manager.getBracketInfo();

        assertEquals("Team@#$% vs 123Team456", bracket.get("Match 1"));
        assertEquals("Team with spaces vs Tëam-Ñamé", bracket.get("Match 2"));
    }

    @Test
    public void GenerateRoundOf16Bracket_GoiNhieuLan_GhiDeLanCuoi() {
        Map<String, String> winners1 = new HashMap<>();
        Map<String, String> runners1 = new HashMap<>();
        winners1.put("A", "Team1");
        runners1.put("B", "Team2");

        manager.generateRoundOf16Bracket(winners1, runners1);
        int firstSize = manager.getBracketInfo().size();

        Map<String, String> winners2 = new HashMap<>();
        Map<String, String> runners2 = new HashMap<>();
        winners2.put("A", "NewTeam1");
        runners2.put("B", "NewTeam2");

        manager.generateRoundOf16Bracket(winners2, runners2);
        int secondSize = manager.getBracketInfo().size();

        assertEquals(firstSize, secondSize);
        assertEquals("NewTeam1 vs NewTeam2", manager.getBracketInfo().get("Match 1"));
    }

    // ========== TESTS FOR setRoundOf16Winners METHOD ==========

    @Test
    public void SetRoundOf16Winners_Dung8Doi_KhoiTaoThanhCong() {
        List<String> winners = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8");
        assertDoesNotThrow(() -> manager.setRoundOf16Winners(winners));
        assertEquals(8, manager.getQuarterFinals().size());
        assertEquals("T1", manager.getQuarterFinals().get(0));
        assertEquals("T8", manager.getQuarterFinals().get(7));
    }

    @Test
    public void SetRoundOf16Winners_7Doi_ThrowIllegalArgumentException() {
        List<String> winners = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7");
        assertThrows(IllegalArgumentException.class, () -> manager.setRoundOf16Winners(winners));
    }

    @Test
    public void SetRoundOf16Winners_9Doi_ThrowIllegalArgumentException() {
        List<String> winners = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9");
        assertThrows(IllegalArgumentException.class, () -> manager.setRoundOf16Winners(winners));
    }

    @Test
    public void SetRoundOf16Winners_0Doi_ThrowIllegalArgumentException() {
        List<String> winners = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> manager.setRoundOf16Winners(winners));
    }

    @Test
    public void SetRoundOf16Winners_ListNull_ThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> manager.setRoundOf16Winners(null));
    }

    @Test
    public void SetRoundOf16Winners_CoDoiNull_ChoPhepNull() {
        List<String> winners = Arrays.asList("T1", null, "T3", "T4", "T5", "T6", "T7", "T8");
        assertDoesNotThrow(() -> manager.setRoundOf16Winners(winners));
        assertTrue(manager.getQuarterFinals().contains(null));
    }

    @Test
    public void SetRoundOf16Winners_DoiTrung_ChoPhepTrung() {
        List<String> winners = Arrays.asList("Brazil", "Brazil", "T3", "T4", "T5", "T6", "T7", "T8");
        assertDoesNotThrow(() -> manager.setRoundOf16Winners(winners));
        assertEquals(2, Collections.frequency(manager.getQuarterFinals(), "Brazil"));
    }

    // ========== TESTS FOR setQuarterFinalWinners METHOD ==========

    @Test
    public void SetQuarterFinalWinners_Dung4Thang4Thua_CapNhatThanhCong() {
        List<String> winners = Arrays.asList("W1", "W2", "W3", "W4");
        List<String> losers = Arrays.asList("L1", "L2", "L3", "L4");

        manager.setQuarterFinalWinners(winners);

        assertEquals(winners, manager.getSemiFinals());
    }

    @Test
    public void SetQuarterFinalWinners_3Thang4Thua_ThrowIllegalArgumentException() {
        List<String> winners = Arrays.asList("W1", "W2", "W3");
        List<String> losers = Arrays.asList("L1", "L2", "L3", "L4");

        assertThrows(IllegalArgumentException.class, () -> manager.setQuarterFinalWinners(winners));
    }

    @Test
    public void SetQuarterFinalWinners_5Thang4Thua_ThrowIllegalArgumentException() {
        List<String> winners = Arrays.asList("W1", "W2", "W3", "W4", "W5");
        List<String> losers = Arrays.asList("L1", "L2", "L3", "L4");

        assertThrows(IllegalArgumentException.class, () -> manager.setQuarterFinalWinners(winners));
    }


    @Test
    public void SetQuarterFinalWinners_CaHaiListRong_ThrowIllegalArgumentException() {
        List<String> winners = new ArrayList<>();
        List<String> losers = new ArrayList<>();

        assertThrows(IllegalArgumentException.class, () -> manager.setQuarterFinalWinners(winners));
    }

    // ========== TESTS FOR setSemiFinalWinners METHOD ==========

    @Test
    public void SetSemiFinalWinners_1Doi_ThrowIllegalArgumentException() {
        List<String> finalists = Arrays.asList("Brazil");
        assertThrows(IllegalArgumentException.class, () -> manager.setSemiFinalWinners(finalists, finalists));
    }

    @Test
    public void SetSemiFinalWinners_3Doi_ThrowIllegalArgumentException() {
        List<String> finalists = Arrays.asList("Brazil", "Argentina", "Germany");
        assertThrows(IllegalArgumentException.class, () -> manager.setSemiFinalWinners(finalists, finalists));
    }

    @Test
    public void SetSemiFinalWinners_0Doi_ThrowIllegalArgumentException() {
        List<String> finalists = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> manager.setSemiFinalWinners(finalists, finalists));
    }

    @Test
    public void SetSemiFinalWinners_ListNull_ThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> manager.setSemiFinalWinners(null, null));
    }

    @Test
    public void SetSemiFinalWinners_CoDoiNull_ChoPhepNull() {
        List<String> finalists = Arrays.asList("Brazil", null);
        assertDoesNotThrow(() -> manager.setSemiFinalWinners(finalists, finalists));
        assertTrue(manager.getFinals().contains(null));
    }

    @Test
    public void SetSemiFinalWinners_DoiTrung_ChoPhepTrung() {
        List<String> finalists = Arrays.asList("Brazil", "Brazil");
        assertDoesNotThrow(() -> manager.setSemiFinalWinners(finalists, finalists));
        assertEquals(2, Collections.frequency(manager.getFinals(), "Brazil"));
    }

    // ========== TESTS FOR setFinalResult METHOD ==========

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
    public void SetFinalResult_CungMotDoi_ChoPhepCungDoi() {
        manager.setFinalResult("Brazil", "Brazil");

        assertEquals("Brazil", manager.getChampion());
        assertEquals("Brazil", manager.getRunnerUp());
    }

    @Test
    public void SetFinalResult_GoiNhieuLan_GhiDeLanCuoi() {
        manager.setFinalResult("Brazil", "Argentina");
        manager.setFinalResult("Germany", "France");

        assertEquals("Germany", manager.getChampion());
        assertEquals("France", manager.getRunnerUp());
    }

    @Test
    public void SetFinalResult_CaHaiNull_ChoPhepCaHaiNull() {
        manager.setFinalResult(null, null);

        assertNull(manager.getChampion());
        assertNull(manager.getRunnerUp());
    }

    // ========== TESTS FOR GETTER METHODS ==========

    @Test
    public void GetBracketInfo_TrangThaiBanDau_TraVeMapRong() {
        assertTrue(manager.getBracketInfo().isEmpty());
    }

    @Test
    public void GetBronzeWinners_TrangThaiBanDau_TraVeListRong() {
        assertTrue(manager.getBronzeWinners().isEmpty());
    }

    @Test
    public void GetRoundOf16_TrangThaiBanDau_TraVeListRong() {
        assertTrue(manager.getRoundOf16().isEmpty());
    }

    @Test
    public void GetQuarterFinals_TrangThaiBanDau_TraVeListRong() {
        assertTrue(manager.getQuarterFinals().isEmpty());
    }

    @Test
    public void GetSemiFinals_TrangThaiBanDau_TraVeListRong() {
        assertTrue(manager.getSemiFinals().isEmpty());
    }

    @Test
    public void GetFinals_TrangThaiBanDau_TraVeListRong() {
        assertTrue(manager.getFinals().isEmpty());
    }

    @Test
    public void GetChampion_TrangThaiBanDau_TraVeNull() {
        assertNull(manager.getChampion());
    }

    @Test
    public void GetRunnerUp_TrangThaiBanDau_TraVeNull() {
        assertNull(manager.getRunnerUp());
    }

    @Test
    public void GetBracketInfo_GoiNhieuLan_TraVeCungThamChieu() {
        Map<String, String> bracket1 = manager.getBracketInfo();
        Map<String, String> bracket2 = manager.getBracketInfo();

        assertSame(bracket1, bracket2);
    }

    @Test
    public void GetQuarterFinals_GoiNhieuLan_TraVeCungThamChieu() {
        List<String> qf1 = manager.getQuarterFinals();
        List<String> qf2 = manager.getQuarterFinals();

        assertSame(qf1, qf2);
    }

    // ========== INTEGRATION TESTS ==========

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

    // ========== BOUNDARY VALUE TESTS ==========

    @Test
    public void SetRoundOf16Winners_GiaTriBien7_ThrowException() {
        List<String> winners = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7");
        assertThrows(IllegalArgumentException.class, () -> manager.setRoundOf16Winners(winners));
    }

    @Test
    public void SetRoundOf16Winners_GiaTriBien8_ThanhCong() {
        List<String> winners = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8");
        assertDoesNotThrow(() -> manager.setRoundOf16Winners(winners));
        assertEquals(8, manager.getQuarterFinals().size());
    }

    @Test
    public void SetRoundOf16Winners_GiaTriBien9_ThrowException() {
        List<String> winners = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9");
        assertThrows(IllegalArgumentException.class, () -> manager.setRoundOf16Winners(winners));
    }

    @Test
    public void SetQuarterFinalWinners_GiaTriBien3_ThrowException() {
        List<String> winners = Arrays.asList("W1", "W2", "W3");
        assertThrows(IllegalArgumentException.class, () -> manager.setQuarterFinalWinners(winners));
    }

    @Test
    public void SetQuarterFinalWinners_GiaTriBien4_ThanhCong() {
        List<String> winners = Arrays.asList("W1", "W2", "W3", "W4");
        assertDoesNotThrow(() -> manager.setQuarterFinalWinners(winners));
    }

    @Test
    public void SetQuarterFinalWinners_GiaTriBien5_ThrowException() {
        List<String> winners = Arrays.asList("W1", "W2", "W3", "W4", "W5");
        assertThrows(IllegalArgumentException.class, () -> manager.setQuarterFinalWinners(winners));
    }

    @Test
    public void SetSemiFinalWinners_GiaTriBien1_ThrowException() {
        List<String> finalists = Arrays.asList("F1");
        assertThrows(IllegalArgumentException.class, () -> manager.setSemiFinalWinners(finalists, null));
    }

}
