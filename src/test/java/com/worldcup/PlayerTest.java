package com.worldcup;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    // ========== CONSTRUCTOR TESTS ==========
    
    // BR6: Khởi tạo cầu thủ cơ bản
    @Test
    void PlayerConstructor_ValidInputs_KhoiTaoThanhCong() {
        Player player = new Player("Messi", 10, "Forward", "Argentina");
        assertEquals("Messi", player.getName());
        assertEquals(10, player.getNumber());
        assertEquals("Forward", player.getPosition());
        assertEquals("Argentina", player.getTeamName());
        assertEquals(0, player.getGoals());
        assertEquals(0, player.getYellowCards());
        assertFalse(player.hasRedCard());
        assertFalse(player.isSentOff());
        assertTrue(player.isEligible());
    }

    // Test cases cho tên cầu thủ - Phân hoạch tương đương
    @Test
    void PlayerConstructor_TenRong_KhoiTaoThanhCong() {
        Player player = new Player("", 1, "Forward", "Brazil");
        assertEquals("", player.getName());
    }

    @Test
    void PlayerConstructor_TenNull_KhoiTaoThanhCong() {
        Player player = new Player(null, 2, "Midfielder", "Argentina");
        assertNull(player.getName());
    }

    @Test
    void PlayerConstructor_TenDai_KhoiTaoThanhCong() {
        String longName = "Abcdefghijklmnopqrstuvwxyz Abcdefghijklmnopqrstuvwxyz";
        Player player = new Player(longName, 3, "Defender", "Spain");
        assertEquals(longName, player.getName());
    }

    @Test
    void PlayerConstructor_TenCoKyTuDacBiet_KhoiTaoThanhCong() {
        Player player = new Player("José María Ñoño", 4, "Goalkeeper", "Mexico");
        assertEquals("José María Ñoño", player.getName());
    }

    // Test cases cho số áo - Phân hoạch giá trị biên
    @Test
    void PlayerConstructor_SoAoBang0_KhoiTaoThanhCong() {
        Player player = new Player("Zero", 0, "Midfield", "Vietnam");
        assertEquals(0, player.getNumber());
    }

    @Test
    void PlayerConstructor_SoAoBang1_KhoiTaoThanhCong() {
        Player player = new Player("One", 1, "Forward", "Thailand");
        assertEquals(1, player.getNumber());
    }

    @Test
    void PlayerConstructor_SoAoBang99_KhoiTaoThanhCong() {
        Player player = new Player("MaxNumber", 99, "Forward", "Germany");
        assertEquals(99, player.getNumber());
    }

    @Test
    void PlayerConstructor_SoAoAm1_KhoiTaoThanhCong() {
        Player player = new Player("NegativeMin", -1, "Defender", "Italy");
        assertEquals(-1, player.getNumber());
    }

    @Test
    void PlayerConstructor_SoAoAm5_KhoiTaoThanhCong() {
        Player player = new Player("Negative", -5, "Defender", "Brazil");
        assertEquals(-5, player.getNumber());
    }

    @Test
    void PlayerConstructor_SoAoLon999_KhoiTaoThanhCong() {
        Player player = new Player("BigNumber", 999, "Midfielder", "France");
        assertEquals(999, player.getNumber());
    }

    // Test cases cho vị trí - Phân hoạch tương đương
    @Test
    void PlayerConstructor_ViTriNull_KhoiTaoThanhCong() {
        Player player = new Player("Unknown", 7, null, "Germany");
        assertNull(player.getPosition());
    }

    @Test
    void PlayerConstructor_ViTriRong_KhoiTaoThanhCong() {
        Player player = new Player("NoPosition", 5, "", "England");
        assertEquals("", player.getPosition());
    }

    @Test
    void PlayerConstructor_ViTriKhongChuan_KhoiTaoThanhCong() {
        Player player = new Player("Sweeper", 6, "Libero", "Netherlands");
        assertEquals("Libero", player.getPosition());
    }

    @Test
    void PlayerConstructor_ViTriChuHoa_KhoiTaoThanhCong() {
        Player player = new Player("MixedCase", 7, "GoAlKeEpEr", "Belgium");
        assertEquals("GoAlKeEpEr", player.getPosition());
    }

    // Test cases cho tên đội - Phân hoạch tương đương
    @Test
    void PlayerConstructor_TenDoiNull_KhoiTaoThanhCong() {
        Player player = new Player("Teamless", 8, "Forward", null);
        assertNull(player.getTeamName());
    }

    @Test
    void PlayerConstructor_TenDoiRong_KhoiTaoThanhCong() {
        Player player = new Player("NoTeam", 9, "Midfielder", "");
        assertEquals("", player.getTeamName());
    }

    @Test
    void PlayerConstructor_TenDoiDai_KhoiTaoThanhCong() {
        String longTeam = "Very Long Team Name That Exceeds Normal Length";
        Player player = new Player("LongTeam", 10, "Defender", longTeam);
        assertEquals(longTeam, player.getTeamName());
    }

    // ========== SCORE GOAL TESTS ==========
    
    // BR11: Ghi bàn
    @Test
    void ScoreGoal_LanDau_Tang1Ban() {
        Player player = new Player("Ronaldo", 7, "Forward", "Portugal");
        player.scoreGoal();
        assertEquals(1, player.getGoals());
    }

    @Test
    void ScoreGoal_5Lan_Tang5Ban() {
        Player player = new Player("Mbappe", 10, "Forward", "France");
        for (int i = 0; i < 5; i++) {
            player.scoreGoal();
        }
        assertEquals(5, player.getGoals());
    }

    @Test
    void ScoreGoal_100Lan_Tang100Ban() {
        Player player = new Player("GoalMachine", 12, "Forward", "Poland");
        for (int i = 0; i < 100; i++) {
            player.scoreGoal();
        }
        assertEquals(100, player.getGoals());
    }

    @Test
    void ScoreGoal_SauKhiNhanThe_VanTangBan() {
        Player player = new Player("CardedScorer", 13, "Forward", "Sweden");
        player.scoreGoal();
        player.receiveYellowCard();
        player.scoreGoal();
        player.receiveRedCard();
        player.scoreGoal();
        assertEquals(3, player.getGoals());
        assertTrue(player.isSentOff());
    }

    // ========== YELLOW CARD TESTS ==========
    
    // BR8: Thẻ vàng - Phân hoạch giá trị biên
    @Test
    void ReceiveYellowCard_1The_Tang1TheVang() {
        Player player = new Player("Defender", 3, "Defender", "Italy");
        player.receiveYellowCard();
        assertEquals(1, player.getYellowCards());
        assertFalse(player.isSentOff());
        assertTrue(player.isEligible());
    }

    @Test
    void ReceiveYellowCard_2The_BiDuoiKhoiSan() {
        Player player = new Player("Midfielder", 6, "Midfield", "Spain");
        player.receiveYellowCard();
        player.receiveYellowCard();
        assertEquals(2, player.getYellowCards());
        assertTrue(player.isSentOff());
        assertFalse(player.isEligible());
    }

    @Test
    void ReceiveYellowCard_3The_VanBiDuoiKhoiSan() {
        Player player = new Player("Tough Guy", 8, "Defender", "Uruguay");
        player.receiveYellowCard();
        player.receiveYellowCard();
        player.receiveYellowCard();
        assertEquals(3, player.getYellowCards());
        assertTrue(player.isSentOff());
        assertFalse(player.isEligible());
    }

    @Test
    void ReceiveYellowCard_4The_VanBiDuoiKhoiSan() {
        Player player = new Player("YellowCollector", 14, "Midfielder", "Norway");
        for (int i = 0; i < 4; i++) {
            player.receiveYellowCard();
        }
        assertEquals(4, player.getYellowCards());
        assertTrue(player.isSentOff());
        assertFalse(player.isEligible());
    }

    @Test
    void ReceiveYellowCard_10The_VanBiDuoiKhoiSan() {
        Player player = new Player("YellowKing", 15, "Defender", "Switzerland");
        for (int i = 0; i < 10; i++) {
            player.receiveYellowCard();
        }
        assertEquals(10, player.getYellowCards());
        assertTrue(player.isSentOff());
        assertFalse(player.isEligible());
    }

    // ========== RED CARD TESTS ==========
    
    @Test
    void ReceiveRedCard_1The_BiDuoiKhoiSan() {
        Player player = new Player("Hothead", 11, "Forward", "Chile");
        player.receiveRedCard();
        assertTrue(player.hasRedCard());
        assertTrue(player.isSentOff());
        assertFalse(player.isEligible());
    }

    @Test
    void ReceiveRedCard_NhieuThe_VanBiDuoiKhoiSan() {
        Player player = new Player("RedCollector", 16, "Forward", "Austria");
        player.receiveRedCard();
        player.receiveRedCard(); // Thẻ đỏ thứ 2 (không thay đổi trạng thái)
        assertTrue(player.hasRedCard());
        assertTrue(player.isSentOff());
        assertFalse(player.isEligible());
    }

    // ========== MIXED CARD TESTS ==========
    
    @Test
    void ReceiveCards_VangTruocDoSau_BiDuoiKhoiSan() {
        Player player = new Player("Mixed", 2, "Defender", "England");
        player.receiveYellowCard();
        player.receiveRedCard();
        assertEquals(1, player.getYellowCards());
        assertTrue(player.hasRedCard());
        assertTrue(player.isSentOff());
        assertFalse(player.isEligible());
    }

    @Test
    void ReceiveCards_DoTruocVangSau_BiDuoiKhoiSan() {
        Player player = new Player("Reverse", 5, "Midfield", "Germany");
        player.receiveRedCard();
        player.receiveYellowCard();
        assertEquals(1, player.getYellowCards());
        assertTrue(player.hasRedCard());
        assertTrue(player.isSentOff());
        assertFalse(player.isEligible());
    }

    @Test
    void ReceiveCards_NhieuVangTruocDo_BiDuoiKhoiSan() {
        Player player = new Player("MixedCards", 17, "Goalkeeper", "Portugal");
        for (int i = 0; i < 5; i++) {
            player.receiveYellowCard();
        }
        player.receiveRedCard();
        assertEquals(5, player.getYellowCards());
        assertTrue(player.hasRedCard());
        assertTrue(player.isSentOff());
        assertFalse(player.isEligible());
    }

    // ========== ELIGIBILITY TESTS ==========
    
    @Test
    void IsEligible_KhongCoThe_DuDieuKien() {
        Player player = new Player("Clean", 4, "Goalkeeper", "Denmark");
        assertTrue(player.isEligible());
        assertFalse(player.isSentOff());
    }

    @Test
    void IsEligible_1TheVang_DuDieuKien() {
        Player player = new Player("OneYellow", 22, "Defender", "Ukraine");
        player.receiveYellowCard();
        assertTrue(player.isEligible());
        assertFalse(player.isSentOff());
    }

    @Test
    void IsEligible_2TheVang_KhongDuDieuKien() {
        Player player = new Player("TwoYellows", 23, "Forward", "Turkey");
        player.receiveYellowCard();
        assertTrue(player.isEligible());
        player.receiveYellowCard();
        assertFalse(player.isEligible());
        assertTrue(player.isSentOff());
    }

    @Test
    void IsEligible_1TheDo_KhongDuDieuKien() {
        Player player = new Player("RedCard", 24, "Midfielder", "Greece");
        player.receiveRedCard();
        assertFalse(player.isEligible());
        assertTrue(player.isSentOff());
    }

    @Test
    void IsEligible_CoGolVaThe_DuDieuKienNeuChua2Vang() {
        Player player = new Player("GoalsAndCards", 25, "Midfielder", "Greece");
        player.scoreGoal();
        player.receiveYellowCard();
        player.scoreGoal();
        assertTrue(player.isEligible());
        assertEquals(2, player.getGoals());
        assertEquals(1, player.getYellowCards());
    }

    // ========== RESET CARDS TESTS ==========
    
    @Test
    void ResetCards_KhongCoThe_GiuNguyenTrangThai() {
        Player player = new Player("CleanPlayer", 18, "Midfielder", "Denmark");
        player.resetCards();
        assertEquals(0, player.getYellowCards());
        assertFalse(player.hasRedCard());
        assertTrue(player.isEligible());
        assertFalse(player.isSentOff());
    }

    @Test
    void ResetCards_Chi1TheVang_ResetThanhCong() {
        Player player = new Player("YellowOnly", 19, "Defender", "Finland");
        player.receiveYellowCard();
        assertFalse(player.isSentOff());
        player.resetCards();
        assertEquals(0, player.getYellowCards());
        assertTrue(player.isEligible());
        assertFalse(player.isSentOff());
    }

    @Test
    void ResetCards_2TheVang_ResetThanhCong() {
        Player player = new Player("YellowBoy", 13, "Defender", "Korea");
        player.receiveYellowCard();
        player.receiveYellowCard();
        assertTrue(player.isSentOff());
        player.resetCards();
        assertEquals(0, player.getYellowCards());
        assertFalse(player.isSentOff());
        assertTrue(player.isEligible());
    }

    @Test
    void ResetCards_Chi1TheDo_ResetThanhCong() {
        Player player = new Player("RedOnly", 20, "Forward", "Iceland");
        player.receiveRedCard();
        assertTrue(player.isSentOff());
        player.resetCards();
        assertFalse(player.hasRedCard());
        assertTrue(player.isEligible());
        assertFalse(player.isSentOff());
    }

    @Test
    void ResetCards_VangVaDo_ResetThanhCong() {
        Player player = new Player("Resetter", 12, "Midfield", "Japan");
        player.receiveYellowCard();
        player.receiveRedCard();
        player.resetCards();
        assertEquals(0, player.getYellowCards());
        assertFalse(player.hasRedCard());
        assertFalse(player.isSentOff());
        assertTrue(player.isEligible());
    }

    @Test
    void ResetCards_NhieuLan_ResetThanhCong() {
        Player player = new Player("MultipleResetter", 21, "Midfielder", "Russia");
        player.receiveYellowCard();
        player.receiveRedCard();
        player.resetCards();
        player.receiveYellowCard();
        player.receiveYellowCard();
        player.resetCards();
        assertEquals(0, player.getYellowCards());
        assertFalse(player.hasRedCard());
        assertTrue(player.isEligible());
        assertFalse(player.isSentOff());
    }

    // ========== INTEGRATION TESTS ==========
    
    @Test
    void PlayerState_TinhNhatQuan_DuyTriTrangThaiDung() {
        Player player = new Player("Consistent", 25, "Goalkeeper", "Serbia");
        
        // Kiểm tra trạng thái ban đầu
        assertEquals(0, player.getGoals());
        assertEquals(0, player.getYellowCards());
        assertFalse(player.hasRedCard());
        assertFalse(player.isSentOff());
        assertTrue(player.isEligible());
        
        // Thay đổi trạng thái và kiểm tra tính nhất quán
        player.scoreGoal();
        player.receiveYellowCard();
        
        assertEquals(1, player.getGoals());
        assertEquals(1, player.getYellowCards());
        assertFalse(player.hasRedCard());
        assertFalse(player.isSentOff());
        assertTrue(player.isEligible());
    }

    @Test
    void PlayerAttributes_KhongThayDoi_SauCacHanhDong() {
        Player player = new Player("Immutable", 27, "Winger", "Slovenia");
        
        String originalName = player.getName();
        int originalNumber = player.getNumber();
        String originalPosition = player.getPosition();
        String originalTeam = player.getTeamName();
        
        // Thực hiện các hành động
        player.scoreGoal();
        player.receiveYellowCard();
        player.receiveRedCard();
        player.resetCards();
        
        // Kiểm tra các thuộc tính không thay đổi
        assertEquals(originalName, player.getName());
        assertEquals(originalNumber, player.getNumber());
        assertEquals(originalPosition, player.getPosition());
        assertEquals(originalTeam, player.getTeamName());
    }

    @Test
    void PlayerMethods_TatCaGetter_HoatDongDung() {
        Player player = new Player("Complete", 26, "Striker", "Montenegro");
        
        // Test tất cả getter methods
        assertNotNull(player.getName());
        assertTrue(player.getNumber() >= 0 || player.getNumber() < 0); // Any number
        assertNotNull(player.getPosition());
        assertNotNull(player.getTeamName());
        assertTrue(player.getGoals() >= 0);
        assertTrue(player.getYellowCards() >= 0);
        assertNotNull(Boolean.valueOf(player.hasRedCard()));
        assertNotNull(Boolean.valueOf(player.isSentOff()));
        assertNotNull(Boolean.valueOf(player.isEligible()));
    }

    // ========== BOUNDARY VALUE TESTS ==========
    
    @Test
    void IsSentOff_BienGiaTri_1TheVang_ChuaBiDuoi() {
        Player player = new Player("Boundary1", 30, "Forward", "TestTeam");
        player.receiveYellowCard();
        assertFalse(player.isSentOff());
        assertTrue(player.isEligible());
    }

    @Test
    void IsSentOff_BienGiaTri_2TheVang_BiDuoi() {
        Player player = new Player("Boundary2", 31, "Forward", "TestTeam");
        player.receiveYellowCard();
        player.receiveYellowCard();
        assertTrue(player.isSentOff());
        assertFalse(player.isEligible());
    }

    @Test
    void Goals_BienGiaTri_0Ban_KhoiTaoDung() {
        Player player = new Player("ZeroGoals", 32, "Forward", "TestTeam");
        assertEquals(0, player.getGoals());
    }

    @Test
    void Goals_BienGiaTri_1Ban_TangDung() {
        Player player = new Player("OneGoal", 33, "Forward", "TestTeam");
        player.scoreGoal();
        assertEquals(1, player.getGoals());
    }
}
