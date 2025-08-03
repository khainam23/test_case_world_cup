package com.worldcup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubstitutionTest {

    private Team team;
    private Team teamA;
    private Team teamB;
    private Match match;
    private Player playerIn;
    private Player playerOut;

    @BeforeEach
    void setUp() {
        // Tạo danh sách trợ lý
        List<String> assistants = Arrays.asList("Assistant 1", "Assistant 2");

        //  Tạo team A: 11 cầu thủ đá chính, 5 cầu thủ dự bị
        List<Player> startingPlayersA = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            startingPlayersA.add(new Player("Team A Starter " + i, i + 1, "MF", "Team A"));
        }

        List<Player> subPlayersA = new ArrayList<>();
        for (int i = 11; i < 16; i++) {
            subPlayersA.add(new Player("Team A Sub " + i, i + 1, "FW", "Team A"));
        }

        teamA = new Team("Team A", "Asia", "Coach A", assistants, "Medic A", startingPlayersA, subPlayersA, false);
        team = teamA; //Gán team dùng trong test

        // Tạo team B: 11 cầu thủ đá chính, 5 cầu thủ dự bị
        List<Player> startingPlayersB = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            startingPlayersB.add(new Player("Team B Starter " + i, i + 23, "MF", "Team B"));
        }

        List<Player> subPlayersB = new ArrayList<>();
        for (int i = 11; i < 16; i++) {
            subPlayersB.add(new Player("Team B Sub " + i, i + 23, "FW", "Team B"));
        }

        teamB = new Team("Team B", "Europe", "Coach B", assistants, "Medic B", startingPlayersB, subPlayersB, false);

        // Tạo match
        match = new Match(
            teamA,
            teamB,
            teamA.getStartingPlayers(),
            teamA.getSubstitutePlayers(),
            teamB.getStartingPlayers(),
            teamB.getSubstitutePlayers(),
            false
        );

        // Tạo cầu thủ
        playerIn = new Player("Player In", 20, "MF", "Team A");
        playerOut = new Player("Player Out", 10, "FW", "Team A");

        // Thêm vào danh sách đội
        team.addSubstitutePlayer(playerIn);
        team.addStartingPlayer(playerOut);
    }

    // ========== VALID SUBSTITUTION TESTS ==========

    @Test
    void SubstitutionConstructor_LanThayNguoiDauTien_KhoiTaoThanhCong() {
        Substitution sub = new Substitution(playerIn, playerOut, 60, team, match);
        assertNotNull(sub);
        assertEquals(1, team.getSubstitutionCount());
    }

    @Test
    void SubstitutionConstructor_LanThayNguoiThuBa_KhoiTaoThanhCong() {
        team.incrementSubstitutionCount();
        team.incrementSubstitutionCount();

        Substitution sub = new Substitution(playerIn, playerOut, 80, team, match);
        assertNotNull(sub);
        assertEquals(3, team.getSubstitutionCount());
    }

    // ========== BOUNDARY VALUE TESTS - MINUTE ==========

    @Test
    void SubstitutionConstructor_PhutBang0_KhoiTaoThanhCong() {
        Substitution sub = new Substitution(playerIn, playerOut, 0, team, match);
        assertNotNull(sub);
        assertEquals(0, sub.getMinute());
    }

    @Test
    void SubstitutionConstructor_PhutBang1_KhoiTaoThanhCong() {
        Substitution sub = new Substitution(playerIn, playerOut, 1, team, match);
        assertNotNull(sub);
        assertEquals(1, sub.getMinute());
    }

    @Test
    void SubstitutionConstructor_PhutBang150_KhoiTaoThanhCong() {
        Substitution sub = new Substitution(playerIn, playerOut, 150, team, match);
        assertNotNull(sub);
        assertEquals(150, sub.getMinute());
    }

    @Test
    void SubstitutionConstructor_PhutBang149_KhoiTaoThanhCong() {
        Substitution sub = new Substitution(playerIn, playerOut, 149, team, match);
        assertNotNull(sub);
        assertEquals(149, sub.getMinute());
    }

    @Test
    void SubstitutionConstructor_PhutAmMotMot_ThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, -1, team, match)
        );
        assertTrue(ex.getMessage().contains("không hợp lệ"));
    }

    @Test
    void SubstitutionConstructor_PhutBang151_ThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, 151, team, match)
        );
        assertTrue(ex.getMessage().contains("không hợp lệ"));
    }

    @Test
    void SubstitutionConstructor_PhutAmLon_ThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, -999, team, match)
        );
        assertTrue(ex.getMessage().contains("không hợp lệ"));
    }

    @Test
    void SubstitutionConstructor_PhutLonHon200_ThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, 200, team, match)
        );
        assertTrue(ex.getMessage().contains("không hợp lệ"));
    }

    // ========== SUBSTITUTION COUNT BOUNDARY TESTS ==========

    @Test
    void SubstitutionConstructor_SoLanThayNguoiVuotQua3_ThrowException() {
        team.incrementSubstitutionCount();
        team.incrementSubstitutionCount();
        team.incrementSubstitutionCount();

        Exception ex = assertThrows(IllegalStateException.class, () ->
                new Substitution(playerIn, playerOut, 85, team, match)
        );
        assertTrue(ex.getMessage().contains("vượt quá số lần thay người"));
    }

    @Test
    void SubstitutionConstructor_SoLanThayNguoiBang3_KhoiTaoThanhCong() {
        team.incrementSubstitutionCount();
        team.incrementSubstitutionCount();

        Substitution sub = new Substitution(playerIn, playerOut, 80, team, match);
        assertNotNull(sub);
        assertEquals(3, team.getSubstitutionCount());
    }

    // ========== NULL PARAMETER TESTS ==========

    @Test
    void SubstitutionConstructor_PlayerInNull_ThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(null, playerOut, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không được null"));
    }

    @Test
    void SubstitutionConstructor_PlayerOutNull_ThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, null, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không được null"));
    }

    @Test
    void SubstitutionConstructor_TeamNull_ThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, 60, null, match)
        );
        assertTrue(ex.getMessage().contains("không được null"));
    }

    @Test
    void SubstitutionConstructor_MatchNull_ThrowException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, 60, team, null)
        );
        assertTrue(ex.getMessage().contains("không được null"));
    }

    // ========== PLAYER LIST VALIDATION TESTS ==========

    @Test
    void SubstitutionConstructor_PlayerInKhongTrongDanhSachDuBi_ThrowException() {
        team.getSubstitutePlayers().clear();

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không thuộc danh sách dự bị"));
    }

    @Test
    void SubstitutionConstructor_PlayerOutKhongTrongDanhSachChinh_ThrowException() {
        team.getStartingPlayers().clear();

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không thuộc danh sách thi đấu chính"));
    }

    @Test
    void SubstitutionConstructor_PlayerInDaTrongDanhSachChinh_ThrowException() {
        Player alreadyStarting = new Player("Already Starting", 24, "FW", "Team A");
        team.addStartingPlayer(alreadyStarting);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(alreadyStarting, playerOut, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không thuộc danh sách dự bị"));
    }

    @Test
    void SubstitutionConstructor_PlayerOutDaTrongDanhSachDuBi_ThrowException() {
        Player alreadySub = new Player("Already Sub", 25, "DF", "Team A");
        team.addSubstitutePlayer(alreadySub);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, alreadySub, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không thuộc danh sách thi đấu chính"));
    }

    // ========== PLAYER ELIGIBILITY TESTS ==========

    @Test
    void SubstitutionConstructor_PlayerInCoTheVang_KhoiTaoThanhCong() {
        playerIn.receiveYellowCard();
        assertTrue(playerIn.isEligible());

        Substitution sub = new Substitution(playerIn, playerOut, 60, team, match);
        assertNotNull(sub);
    }

    @Test
    void SubstitutionConstructor_PlayerOutCoTheVang_KhoiTaoThanhCong() {
        playerOut.receiveYellowCard();
        assertTrue(playerOut.isEligible());

        Substitution sub = new Substitution(playerIn, playerOut, 60, team, match);
        assertNotNull(sub);
    }

    @Test
    void SubstitutionConstructor_PlayerInCoTheDo_ThrowException() {
        playerIn.receiveRedCard();
        assertFalse(playerIn.isEligible());

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không đủ điều kiện"));
    }

    @Test
    void SubstitutionConstructor_PlayerOutCoHaiTheVang_ThrowException() {
        playerOut.receiveYellowCard();
        playerOut.receiveYellowCard();
        assertFalse(playerOut.isEligible());

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không đủ điều kiện"));
    }

    // ========== TEAM VALIDATION TESTS ==========

    @Test
    void SubstitutionConstructor_PlayerInKhacDoiBong_ThrowException() {
        Player outsider = new Player("Other", 99, "DF", "Team B");
        team.getSubstitutePlayers().add(outsider);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(outsider, playerOut, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không thuộc đội bóng"));
    }

    @Test
    void SubstitutionConstructor_PlayerOutKhacDoiBong_ThrowException() {
        Player outsider = new Player("Outsider", 99, "FW", "Different Team");
        team.getStartingPlayers().add(outsider);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, outsider, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không thuộc đội bóng"));
    }

    @Test
    void SubstitutionConstructor_CaHaiCauThuKhacDoiBong_ThrowException() {
        Player outsider1 = new Player("Outsider 1", 98, "FW", "Different Team");
        Player outsider2 = new Player("Outsider 2", 99, "MF", "Different Team");
        team.getSubstitutePlayers().add(outsider1);
        team.getStartingPlayers().add(outsider2);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(outsider1, outsider2, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không thuộc đội bóng"));
    }

    // ========== POSITION TESTS ==========

    @Test
    void SubstitutionConstructor_ViTriKhacNhau_KhoiTaoThanhCong() {
        Player goalkeeper = new Player("GK Player", 22, "GK", "Team A");
        Player defender = new Player("DF Player", 12, "DF", "Team A");

        team.addSubstitutePlayer(goalkeeper);
        team.addStartingPlayer(defender);

        Substitution sub = new Substitution(goalkeeper, defender, 45, team, match);
        assertNotNull(sub);
        assertEquals("GK", sub.getInPlayer().getPosition());
        assertEquals("DF", sub.getOutPlayer().getPosition());
    }

    @Test
    void SubstitutionConstructor_CungViTri_KhoiTaoThanhCong() {
        Player midfielder1 = new Player("MF Player 1", 23, "MF", "Team A");
        Player midfielder2 = new Player("MF Player 2", 13, "MF", "Team A");

        team.addSubstitutePlayer(midfielder1);
        team.addStartingPlayer(midfielder2);

        Substitution sub = new Substitution(midfielder1, midfielder2, 60, team, match);
        assertNotNull(sub);
        assertEquals("MF", sub.getInPlayer().getPosition());
        assertEquals("MF", sub.getOutPlayer().getPosition());
    }

    // ========== SPECIAL TIME TESTS ==========

    @Test
    void SubstitutionConstructor_PhutGiaiLao45_KhoiTaoThanhCong() {
        Substitution sub = new Substitution(playerIn, playerOut, 45, team, match);
        assertNotNull(sub);
        assertEquals(45, sub.getMinute());
    }

    @Test
    void SubstitutionConstructor_PhutKetThucHiepChinh90_KhoiTaoThanhCong() {
        Substitution sub = new Substitution(playerIn, playerOut, 90, team, match);
        assertNotNull(sub);
        assertEquals(90, sub.getMinute());
    }

    @Test
    void SubstitutionConstructor_PhutHiepPhu105_KhoiTaoThanhCong() {
        Substitution sub = new Substitution(playerIn, playerOut, 105, team, match);
        assertNotNull(sub);
        assertEquals(105, sub.getMinute());
    }

    @Test
    void SubstitutionConstructor_PhutKetThucHiepPhu120_KhoiTaoThanhCong() {
        Substitution sub = new Substitution(playerIn, playerOut, 120, team, match);
        assertNotNull(sub);
        assertEquals(120, sub.getMinute());
    }

    // ========== PLAYER MOVEMENT TESTS ==========

    @Test
    void SubstitutionConstructor_SauThayNguoi_PlayerInVaoDanhSachChinh() {
        new Substitution(playerIn, playerOut, 60, team, match);
        assertTrue(team.getStartingPlayers().contains(playerIn));
        assertFalse(team.getSubstitutePlayers().contains(playerIn));
    }

    @Test
    void SubstitutionConstructor_SauThayNguoi_PlayerOutVaoDanhSachDuBi() {
        new Substitution(playerIn, playerOut, 60, team, match);
        assertTrue(team.getSubstitutePlayers().contains(playerOut));
        assertFalse(team.getStartingPlayers().contains(playerOut));
    }

    // ========== MULTIPLE SUBSTITUTIONS TESTS ==========

    @Test
    void SubstitutionConstructor_NhieuLanThayNguoi_CapNhatSoLanDung() {
        // First substitution
        Player playerIn1 = new Player("Sub 1", 30, "MF", "Team A");
        Player playerOut1 = new Player("Out 1", 31, "FW", "Team A");
        team.addSubstitutePlayer(playerIn1);
        team.addStartingPlayer(playerOut1);

        new Substitution(playerIn1, playerOut1, 30, team, match);
        assertEquals(1, team.getSubstitutionCount());

        // Second substitution
        Player playerIn2 = new Player("Sub 2", 32, "DF", "Team A");
        Player playerOut2 = new Player("Out 2", 33, "MF", "Team A");
        team.addSubstitutePlayer(playerIn2);
        team.addStartingPlayer(playerOut2);

        new Substitution(playerIn2, playerOut2, 60, team, match);
        assertEquals(2, team.getSubstitutionCount());

        // Third substitution
        Player playerIn3 = new Player("Sub 3", 34, "GK", "Team A");
        Player playerOut3 = new Player("Out 3", 35, "DF", "Team A");
        team.addSubstitutePlayer(playerIn3);
        team.addStartingPlayer(playerOut3);

        new Substitution(playerIn3, playerOut3, 90, team, match);
        assertEquals(3, team.getSubstitutionCount());
    }

    // ========== GETTER METHODS TESTS ==========

    @Test
    void GetInPlayer_TraVePlayerInDung() {
        Substitution sub = new Substitution(playerIn, playerOut, 75, team, match);
        assertEquals(playerIn, sub.getInPlayer());
    }

    @Test
    void GetOutPlayer_TraVePlayerOutDung() {
        Substitution sub = new Substitution(playerIn, playerOut, 75, team, match);
        assertEquals(playerOut, sub.getOutPlayer());
    }

    @Test
    void GetMinute_TraVePhutDung() {
        Substitution sub = new Substitution(playerIn, playerOut, 75, team, match);
        assertEquals(75, sub.getMinute());
    }

    @Test
    void GetTeam_TraVeTeamDung() {
        Substitution sub = new Substitution(playerIn, playerOut, 75, team, match);
        assertEquals(team, sub.getTeam());
    }

    @Test
    void GetMatch_TraVeMatchDung() {
        Substitution sub = new Substitution(playerIn, playerOut, 75, team, match);
        assertEquals(match, sub.getMatch());
    }

    // ========== toString() METHOD TEST ==========

    @Test
    void ToString_ChuaThongTinDayDu() {
        Substitution sub = new Substitution(playerIn, playerOut, 80, team, match);
        String result = sub.toString();

        assertTrue(result.contains("Player In"));
        assertTrue(result.contains("Player Out"));
        assertTrue(result.contains("80"));
        assertTrue(result.contains("Team A"));
        assertTrue(result.contains("Team B"));
    }

    // ========== SPECIAL CHARACTER TESTS ==========

    @Test
    void SubstitutionConstructor_TenCauThuCoKyTuDacBiet_KhoiTaoThanhCong() {
        Player playerWithSpecialName = new Player("Nguyễn Văn A", 40, "MF", "Team A");
        Player playerWithAccent = new Player("José María", 41, "FW", "Team A");

        team.addSubstitutePlayer(playerWithSpecialName);
        team.addStartingPlayer(playerWithAccent);

        Substitution sub = new Substitution(playerWithSpecialName, playerWithAccent, 70, team, match);
        assertNotNull(sub);
        assertEquals("Nguyễn Văn A", sub.getInPlayer().getName());
        assertEquals("José María", sub.getOutPlayer().getName());
    }

    @Test
    void SubstitutionConstructor_TenCauThuDai_KhoiTaoThanhCong() {
        Player longNameIn = new Player("Very Long Player Name For Testing Purposes", 50, "MF", "Team A");
        Player longNameOut = new Player("Another Very Long Player Name For Testing", 51, "FW", "Team A");

        team.addSubstitutePlayer(longNameIn);
        team.addStartingPlayer(longNameOut);

        Substitution sub = new Substitution(longNameIn, longNameOut, 85, team, match);
        assertNotNull(sub);
        assertTrue(sub.toString().contains("Very Long Player Name"));
    }

    // ========== IMMUTABILITY TEST ==========

    @Test
    void SubstitutionConstructor_CacTruongKhongThayDoi_BatBien() {
        Substitution sub = new Substitution(playerIn, playerOut, 60, team, match);

        Player originalPlayerIn = sub.getInPlayer();
        Player originalPlayerOut = sub.getOutPlayer();
        int originalMinute = sub.getMinute();
        Team originalTeam = sub.getTeam();
        Match originalMatch = sub.getMatch();

        // Fields should remain the same (immutable)
        assertEquals(originalPlayerIn, sub.getInPlayer());
        assertEquals(originalPlayerOut, sub.getOutPlayer());
        assertEquals(originalMinute, sub.getMinute());
        assertEquals(originalTeam, sub.getTeam());
        assertEquals(originalMatch, sub.getMatch());
    }

    // ========== MINIMAL SETUP TEST ==========

    @Test
    void SubstitutionConstructor_DoiBoiToiThieu_KhoiTaoThanhCong() {
        // Create minimal team setup
        Team minimalTeam = new Team("Minimal Team", "Asia", "Coach",
                Arrays.asList("Assistant"), "Medic",
                new ArrayList<>(), new ArrayList<>(), false);

        Player minPlayerIn = new Player("Min In", 60, "MF", "Minimal Team");
        Player minPlayerOut = new Player("Min Out", 61, "FW", "Minimal Team");

        minimalTeam.addSubstitutePlayer(minPlayerIn);
        minimalTeam.addStartingPlayer(minPlayerOut);

        Substitution sub = new Substitution(minPlayerIn, minPlayerOut, 30, minimalTeam, match);
        assertNotNull(sub);
        assertEquals("Minimal Team", sub.getTeam().getName());
    }
}