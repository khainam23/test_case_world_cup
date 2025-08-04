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

    private Team team1, team2, team3, team4;
    private Match match1, match2;
    private List<Player> players1, players2, players3, players4;
    private List<Player> startingPlayers, substitutePlayers;

    @BeforeEach
    void setUp() {
        // Tạo danh sách cầu thủ cho các đội
        players1 = createPlayerList("Team1Player", 22);
        players2 = createPlayerList("Team2Player", 22);
        players3 = createPlayerList("Team3Player", 22);
        players4 = createPlayerList("Team4Player", 22);

        // Tạo các đội
        team1 = new Team("Brazil", "South America", "Coach1", 
                        List.of("Assistant1"), "Medical1", players1, false);
        team2 = new Team("Argentina", "South America", "Coach2", 
                        List.of("Assistant2"), "Medical2", players2, false);
        team3 = new Team("Germany", "Europe", "Coach3", 
                        List.of("Assistant3"), "Medical3", players3, false);
        team4 = new Team("France", "Europe", "Coach4", 
                        List.of("Assistant4"), "Medical4", players4, false);

        // Tạo danh sách cầu thủ đá chính và dự bị
        startingPlayers = players1.subList(0, 11);
        substitutePlayers = players1.subList(11, 16);

        // Tạo các trận đấu
        match1 = new Match(team1, team2, startingPlayers, substitutePlayers,
                          players2.subList(0, 11), players2.subList(11, 16), false);
        match2 = new Match(team3, team4, players3.subList(0, 11), players3.subList(11, 16),
                          players4.subList(0, 11), players4.subList(11, 16), false);
    }

    private List<Player> createPlayerList(String baseName, int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            players.add(new Player(baseName + i, i, "Position" + (i % 4)));
        }
        return players;
    }

    // ========== CONSTRUCTOR TESTS ==========

    @Test
    void GroupConstructor_DayDuThamSo_KhoiTaoThanhCong() {
        List<Team> teams = List.of(team1, team2);
        List<Match> matches = List.of(match1);
        
        Group group = new Group(1, "Group A", teams, matches);
        
        assertEquals(1, group.getId());
        assertEquals("Group A", group.getName());
        assertEquals(2, group.getTeamList().size());
        assertEquals(1, group.getMatches().size());
        assertTrue(group.getTeamList().contains(team1));
        assertTrue(group.getTeamList().contains(team2));
        assertTrue(group.getMatches().contains(match1));
    }

    @Test
    void GroupConstructor_ChiIdVaTen_KhoiTaoThanhCong() {
        Group group = new Group(2, "Group B");
        
        assertEquals(2, group.getId());
        assertEquals("Group B", group.getName());
        assertNotNull(group.getTeamList());
        assertNotNull(group.getMatches());
        assertEquals(0, group.getTeamList().size());
        assertEquals(0, group.getMatches().size());
    }

    // Test phân hoạch giá trị biên cho ID
    @Test
    void GroupConstructor_IdBang0_KhoiTaoThanhCong() {
        Group group = new Group(0, "Group Zero");
        assertEquals(0, group.getId());
    }

    @Test
    void GroupConstructor_IdAm_KhoiTaoThanhCong() {
        Group group = new Group(-1, "Group Negative");
        assertEquals(-1, group.getId());
    }

    @Test
    void GroupConstructor_IdLon_KhoiTaoThanhCong() {
        Group group = new Group(999999, "Group Large");
        assertEquals(999999, group.getId());
    }

    // Test phân hoạch tương đương cho tên
    @Test
    void GroupConstructor_TenNull_KhoiTaoThanhCong() {
        Group group = new Group(3, null);
        assertNull(group.getName());
    }

    @Test
    void GroupConstructor_TenRong_KhoiTaoThanhCong() {
        Group group = new Group(4, "");
        assertEquals("", group.getName());
    }

    @Test
    void GroupConstructor_TenDai_KhoiTaoThanhCong() {
        String longName = "A".repeat(1000);
        Group group = new Group(5, longName);
        assertEquals(longName, group.getName());
    }

    @Test
    void GroupConstructor_TenCoKyTuDacBiet_KhoiTaoThanhCong() {
        Group group = new Group(6, "Group @#$%^&*()");
        assertEquals("Group @#$%^&*()", group.getName());
    }

    // Test với danh sách null
    @Test
    void GroupConstructor_TeamListNull_KhoiTaoThanhCong() {
        Group group = new Group(7, "Group C", null, List.of(match1));
        assertEquals("Group C", group.getName());
        assertNull(group.getTeamList());
    }

    @Test
    void GroupConstructor_MatchListNull_KhoiTaoThanhCong() {
        Group group = new Group(8, "Group D", List.of(team1), null);
        assertEquals("Group D", group.getName());
        assertNull(group.getMatches());
    }

    @Test
    void GroupConstructor_CaDanhSachNull_KhoiTaoThanhCong() {
        Group group = new Group(9, "Group E", null, null);
        assertEquals("Group E", group.getName());
        assertNull(group.getTeamList());
        assertNull(group.getMatches());
    }

    // ========== ADD TEAM TESTS ==========

    @Test
    void AddTeam_TeamHopLe_ThemThanhCong() {
        Group group = new Group(10, "Group F");
        group.addTeam(team1);
        
        assertEquals(1, group.getTeamList().size());
        assertTrue(group.getTeamList().contains(team1));
    }

    @Test
    void AddTeam_NhieuTeam_ThemThanhCong() {
        Group group = new Group(11, "Group G");
        group.addTeam(team1);
        group.addTeam(team2);
        group.addTeam(team3);
        
        assertEquals(3, group.getTeamList().size());
        assertTrue(group.getTeamList().contains(team1));
        assertTrue(group.getTeamList().contains(team2));
        assertTrue(group.getTeamList().contains(team3));
    }

    @Test
    void AddTeam_TeamNull_KhongThem() {
        Group group = new Group(12, "Group H");
        int sizeBefore = group.getTeamList().size();
        
        group.addTeam(null);
        
        assertEquals(sizeBefore, group.getTeamList().size());
    }

    @Test
    void AddTeam_TeamTrung_KhongThemLai() {
        Group group = new Group(13, "Group I");
        group.addTeam(team1);
        int sizeBefore = group.getTeamList().size();
        
        group.addTeam(team1); // Thêm lại team1
        
        assertEquals(sizeBefore, group.getTeamList().size());
        assertEquals(1, group.getTeamList().size());
    }

    // Test phân hoạch giá trị biên cho số lượng team
    @Test
    void AddTeam_4Team_ThemThanhCong() {
        Group group = new Group(14, "Group J");
        group.addTeam(team1);
        group.addTeam(team2);
        group.addTeam(team3);
        group.addTeam(team4);
        
        assertEquals(4, group.getTeamList().size());
    }

    @Test
    void AddTeam_QuaSoLuongChuan_VanThemDuoc() {
        Group group = new Group(15, "Group K");
        // Thêm nhiều hơn 4 đội (số lượng chuẩn của một bảng)
        group.addTeam(team1);
        group.addTeam(team2);
        group.addTeam(team3);
        group.addTeam(team4);
        
        // Tạo thêm team thứ 5
        List<Player> players5 = createPlayerList("Team5Player", 22);
        Team team5 = new Team("Spain", "Europe", "Coach5", 
                             List.of("Assistant5"), "Medical5", players5, false);
        group.addTeam(team5);
        
        assertEquals(5, group.getTeamList().size());
    }

    // ========== ADD MATCH TESTS ==========

    @Test
    void AddMatch_MatchHopLe_ThemThanhCong() {
        Group group = new Group(16, "Group L");
        group.addMatch(match1);
        
        assertEquals(1, group.getMatches().size());
        assertTrue(group.getMatches().contains(match1));
    }

    @Test
    void AddMatch_NhieuMatch_ThemThanhCong() {
        Group group = new Group(17, "Group M");
        group.addMatch(match1);
        group.addMatch(match2);
        
        assertEquals(2, group.getMatches().size());
        assertTrue(group.getMatches().contains(match1));
        assertTrue(group.getMatches().contains(match2));
    }

    @Test
    void AddMatch_MatchNull_KhongThem() {
        Group group = new Group(18, "Group N");
        int sizeBefore = group.getMatches().size();
        
        group.addMatch(null);
        
        assertEquals(sizeBefore, group.getMatches().size());
    }

    @Test
    void AddMatch_MatchTrung_KhongThemLai() {
        Group group = new Group(19, "Group O");
        group.addMatch(match1);
        int sizeBefore = group.getMatches().size();
        
        group.addMatch(match1); // Thêm lại match1
        
        assertEquals(sizeBefore, group.getMatches().size());
        assertEquals(1, group.getMatches().size());
    }

    // ========== SETTER TESTS ==========

    @Test
    void SetName_TenHopLe_SetThanhCong() {
        Group group = new Group(20, "Old Name");
        group.setName("New Name");
        assertEquals("New Name", group.getName());
    }

    @Test
    void SetName_TenNull_SetThanhCong() {
        Group group = new Group(21, "Old Name");
        group.setName(null);
        assertNull(group.getName());
    }

    @Test
    void SetName_TenRong_SetThanhCong() {
        Group group = new Group(22, "Old Name");
        group.setName("");
        assertEquals("", group.getName());
    }

    @Test
    void SetTeamList_DanhSachHopLe_SetThanhCong() {
        Group group = new Group(23, "Group P");
        List<Team> newTeams = List.of(team1, team2);
        
        group.setTeamList(newTeams);
        
        assertEquals(2, group.getTeamList().size());
        assertTrue(group.getTeamList().contains(team1));
        assertTrue(group.getTeamList().contains(team2));
    }

    @Test
    void SetTeamList_DanhSachNull_TaoMoi() {
        Group group = new Group(24, "Group Q");
        group.setTeamList(null);
        
        assertNotNull(group.getTeamList());
        assertEquals(0, group.getTeamList().size());
    }

    @Test
    void SetTeamList_DanhSachRong_SetThanhCong() {
        Group group = new Group(25, "Group R");
        group.setTeamList(new ArrayList<>());
        
        assertEquals(0, group.getTeamList().size());
    }

    @Test
    void SetMatches_DanhSachHopLe_SetThanhCong() {
        Group group = new Group(26, "Group S");
        List<Match> newMatches = List.of(match1, match2);
        
        group.setMatches(newMatches);
        
        assertEquals(2, group.getMatches().size());
        assertTrue(group.getMatches().contains(match1));
        assertTrue(group.getMatches().contains(match2));
    }

    @Test
    void SetMatches_DanhSachNull_TaoMoi() {
        Group group = new Group(27, "Group T");
        group.setMatches(null);
        
        assertNotNull(group.getMatches());
        assertEquals(0, group.getMatches().size());
    }

    @Test
    void SetMatches_DanhSachRong_SetThanhCong() {
        Group group = new Group(28, "Group U");
        group.setMatches(new ArrayList<>());
        
        assertEquals(0, group.getMatches().size());
    }

    // ========== GETTER TESTS ==========

    @Test
    void GetTeams_TraVeDanhSachTeam() {
        List<Team> teams = List.of(team1, team2);
        Group group = new Group(29, "Group V", teams, List.of(match1));
        
        List<Team> result = group.getTeams();
        assertEquals(teams, result);
    }

    @Test
    void GetTeamList_TraVeDanhSachTeam() {
        List<Team> teams = List.of(team1, team2);
        Group group = new Group(30, "Group W", teams, List.of(match1));
        
        List<Team> result = group.getTeamList();
        assertEquals(teams, result);
    }

    // ========== TOSTRING TESTS ==========

    @Test
    void ToString_GroupDayDu_ChuoiHopLe() {
        List<Team> teams = List.of(team1);
        List<Match> matches = List.of(match1);
        Group group = new Group(31, "Group X", teams, matches);
        
        String result = group.toString();
        
        assertTrue(result.contains("Group X"));
        assertTrue(result.contains("teamList"));
        assertTrue(result.contains("matches"));
    }

    @Test
    void ToString_GroupRong_ChuoiHopLe() {
        Group group = new Group(32, "Empty Group");
        
        String result = group.toString();
        
        assertTrue(result.contains("Empty Group"));
        assertTrue(result.contains("teamList"));
        assertTrue(result.contains("matches"));
    }

    @Test
    void ToString_TenNull_ChuoiHopLe() {
        Group group = new Group(33, null);
        
        String result = group.toString();
        
        assertTrue(result.contains("null"));
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    void Group_TinhNangTichHop_HoatDongDung() {
        Group group = new Group(34, "Integration Group");
        
        // Thêm teams
        group.addTeam(team1);
        group.addTeam(team2);
        
        // Thêm matches
        group.addMatch(match1);
        
        // Kiểm tra trạng thái
        assertEquals(2, group.getTeamList().size());
        assertEquals(1, group.getMatches().size());
        
        // Thay đổi tên
        group.setName("Updated Group");
        assertEquals("Updated Group", group.getName());
        
        // Thêm team trùng
        group.addTeam(team1);
        assertEquals(2, group.getTeamList().size()); // Không thay đổi
    }

    @Test
    void Group_BienGioiHan_XuLyDung() {
        Group group = new Group(Integer.MAX_VALUE, "Max Group");
        
        // Test với giá trị biên
        assertEquals(Integer.MAX_VALUE, group.getId());
        
        // Test với danh sách lớn
        for (int i = 0; i < 100; i++) {
            List<Player> players = createPlayerList("Player" + i, 22);
            Team team = new Team("Team" + i, "Region" + i, "Coach" + i,
                               List.of("Assistant" + i), "Medical" + i, players, false);
            group.addTeam(team);
        }
        
        assertEquals(100, group.getTeamList().size());
    }
}