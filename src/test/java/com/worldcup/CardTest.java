package com.worldcup;

import com.worldcup.model.Card;
import com.worldcup.model.Card.CardType;
import com.worldcup.model.Player;
import com.worldcup.model.Team;
import com.worldcup.model.Match;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class CardTest {

    private Player validPlayer;
    private Team validTeam;
    private Match validMatch;
    private List<Player> startingPlayers;
    private List<Player> substitutePlayers;
    private List<String> assistants;

    @BeforeEach
    void setUp() {
        // Tạo player hợp lệ
        validPlayer = new Player("Lionel Messi", 10, "Forward");
        
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
        validTeam = new Team("Argentina", "South America", "Coach", assistants, 
                           "Medical Staff", startingPlayers, substitutePlayers, false);
        
        // Tạo match hợp lệ
        Team teamB = new Team("Brazil", "South America", "Coach B", assistants, 
                            "Medical B", startingPlayers, substitutePlayers, false);
        validMatch = new Match(validTeam, teamB, "2024/12/15", "Stadium", false);
    }

    // ========== CONSTRUCTOR TESTS ==========

    @Test
    void CardConstructor_ThamSoHopLe_KhoiTaoThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 45, CardType.YELLOW);
        
        assertEquals(validPlayer, card.getPlayer());
        assertEquals(validTeam, card.getTeam());
        assertEquals(validMatch, card.getMatch());
        assertEquals(45, card.getMinutes());
        assertEquals(CardType.YELLOW, card.getType());
        assertEquals(0, card.getId()); // ID mặc định là 0
    }

    @Test
    void CardConstructor_TheVang_KhoiTaoThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 30, CardType.YELLOW);
        assertEquals(CardType.YELLOW, card.getType());
    }

    @Test
    void CardConstructor_TheDo_KhoiTaoThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 60, CardType.RED);
        assertEquals(CardType.RED, card.getType());
    }

    // Test phân hoạch giá trị biên cho minutes
    @Test
    void CardConstructor_Phut0_KhoiTaoThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 0, CardType.YELLOW);
        assertEquals(0, card.getMinutes());
    }

    @Test
    void CardConstructor_Phut1_KhoiTaoThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 1, CardType.YELLOW);
        assertEquals(1, card.getMinutes());
    }

    @Test
    void CardConstructor_Phut45_KhoiTaoThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 45, CardType.YELLOW);
        assertEquals(45, card.getMinutes());
    }

    @Test
    void CardConstructor_Phut90_KhoiTaoThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 90, CardType.YELLOW);
        assertEquals(90, card.getMinutes());
    }

    @Test
    void CardConstructor_Phut120_KhoiTaoThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 120, CardType.YELLOW);
        assertEquals(120, card.getMinutes());
    }

    @Test
    void CardConstructor_Phut149_KhoiTaoThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 149, CardType.YELLOW);
        assertEquals(149, card.getMinutes());
    }

    @Test
    void CardConstructor_Phut150_KhoiTaoThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 150, CardType.YELLOW);
        assertEquals(150, card.getMinutes());
    }

    @Test
    void CardConstructor_PhutAm1_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(validPlayer, validTeam, validMatch, -1, CardType.YELLOW);
        });
    }

    @Test
    void CardConstructor_PhutAm10_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(validPlayer, validTeam, validMatch, -10, CardType.YELLOW);
        });
    }

    @Test
    void CardConstructor_Phut151_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(validPlayer, validTeam, validMatch, 151, CardType.YELLOW);
        });
    }

    @Test
    void CardConstructor_Phut200_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(validPlayer, validTeam, validMatch, 200, CardType.YELLOW);
        });
    }

    // Test null parameters
    @Test
    void CardConstructor_PlayerNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(null, validTeam, validMatch, 45, CardType.YELLOW);
        });
    }

    @Test
    void CardConstructor_TeamNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(validPlayer, null, validMatch, 45, CardType.YELLOW);
        });
    }

    @Test
    void CardConstructor_MatchNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(validPlayer, validTeam, null, 45, CardType.YELLOW);
        });
    }

    @Test
    void CardConstructor_CardTypeNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(validPlayer, validTeam, validMatch, 45, null);
        });
    }

    @Test
    void CardConstructor_TatCaThamSoNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(null, null, null, 45, null);
        });
    }

    // ========== GETTER TESTS ==========

    @Test
    void GetPlayer_CardHopLe_TraVePlayer() {
        Card card = new Card(validPlayer, validTeam, validMatch, 45, CardType.YELLOW);
        assertEquals(validPlayer, card.getPlayer());
        assertEquals("Lionel Messi", card.getPlayer().getName());
    }

    @Test
    void GetTeam_CardHopLe_TraVeTeam() {
        Card card = new Card(validPlayer, validTeam, validMatch, 45, CardType.YELLOW);
        assertEquals(validTeam, card.getTeam());
        assertEquals("Argentina", card.getTeam().getName());
    }

    @Test
    void GetMatch_CardHopLe_TraVeMatch() {
        Card card = new Card(validPlayer, validTeam, validMatch, 45, CardType.YELLOW);
        assertEquals(validMatch, card.getMatch());
    }

    @Test
    void GetMinutes_CardHopLe_TraVeMinutes() {
        Card card = new Card(validPlayer, validTeam, validMatch, 75, CardType.RED);
        assertEquals(75, card.getMinutes());
    }

    @Test
    void GetType_TheVang_TraVeYellow() {
        Card card = new Card(validPlayer, validTeam, validMatch, 45, CardType.YELLOW);
        assertEquals(CardType.YELLOW, card.getType());
    }

    @Test
    void GetType_TheDo_TraVeRed() {
        Card card = new Card(validPlayer, validTeam, validMatch, 45, CardType.RED);
        assertEquals(CardType.RED, card.getType());
    }

    @Test
    void GetId_MacDinh_TraVe0() {
        Card card = new Card(validPlayer, validTeam, validMatch, 45, CardType.YELLOW);
        assertEquals(0, card.getId());
    }

    // ========== SETTER TESTS ==========

    @Test
    void SetId_GiaTriDuong_SetThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 45, CardType.YELLOW);
        card.setId(100);
        assertEquals(100, card.getId());
    }

    @Test
    void SetId_GiaTriAm_SetThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 45, CardType.YELLOW);
        card.setId(-1);
        assertEquals(-1, card.getId());
    }

    @Test
    void SetId_GiaTri0_SetThanhCong() {
        Card card = new Card(validPlayer, validTeam, validMatch, 45, CardType.YELLOW);
        card.setId(0);
        assertEquals(0, card.getId());
    }

    // ========== CARDTYPE ENUM TESTS ==========

    @Test
    void CardType_Yellow_CoLabelDung() {
        assertEquals("Yellow", CardType.YELLOW.getLabel());
    }

    @Test
    void CardType_Red_CoLabelDung() {
        assertEquals("Red", CardType.RED.getLabel());
    }

    @Test
    void CardTypeFromLabel_Yellow_TraVeYellow() {
        assertEquals(CardType.YELLOW, CardType.fromLabel("Yellow"));
    }

    @Test
    void CardTypeFromLabel_Red_TraVeRed() {
        assertEquals(CardType.RED, CardType.fromLabel("Red"));
    }

    @Test
    void CardTypeFromLabel_YellowKhongPhanBietHoaThuong_TraVeYellow() {
        assertEquals(CardType.YELLOW, CardType.fromLabel("yellow"));
        assertEquals(CardType.YELLOW, CardType.fromLabel("YELLOW"));
        assertEquals(CardType.YELLOW, CardType.fromLabel("YeLLoW"));
    }

    @Test
    void CardTypeFromLabel_RedKhongPhanBietHoaThuong_TraVeRed() {
        assertEquals(CardType.RED, CardType.fromLabel("red"));
        assertEquals(CardType.RED, CardType.fromLabel("RED"));
        assertEquals(CardType.RED, CardType.fromLabel("ReD"));
    }

    @Test
    void CardTypeFromLabel_LabelKhongHopLe_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            CardType.fromLabel("Blue");
        });
    }

    @Test
    void CardTypeFromLabel_LabelRong_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            CardType.fromLabel("");
        });
    }

    @Test
    void CardTypeFromLabel_LabelNull_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            CardType.fromLabel(null);
        });
    }

    @Test
    void CardTypeFromLabel_LabelKhoangTrang_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            CardType.fromLabel("   ");
        });
    }

    // ========== TOSTRING TESTS ==========

    @Test
    void ToString_TheVang_ChuoiDungDinhDang() {
        Card card = new Card(validPlayer, validTeam, validMatch, 45, CardType.YELLOW);
        String expected = "Card{player=Lionel Messi, team=Argentina, minutes=45, type=Yellow}";
        assertEquals(expected, card.toString());
    }

    @Test
    void ToString_TheDo_ChuoiDungDinhDang() {
        Card card = new Card(validPlayer, validTeam, validMatch, 90, CardType.RED);
        String expected = "Card{player=Lionel Messi, team=Argentina, minutes=90, type=Red}";
        assertEquals(expected, card.toString());
    }

    @Test
    void ToString_Phut0_ChuoiDungDinhDang() {
        Card card = new Card(validPlayer, validTeam, validMatch, 0, CardType.YELLOW);
        String expected = "Card{player=Lionel Messi, team=Argentina, minutes=0, type=Yellow}";
        assertEquals(expected, card.toString());
    }

    @Test
    void ToString_Phut150_ChuoiDungDinhDang() {
        Card card = new Card(validPlayer, validTeam, validMatch, 150, CardType.RED);
        String expected = "Card{player=Lionel Messi, team=Argentina, minutes=150, type=Red}";
        assertEquals(expected, card.toString());
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    void Card_ToanBoChucNang_HoatDongDung() {
        // Tạo card
        Card card = new Card(validPlayer, validTeam, validMatch, 75, CardType.RED);
        
        // Kiểm tra constructor
        assertEquals(validPlayer, card.getPlayer());
        assertEquals(validTeam, card.getTeam());
        assertEquals(validMatch, card.getMatch());
        assertEquals(75, card.getMinutes());
        assertEquals(CardType.RED, card.getType());
        assertEquals(0, card.getId());
        
        // Kiểm tra setter
        card.setId(999);
        assertEquals(999, card.getId());
        
        // Kiểm tra toString
        String expected = "Card{player=Lionel Messi, team=Argentina, minutes=75, type=Red}";
        assertEquals(expected, card.toString());
    }

    @Test
    void Card_NhieuCardCungPlayer_TaoThanhCong() {
        Card yellowCard = new Card(validPlayer, validTeam, validMatch, 30, CardType.YELLOW);
        Card redCard = new Card(validPlayer, validTeam, validMatch, 60, CardType.RED);
        
        assertEquals(validPlayer, yellowCard.getPlayer());
        assertEquals(validPlayer, redCard.getPlayer());
        assertEquals(CardType.YELLOW, yellowCard.getType());
        assertEquals(CardType.RED, redCard.getType());
        assertNotEquals(yellowCard.getMinutes(), redCard.getMinutes());
    }

    // ========== BOUNDARY VALUE ANALYSIS TESTS ==========

    @Test
    void CardConstructor_GiaTriBienDuoiCuaMinutes_KhoiTaoThanhCong() {
        // Giá trị biên dưới: 0
        Card card = new Card(validPlayer, validTeam, validMatch, 0, CardType.YELLOW);
        assertEquals(0, card.getMinutes());
    }

    @Test
    void CardConstructor_GiaTriBienTrenCuaMinutes_KhoiTaoThanhCong() {
        // Giá trị biên trên: 150
        Card card = new Card(validPlayer, validTeam, validMatch, 150, CardType.RED);
        assertEquals(150, card.getMinutes());
    }

    @Test
    void CardConstructor_GiaTriNgoaiBienDuoi_ThrowException() {
        // Giá trị ngoài biên dưới: -1
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(validPlayer, validTeam, validMatch, -1, CardType.YELLOW);
        });
    }

    @Test
    void CardConstructor_GiaTriNgoaiBienTren_ThrowException() {
        // Giá trị ngoài biên trên: 151
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(validPlayer, validTeam, validMatch, 151, CardType.RED);
        });
    }
}