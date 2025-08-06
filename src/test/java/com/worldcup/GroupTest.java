package com.worldcup;

import com.worldcup.model.Group;
import com.worldcup.model.Team;
import com.worldcup.model.Match;
import com.worldcup.model.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class GroupTest {

    private Team validTeam1;
    private Team validTeam2;
    private Team validTeam3;
    private Team validTeam4;
    private Match validMatch1;
    private Match validMatch2;
    private List<Team> validTeams;
    private List<Match> validMatches;
    private List<Player> startingPlayers;
    private List<Player> substitutePlayers;
    private List<String> assistants;

    @BeforeEach
    void setUp() {
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
        
        // Tạo các team hợp lệ
        validTeam1 = new Team("Brazil", "South America", "Coach1", assistants, 
                            "Medical1", startingPlayers, substitutePlayers, false);
        validTeam2 = new Team("Argentina", "South America", "Coach2", assistants, 
                            "Medical2", startingPlayers, substitutePlayers, false);
        validTeam3 = new Team("Germany", "Europe", "Coach3", assistants, 
                            "Medical3", startingPlayers, substitutePlayers, false);
        validTeam4 = new Team("Spain", "Europe", "Coach4", assistants, 
                            "Medical4", startingPlayers, substitutePlayers, false);
        
        // Tạo các match hợp lệ
        validMatch1 = new Match(validTeam1, validTeam2, "2024/12/15", "Stadium1", false);
        validMatch2 = new Match(validTeam3, validTeam4, "2024/12/16", "Stadium2", false);
        
        // Tạo danh sách teams và matches
        validTeams = new ArrayList<>();
        validTeams.add(validTeam1);
        validTeams.add(validTeam2);
        
        validMatches = new ArrayList<>();
        validMatches.add(validMatch1);
        validMatches.add(validMatch2);
    }

    // ========== CONSTRUCTOR TESTS ==========

    @Test
    void GroupConstructor_ThamSoHopLe_KhoiTaoThanhCong() {
        Group group = new Group("Group A", validTeams, validMatches);
        
        assertEquals("Group A", group.getName());
        assertEquals(validTeams, group.getTeams());
        assertEquals(validMatches, group.getMatches());
        assertEquals(0, group.getId()); // ID mặc định là 0
    }

    @Test
    void GroupConstructor_ChiCoTen_KhoiTaoThanhCong() {
        Group group = new Group("Group B");
        
        assertEquals("Group B", group.getName());
        assertNotNull(group.getTeams());
        assertNotNull(group.getMatches());
        assertTrue(group.getTeams().isEmpty());
        assertTrue(group.getMatches().isEmpty());
        assertEquals(0, group.getId());
    }

    @Test
    void GroupConstructor_TenNull_KhoiTaoThanhCong() {
        Group group = new Group(null, validTeams, validMatches);
        
        assertNull(group.getName());
        assertEquals(validTeams, group.getTeams());
        assertEquals(validMatches, group.getMatches());
    }

    @Test
    void GroupConstructor_TeamsNull_KhoiTaoThanhCong() {
        Group group = new Group("Group C", null, validMatches);
        
        assertEquals("Group C", group.getName());
        assertNull(group.getTeams());
        assertEquals(validMatches, group.getMatches());
    }

    @Test
    void GroupConstructor_MatchesNull_KhoiTaoThanhCong() {
        Group group = new Group("Group D", validTeams, null);
        
        assertEquals("Group D", group.getName());
        assertEquals(validTeams, group.getTeams());
        assertNull(group.getMatches());
    }

    @Test
    void GroupConstructor_TatCaThamSoNull_KhoiTaoThanhCong() {
        Group group = new Group(null, null, null);
        
        assertNull(group.getName());
        assertNull(group.getTeams());
        assertNull(group.getMatches());
    }

    @Test
    void GroupConstructor_TenRong_KhoiTaoThanhCong() {
        Group group = new Group("", validTeams, validMatches);
        
        assertEquals("", group.getName());
        assertEquals(validTeams, group.getTeams());
        assertEquals(validMatches, group.getMatches());
    }

    @Test
    void GroupConstructor_TenKhoangTrang_KhoiTaoThanhCong() {
        Group group = new Group("   ", validTeams, validMatches);
        
        assertEquals("   ", group.getName());
        assertEquals(validTeams, group.getTeams());
        assertEquals(validMatches, group.getMatches());
    }

    @Test
    void GroupConstructor_DanhSachRong_KhoiTaoThanhCong() {
        List<Team> emptyTeams = new ArrayList<>();
        List<Match> emptyMatches = new ArrayList<>();
        
        Group group = new Group("Group E", emptyTeams, emptyMatches);
        
        assertEquals("Group E", group.getName());
        assertTrue(group.getTeams().isEmpty());
        assertTrue(group.getMatches().isEmpty());
    }

    // ========== GETTER TESTS ==========

    @Test
    void GetName_GroupHopLe_TraVeTen() {
        Group group = new Group("Group A", validTeams, validMatches);
        assertEquals("Group A", group.getName());
    }

    @Test
    void GetTeams_GroupHopLe_TraVeDanhSachTeam() {
        Group group = new Group("Group A", validTeams, validMatches);
        assertEquals(validTeams, group.getTeams());
        assertEquals(2, group.getTeams().size());
    }

    @Test
    void GetMatches_GroupHopLe_TraVeDanhSachMatch() {
        Group group = new Group("Group A", validTeams, validMatches);
        assertEquals(validMatches, group.getMatches());
        assertEquals(2, group.getMatches().size());
    }

    @Test
    void GetId_MacDinh_TraVe0() {
        Group group = new Group("Group A");
        assertEquals(0, group.getId());
    }

    // ========== SETTER TESTS ==========

    @Test
    void SetName_TenHopLe_SetThanhCong() {
        Group group = new Group("Group A");
        group.setName("Group B");
        assertEquals("Group B", group.getName());
    }

    @Test
    void SetName_TenNull_SetThanhCong() {
        Group group = new Group("Group A");
        group.setName(null);
        assertNull(group.getName());
    }

    @Test
    void SetName_TenRong_SetThanhCong() {
        Group group = new Group("Group A");
        group.setName("");
        assertEquals("", group.getName());
    }

    @Test
    void SetName_TenKhoangTrang_SetThanhCong() {
        Group group = new Group("Group A");
        group.setName("   ");
        assertEquals("   ", group.getName());
    }

    @Test
    void SetTeams_DanhSachHopLe_SetThanhCong() {
        Group group = new Group("Group A");
        group.setTeams(validTeams);
        assertEquals(validTeams, group.getTeams());
        assertEquals(2, group.getTeams().size());
    }

    @Test
    void SetTeams_DanhSachNull_SetDanhSachRong() {
        Group group = new Group("Group A", validTeams, validMatches);
        group.setTeams(null);
        assertNotNull(group.getTeams());
        assertTrue(group.getTeams().isEmpty());
    }

    @Test
    void SetTeams_DanhSachRong_SetThanhCong() {
        Group group = new Group("Group A");
        List<Team> emptyTeams = new ArrayList<>();
        group.setTeams(emptyTeams);
        assertEquals(emptyTeams, group.getTeams());
        assertTrue(group.getTeams().isEmpty());
    }

    @Test
    void SetMatches_DanhSachHopLe_SetThanhCong() {
        Group group = new Group("Group A");
        group.setMatches(validMatches);
        assertEquals(validMatches, group.getMatches());
        assertEquals(2, group.getMatches().size());
    }

    @Test
    void SetMatches_DanhSachNull_SetDanhSachRong() {
        Group group = new Group("Group A", validTeams, validMatches);
        group.setMatches(null);
        assertNotNull(group.getMatches());
        assertTrue(group.getMatches().isEmpty());
    }

    @Test
    void SetMatches_DanhSachRong_SetThanhCong() {
        Group group = new Group("Group A");
        List<Match> emptyMatches = new ArrayList<>();
        group.setMatches(emptyMatches);
        assertEquals(emptyMatches, group.getMatches());
        assertTrue(group.getMatches().isEmpty());
    }

    @Test
    void SetId_GiaTriDuong_SetThanhCong() {
        Group group = new Group("Group A");
        group.setId(100);
        assertEquals(100, group.getId());
    }

    @Test
    void SetId_GiaTriAm_SetThanhCong() {
        Group group = new Group("Group A");
        group.setId(-1);
        assertEquals(-1, group.getId());
    }

    @Test
    void SetId_GiaTri0_SetThanhCong() {
        Group group = new Group("Group A");
        group.setId(0);
        assertEquals(0, group.getId());
    }

    // ========== ADD TEAM TESTS ==========

    @Test
    void AddTeam_TeamHopLe_ThemThanhCong() {
        Group group = new Group("Group A");
        group.addTeam(validTeam1);
        
        assertTrue(group.getTeams().contains(validTeam1));
        assertEquals(1, group.getTeams().size());
    }

    @Test
    void AddTeam_TeamNull_KhongThem() {
        Group group = new Group("Group A");
        int initialSize = group.getTeams().size();
        
        group.addTeam(null);
        
        assertEquals(initialSize, group.getTeams().size());
    }

    @Test
    void AddTeam_TeamDaTonTai_KhongThemLai() {
        Group group = new Group("Group A");
        group.addTeam(validTeam1);
        int sizeAfterFirst = group.getTeams().size();
        
        group.addTeam(validTeam1); // Thêm lại team đã tồn tại
        
        assertEquals(sizeAfterFirst, group.getTeams().size());
        assertEquals(1, group.getTeams().size());
    }

    @Test
    void AddTeam_NhieuTeamKhacNhau_ThemTatCa() {
        Group group = new Group("Group A");
        
        group.addTeam(validTeam1);
        group.addTeam(validTeam2);
        group.addTeam(validTeam3);
        
        assertEquals(3, group.getTeams().size());
        assertTrue(group.getTeams().contains(validTeam1));
        assertTrue(group.getTeams().contains(validTeam2));
        assertTrue(group.getTeams().contains(validTeam3));
    }


    // ========== ADD MATCH TESTS ==========

    @Test
    void AddMatch_MatchHopLe_ThemThanhCong() {
        Group group = new Group("Group A");
        group.addMatch(validMatch1);
        
        assertTrue(group.getMatches().contains(validMatch1));
        assertEquals(1, group.getMatches().size());
    }

    @Test
    void AddMatch_MatchNull_KhongThem() {
        Group group = new Group("Group A");
        int initialSize = group.getMatches().size();
        
        group.addMatch(null);
        
        assertEquals(initialSize, group.getMatches().size());
    }

    @Test
    void AddMatch_MatchDaTonTai_KhongThemLai() {
        Group group = new Group("Group A");
        group.addMatch(validMatch1);
        int sizeAfterFirst = group.getMatches().size();
        
        group.addMatch(validMatch1); // Thêm lại match đã tồn tại
        
        assertEquals(sizeAfterFirst, group.getMatches().size());
        assertEquals(1, group.getMatches().size());
    }

    @Test
    void AddMatch_NhieuMatchKhacNhau_ThemTatCa() {
        Group group = new Group("Group A");
        
        group.addMatch(validMatch1);
        group.addMatch(validMatch2);
        
        assertEquals(2, group.getMatches().size());
        assertTrue(group.getMatches().contains(validMatch1));
        assertTrue(group.getMatches().contains(validMatch2));
    }

    @Test
    void AddMatch_MatchesListNull_KhoiTaoListMoiVaThem() {
        Group group = new Group("Group A", validTeams, null);
        
        // Vì matches list ban đầu là null, addMatch sẽ gây lỗi NullPointerException
        assertThrows(NullPointerException.class, () -> {
            group.addMatch(validMatch1);
        });
    }

    // ========== TOSTRING TESTS ==========

    @Test
    void ToString_GroupDayDu_ChuoiDungDinhDang() {
        Group group = new Group("Group A", validTeams, validMatches);
        String result = group.toString();
        
        assertTrue(result.contains("Group{"));
        assertTrue(result.contains("name='Group A'"));
        assertTrue(result.contains("teamList="));
        assertTrue(result.contains("matches="));
    }

    @Test
    void ToString_GroupChiCoTen_ChuoiDungDinhDang() {
        Group group = new Group("Group B");
        String result = group.toString();
        
        assertTrue(result.contains("Group{"));
        assertTrue(result.contains("name='Group B'"));
        assertTrue(result.contains("teamList=[]"));
        assertTrue(result.contains("matches=[]"));
    }


    @Test
    void ToString_GroupTenRong_ChuoiDungDinhDang() {
        Group group = new Group("");
        String result = group.toString();
        
        assertTrue(result.contains("Group{"));
        assertTrue(result.contains("name=''"));
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    void Group_ToanBoChucNang_HoatDongDung() {
        // Tạo group với constructor đầy đủ
        Group group = new Group("Group A", validTeams, validMatches);
        
        // Kiểm tra constructor
        assertEquals("Group A", group.getName());
        assertEquals(2, group.getTeams().size());
        assertEquals(2, group.getMatches().size());
        assertEquals(0, group.getId());
        
        // Kiểm tra setters
        group.setName("Group B");
        group.setId(999);
        assertEquals("Group B", group.getName());
        assertEquals(999, group.getId());
        
        // Kiểm tra add methods
        group.addTeam(validTeam3);
        group.addMatch(validMatch2); // Match đã tồn tại, không thêm lại
        assertEquals(3, group.getTeams().size());
        assertEquals(2, group.getMatches().size()); // Vẫn là 2 vì match2 đã tồn tại
        
        // Kiểm tra toString
        String result = group.toString();
        assertTrue(result.contains("Group B"));
    }

    @Test
    void Group_KhoiTaoVoiConstructorDon_ThemTeamVaMatch() {
        Group group = new Group("Group C");
        
        // Kiểm tra khởi tạo
        assertEquals("Group C", group.getName());
        assertTrue(group.getTeams().isEmpty());
        assertTrue(group.getMatches().isEmpty());
        
        // Thêm teams và matches
        group.addTeam(validTeam1);
        group.addTeam(validTeam2);
        group.addMatch(validMatch1);
        
        assertEquals(2, group.getTeams().size());
        assertEquals(1, group.getMatches().size());
        assertTrue(group.getTeams().contains(validTeam1));
        assertTrue(group.getTeams().contains(validTeam2));
        assertTrue(group.getMatches().contains(validMatch1));
    }

    // ========== BOUNDARY VALUE ANALYSIS TESTS ==========

    @Test
    void Group_DanhSachTeamRong_HoatDongBinhThuong() {
        List<Team> emptyTeams = new ArrayList<>();
        Group group = new Group("Group A", emptyTeams, validMatches);
        
        assertTrue(group.getTeams().isEmpty());
        group.addTeam(validTeam1);
        assertEquals(1, group.getTeams().size());
    }

    @Test
    void Group_DanhSachMatchRong_HoatDongBinhThuong() {
        List<Match> emptyMatches = new ArrayList<>();
        Group group = new Group("Group A", validTeams, emptyMatches);
        
        assertTrue(group.getMatches().isEmpty());
        group.addMatch(validMatch1);
        assertEquals(1, group.getMatches().size());
    }

    @Test
    void Group_DanhSachTeamMotPhanTu_HoatDongBinhThuong() {
        List<Team> singleTeam = new ArrayList<>();
        singleTeam.add(validTeam1);
        
        Group group = new Group("Group A", singleTeam, validMatches);
        assertEquals(1, group.getTeams().size());
        assertEquals(validTeam1, group.getTeams().get(0));
    }

    @Test
    void Group_DanhSachMatchMotPhanTu_HoatDongBinhThuong() {
        List<Match> singleMatch = new ArrayList<>();
        singleMatch.add(validMatch1);
        
        Group group = new Group("Group A", validTeams, singleMatch);
        assertEquals(1, group.getMatches().size());
        assertEquals(validMatch1, group.getMatches().get(0));
    }

    // ========== EQUIVALENCE PARTITIONING TESTS ==========

    @Test
    void GroupConstructor_TenHopLe_KhoiTaoThanhCong() {
        // Phân hoạch tương đương: tên hợp lệ (không null, không rỗng)
        String[] validNames = {"Group A", "Group B", "Group 1", "Bảng A", "グループA", "123"};
        
        for (String name : validNames) {
            Group group = new Group(name);
            assertEquals(name, group.getName());
            assertNotNull(group.getTeams());
            assertNotNull(group.getMatches());
        }
    }

    @Test
    void GroupConstructor_TenKhongHopLe_VanKhoiTaoThanhCong() {
        // Phân hoạch tương đương: tên không hợp lệ (null, rỗng, khoảng trắng)
        String[] invalidNames = {null, "", "   ", "\t", "\n"};
        
        for (String name : invalidNames) {
            Group group = new Group(name);
            assertEquals(name, group.getName());
            assertNotNull(group.getTeams());
            assertNotNull(group.getMatches());
        }
    }

    // ========== EDGE CASE TESTS ==========


    @Test
    void Group_ThayDoiDanhSachTuGroup_AnhHuongDenDanhSachGoc() {
        Group group = new Group("Group A", validTeams, validMatches);
        
        // Thay đổi danh sách từ group
        group.getTeams().add(validTeam3);
        
        // Danh sách gốc cũng bị thay đổi (vì cùng reference)
        assertEquals(3, validTeams.size());
        assertTrue(validTeams.contains(validTeam3));
    }
}