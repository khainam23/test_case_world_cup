package com.worldcup;

import com.worldcup.model.Match;
import com.worldcup.model.Player;
import com.worldcup.model.Substitution;
import com.worldcup.model.Team;
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
            startingPlayersA.add(new Player("Team A Starter " + i, i + 1, "MF"));
        }

        List<Player> subPlayersA = new ArrayList<>();
        for (int i = 11; i < 16; i++) {
            subPlayersA.add(new Player("Team A Sub " + i, i + 1, "FW"));
        }

        teamA = new Team("Team A", "Asia", "Coach A", assistants, "Medic A", startingPlayersA, subPlayersA, false);
        team = teamA; //Gán team dùng trong test

        // Tạo team B: 11 cầu thủ đá chính, 5 cầu thủ dự bị
        List<Player> startingPlayersB = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            startingPlayersB.add(new Player("Team B Starter " + i, i + 23, "MF"));
        }

        List<Player> subPlayersB = new ArrayList<>();
        for (int i = 11; i < 16; i++) {
            subPlayersB.add(new Player("Team B Sub " + i, i + 23, "FW"));
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
        playerIn = new Player("Player In", 20, "MF");
        playerOut = new Player("Player Out", 10, "FW");

        // Thêm vào danh sách đội
        team.addSubstitutePlayer(playerIn);
        team.addStartingPlayer(playerOut);
        
        // Thêm vào players list để isContainPlayer() trả về true
        team.getPlayers().add(playerIn);
        team.getPlayers().add(playerOut);
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


    @Test
    void SubstitutionConstructor_PhutBang0_KhoiTaoThanhCong() {
        assertDoesNotThrow(() -> {
            new Substitution(playerIn, playerOut, 0, team, match);
        });
    }

    @Test
    void SubstitutionConstructor_PhutBang1_KhoiTaoThanhCong() {
        assertDoesNotThrow(() -> {
            new Substitution(playerIn, playerOut, 1, team, match);
        });
    }

    @Test
    void SubstitutionConstructor_PhutBang45_KhoiTaoThanhCong() {
        assertDoesNotThrow(() -> {
            new Substitution(playerIn, playerOut, 45, team, match);
        });
    }

    @Test
    void SubstitutionConstructor_PhutBang90_KhoiTaoThanhCong() {
        assertDoesNotThrow(() -> {
            new Substitution(playerIn, playerOut, 90, team, match);
        });
    }

    @Test
    void SubstitutionConstructor_PhutBang149_KhoiTaoThanhCong() {
        assertDoesNotThrow(() -> {
            new Substitution(playerIn, playerOut, 149, team, match);
        });
    }

    @Test
    void SubstitutionConstructor_PhutBang150_KhoiTaoThanhCong() {
        assertDoesNotThrow(() -> {
            new Substitution(playerIn, playerOut, 150, team, match);
        });
    }

    // ========== PLAYER ELIGIBILITY TESTS ==========

    @Test
    void SubstitutionConstructor_PlayerInKhongDuDieuKien_ThrowException() {
        playerIn.receiveRedCard(); // Làm cho playerIn không đủ điều kiện
        
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không đủ điều kiện"));
    }

    @Test
    void SubstitutionConstructor_PlayerOutKhongDuDieuKien_ThrowException() {
        playerOut.receiveRedCard(); // Làm cho playerOut không đủ điều kiện
        
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không đủ điều kiện"));
    }

    @Test
    void SubstitutionConstructor_PlayerInCo2TheVang_ThrowException() {
        playerIn.receiveYellowCard();
        playerIn.receiveYellowCard(); // 2 thẻ vàng = bị đuổi
        
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không đủ điều kiện"));
    }

    @Test
    void SubstitutionConstructor_PlayerOutCo2TheVang_ThrowException() {
        playerOut.receiveYellowCard();
        playerOut.receiveYellowCard(); // 2 thẻ vàng = bị đuổi
        
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, playerOut, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không đủ điều kiện"));
    }

    @Test
    void SubstitutionConstructor_PlayerInCo1TheVang_KhoiTaoThanhCong() {
        playerIn.receiveYellowCard(); // 1 thẻ vàng vẫn đủ điều kiện
        
        assertDoesNotThrow(() -> {
            new Substitution(playerIn, playerOut, 60, team, match);
        });
    }

    @Test
    void SubstitutionConstructor_PlayerOutCo1TheVang_KhoiTaoThanhCong() {
        playerOut.receiveYellowCard(); // 1 thẻ vàng vẫn đủ điều kiện
        
        assertDoesNotThrow(() -> {
            new Substitution(playerIn, playerOut, 60, team, match);
        });
    }

    // ========== SUCCESSFUL SUBSTITUTION TESTS ==========

    @Test
    void SubstitutionConstructor_TatCaDieuKienHopLe_KhoiTaoThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        
        assertEquals(playerIn, substitution.getInPlayer());
        assertEquals(playerOut, substitution.getOutPlayer());
        assertEquals(60, substitution.getMinute());
        assertEquals(team, substitution.getTeam());
        assertEquals(match, substitution.getMatch());
    }

    @Test
    void SubstitutionConstructor_KiemTraCapNhatDanhSach_ThanhCong() {
        // Kiểm tra trạng thái trước khi thay người
        assertTrue(team.getSubstitutePlayers().contains(playerIn));
        assertTrue(team.getStartingPlayers().contains(playerOut));
        assertFalse(team.getStartingPlayers().contains(playerIn));
        assertFalse(team.getSubstitutePlayers().contains(playerOut));
        
        new Substitution(playerIn, playerOut, 60, team, match);
        
        // Kiểm tra trạng thái sau khi thay người
        assertFalse(team.getSubstitutePlayers().contains(playerIn));
        assertFalse(team.getStartingPlayers().contains(playerOut));
        assertTrue(team.getStartingPlayers().contains(playerIn));
        assertTrue(team.getSubstitutePlayers().contains(playerOut));
    }

    @Test
    void SubstitutionConstructor_KiemTraThemVaoMatch_ThanhCong() {
        int initialSubCount = match.getSubstitutions().size();
        
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        
        assertEquals(initialSubCount + 1, match.getSubstitutions().size());
        assertTrue(match.getSubstitutions().contains(substitution));
    }

    // ========== GETTER METHOD TESTS ==========

    @Test
    void GetInPlayer_TraVePlayerIn_ThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertEquals(playerIn, substitution.getInPlayer());
    }

    @Test
    void GetOutPlayer_TraVePlayerOut_ThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertEquals(playerOut, substitution.getOutPlayer());
    }

    @Test
    void GetMinute_TraVePhut_ThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 75, team, match);
        assertEquals(75, substitution.getMinute());
    }

    @Test
    void GetTeam_TraVeTeam_ThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertEquals(team, substitution.getTeam());
    }

    @Test
    void GetMatch_TraVeMatch_ThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        assertEquals(match, substitution.getMatch());
    }

    // ========== toString METHOD TEST ==========

    @Test
    void ToString_ChuoiMoTaHopLe_ThanhCong() {
        Substitution substitution = new Substitution(playerIn, playerOut, 60, team, match);
        String result = substitution.toString();
        
        assertTrue(result.contains("Player In"));
        assertTrue(result.contains("Player Out"));
        assertTrue(result.contains("60"));
        assertTrue(result.contains("Team A"));
        assertTrue(result.contains("Team B"));
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
        Player alreadyStarting = new Player("Already Starting", 24, "FW");
        team.addStartingPlayer(alreadyStarting);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(alreadyStarting, playerOut, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không thuộc danh sách dự bị"));
    }

    @Test
    void SubstitutionConstructor_PlayerOutDaTrongDanhSachDuBi_ThrowException() {
        Player alreadySub = new Player("Already Sub", 25, "DF");
        team.addSubstitutePlayer(alreadySub);

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new Substitution(playerIn, alreadySub, 60, team, match)
        );
        assertTrue(ex.getMessage().contains("không thuộc danh sách thi đấu chính"));
    }
}