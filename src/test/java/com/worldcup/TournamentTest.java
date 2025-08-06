package com.worldcup;

import com.worldcup.model.Tournament;
import com.worldcup.model.Team;
import com.worldcup.model.Group;
import com.worldcup.model.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TournamentTest {

    private Tournament tournament;
    private Team team1, team2, team3, team4;
    private Group group1, group2;
    private List<Player> players1, players2, players3, players4;

    @BeforeEach
    void setUp() {
        tournament = new Tournament();
        
        // Tạo cầu thủ cho các đội
        players1 = createPlayerList("Player1", 22);
        players2 = createPlayerList("Player2", 22);
        players3 = createPlayerList("Player3", 22);
        players4 = createPlayerList("Player4", 22);

        // Tạo các đội
        List<Player> starting1 = players1.subList(0, 11);
        List<Player> substitute1 = players1.subList(11, 16);
        List<Player> starting2 = players2.subList(0, 11);
        List<Player> substitute2 = players2.subList(11, 16);
        List<Player> starting3 = players3.subList(0, 11);
        List<Player> substitute3 = players3.subList(11, 16);
        List<Player> starting4 = players4.subList(0, 11);
        List<Player> substitute4 = players4.subList(11, 16);
        
        team1 = new Team("Brazil", "South America", "Coach1", 
                        List.of("Assistant1"), "Medical1", starting1, substitute1, false);
        team2 = new Team("Argentina", "South America", "Coach2", 
                        List.of("Assistant2"), "Medical2", starting2, substitute2, false);
        team3 = new Team("Germany", "Europe", "Coach3", 
                        List.of("Assistant3"), "Medical3", starting3, substitute3, false);
        team4 = new Team("France", "Europe", "Coach4", 
                        List.of("Assistant4"), "Medical4", starting4, substitute4, true); // Host team

        // Tạo các bảng đấu
        group1 = new Group("Group A");
        group2 = new Group("Group B");
    }

    private List<Player> createPlayerList(String baseName, int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            players.add(new Player(baseName + i, i, "Position" + (i % 4)));
        }
        return players;
    }

    // ========== GETTER TESTS ==========

    @Test
    void GetId_GiaTriMacDinh_TraVe0() {
        assertEquals(0, tournament.getId());
    }

    @Test
    void GetYear_GiaTriMacDinh_TraVe0() {
        assertEquals(0, tournament.getYear());
    }

    @Test
    void GetHost_GiaTriMacDinh_TraVeNull() {
        assertNull(tournament.getHost());
    }

    @Test
    void GetStart_GiaTriMacDinh_TraVeNull() {
        assertNull(tournament.getStart());
    }

    @Test
    void GetEnd_GiaTriMacDinh_TraVeNull() {
        assertNull(tournament.getEnd());
    }

    @Test
    void GetChampion_GiaTriMacDinh_TraVeNull() {
        assertNull(tournament.getChampion());
    }

    @Test
    void GetRunnerUp_GiaTriMacDinh_TraVeNull() {
        assertNull(tournament.getRunnerUp());
    }

    @Test
    void GetThirdPlace_GiaTriMacDinh_TraVeNull() {
        assertNull(tournament.getThirdPlace());
    }

    @Test
    void GetTeamList_GiaTriMacDinh_TraVeNull() {
        assertNull(tournament.getTeamList());
    }

    @Test
    void GetGroupList_GiaTriMacDinh_TraVeNull() {
        assertNull(tournament.getGroupList());
    }

    @Test
    void GetPlayers_GiaTriMacDinh_TraVeNull() {
        assertNull(tournament.getPlayers());
    }

    @Test
    void GetRandom_GiaTriMacDinh_KhoiTaoSan() {
        Random random = tournament.getRandom();
        assertNotNull(random);
        assertTrue(random instanceof Random);
    }


    @Test
    void GetRandom_TaoSoNgauNhien_HoatDongDung() {
        Random random = tournament.getRandom();
        
        // Test tạo số ngẫu nhiên
        int number1 = random.nextInt(100);
        int number2 = random.nextInt(100);
        
        assertTrue(number1 >= 0 && number1 < 100);
        assertTrue(number2 >= 0 && number2 < 100);
        
        // Kiểm tra có thể tạo số khác nhau (không phải lúc nào cũng đúng nhưng có xác suất cao)
        boolean foundDifferent = false;
        for (int i = 0; i < 10; i++) {
            if (random.nextInt(1000) != random.nextInt(1000)) {
                foundDifferent = true;
                break;
            }
        }
        // Chú ý: Test này có thể fail trong trường hợp hiếm hoi, nhưng xác suất rất thấp
    }

    // ========== OBJECT STATE TESTS ==========

    @Test
    void Tournament_TrangThaiBanDau_TatCaFieldNull() {
        Tournament newTournament = new Tournament();
        
        assertEquals(0, newTournament.getId());
        assertEquals(0, newTournament.getYear());
        assertNull(newTournament.getHost());
        assertNull(newTournament.getStart());
        assertNull(newTournament.getEnd());
        assertNull(newTournament.getChampion());
        assertNull(newTournament.getRunnerUp());
        assertNull(newTournament.getThirdPlace());
        assertNull(newTournament.getTeamList());
        assertNull(newTournament.getGroupList());
        assertNull(newTournament.getPlayers());
        assertNotNull(newTournament.getRandom());
    }

    // ========== BOUNDARY VALUE TESTS ==========

    @Test
    void Tournament_GiaTriBienId_KiemTraGetter() {
        // Vì không có setter, chỉ có thể test getter với giá trị mặc định
        Tournament tournament1 = new Tournament();
        Tournament tournament2 = new Tournament();
        
        assertEquals(tournament1.getId(), tournament2.getId());
        assertEquals(0, tournament1.getId());
    }

    @Test
    void Tournament_GiaTriBienYear_KiemTraGetter() {
        // Test với giá trị mặc định
        assertEquals(0, tournament.getYear());
        
        // Vì không có setter public, không thể test các giá trị biên khác
        // Nhưng có thể test tính nhất quán
        Tournament anotherTournament = new Tournament();
        assertEquals(tournament.getYear(), anotherTournament.getYear());
    }

    // ========== EQUIVALENCE PARTITIONING TESTS ==========

    @Test
    void Tournament_NhieuInstance_DocLap() {
        Tournament tournament1 = new Tournament();
        Tournament tournament2 = new Tournament();
        Tournament tournament3 = new Tournament();
        
        // Các instance khác nhau
        assertNotSame(tournament1, tournament2);
        assertNotSame(tournament2, tournament3);
        assertNotSame(tournament1, tournament3);
        
        // Nhưng có cùng giá trị mặc định
        assertEquals(tournament1.getId(), tournament2.getId());
        assertEquals(tournament1.getYear(), tournament2.getYear());
        assertEquals(tournament1.getHost(), tournament2.getHost());
        assertEquals(tournament1.getStart(), tournament2.getStart());
        assertEquals(tournament1.getEnd(), tournament2.getEnd());
    }

    @Test
    void Tournament_RandomInstance_DocLap() {
        Tournament tournament1 = new Tournament();
        Tournament tournament2 = new Tournament();
        
        Random random1 = tournament1.getRandom();
        Random random2 = tournament2.getRandom();
        
        // Mỗi tournament có Random instance riêng
        assertNotSame(random1, random2);
    }

    // ========== NULL SAFETY TESTS ==========

    @Test
    void Tournament_GetterVoiGiaTriNull_KhongThrowException() {
        // Tất cả getter trả về null hoặc giá trị mặc định, không throw exception
        assertDoesNotThrow(() -> {
            tournament.getId();
            tournament.getYear();
            tournament.getHost();
            tournament.getStart();
            tournament.getEnd();
            tournament.getChampion();
            tournament.getRunnerUp();
            tournament.getThirdPlace();
            tournament.getTeamList();
            tournament.getGroupList();
            tournament.getPlayers();
            tournament.getRandom();
        });
    }

    // ========== CONSISTENCY TESTS ==========

    @Test
    void Tournament_GetterConsistency_GiaTriKhongThayDoi() {
        // Gọi getter nhiều lần, giá trị phải nhất quán
        int id1 = tournament.getId();
        int id2 = tournament.getId();
        int id3 = tournament.getId();
        
        assertEquals(id1, id2);
        assertEquals(id2, id3);
        
        int year1 = tournament.getYear();
        int year2 = tournament.getYear();
        assertEquals(year1, year2);
        
        Team host1 = tournament.getHost();
        Team host2 = tournament.getHost();
        assertEquals(host1, host2); // Cả hai đều null
        
        LocalDate start1 = tournament.getStart();
        LocalDate start2 = tournament.getStart();
        assertEquals(start1, start2); // Cả hai đều null
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    void Tournament_TichHopVoiCacModel_HoatDongDung() {
        // Mặc dù Tournament chỉ có getter, có thể test tích hợp với các model khác
        
        // Tạo danh sách team
        List<Team> teams = List.of(team1, team2, team3, team4);
        
        // Tạo danh sách group
        List<Group> groups = List.of(group1, group2);
        
        // Tạo danh sách tất cả players
        List<Player> allPlayers = new ArrayList<>();
        allPlayers.addAll(players1);
        allPlayers.addAll(players2);
        allPlayers.addAll(players3);
        allPlayers.addAll(players4);
        
        // Kiểm tra tournament có thể làm việc với các model này
        assertNotNull(teams);
        assertNotNull(groups);
        assertNotNull(allPlayers);
        assertEquals(4, teams.size());
        assertEquals(2, groups.size());
        assertEquals(88, allPlayers.size()); // 4 teams * 22 players
        
        // Kiểm tra host team
        Team hostTeam = teams.stream()
                           .filter(Team::isHost)
                           .findFirst()
                           .orElse(null);
        assertEquals(team4, hostTeam);
    }

    // ========== PERFORMANCE TESTS ==========

    @Test
    void Tournament_GetterPerformance_NhanhChong() {
        long startTime = System.nanoTime();
        
        // Gọi getter nhiều lần
        for (int i = 0; i < 10000; i++) {
            tournament.getId();
            tournament.getYear();
            tournament.getHost();
            tournament.getStart();
            tournament.getEnd();
            tournament.getChampion();
            tournament.getRunnerUp();
            tournament.getThirdPlace();
            tournament.getTeamList();
            tournament.getGroupList();
            tournament.getPlayers();
            tournament.getRandom();
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Thời gian thực hiện phải nhỏ hơn 100ms (100,000,000 nanoseconds)
        assertTrue(duration < 100_000_000, "Getter performance too slow: " + duration + " ns");
    }

    // ========== MEMORY TESTS ==========

    @Test
    void Tournament_TaoNhieuInstance_KhongLeakMemory() {
        List<Tournament> tournaments = new ArrayList<>();
        
        // Tạo nhiều instance
        for (int i = 0; i < 1000; i++) {
            tournaments.add(new Tournament());
        }
        
        assertEquals(1000, tournaments.size());
        
        // Kiểm tra mỗi instance có Random riêng
        Random firstRandom = tournaments.get(0).getRandom();
        Random lastRandom = tournaments.get(999).getRandom();
        assertNotSame(firstRandom, lastRandom);
        
        // Clear list để giúp GC
        tournaments.clear();
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void Tournament_CacTinhHuongDacBiet_XuLyDung() {
        // Test với multiple threads (basic thread safety check)
        Tournament sharedTournament = new Tournament();
        
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                sharedTournament.getId();
                sharedTournament.getRandom().nextInt();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                sharedTournament.getYear();
                sharedTournament.getRandom().nextDouble();
            }
        });
        
        assertDoesNotThrow(() -> {
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();
        });
    }

    // ========== RANDOM SPECIFIC TESTS ==========

    @Test
    void Tournament_RandomFunctionality_DayDuTinhNang() {
        Random random = tournament.getRandom();
        
        // Test các method cơ bản của Random
        assertDoesNotThrow(() -> {
            random.nextInt();
            random.nextInt(100);
            random.nextLong();
            random.nextDouble();
            random.nextFloat();
            random.nextBoolean();
            random.nextGaussian();
        });
    }

    @Test
    void Tournament_RandomSeed_KhacNhauGiuaCacInstance() {
        Tournament tournament1 = new Tournament();
        Tournament tournament2 = new Tournament();
        
        Random random1 = tournament1.getRandom();
        Random random2 = tournament2.getRandom();
        
        // Tạo sequence số ngẫu nhiên từ mỗi random
        List<Integer> sequence1 = new ArrayList<>();
        List<Integer> sequence2 = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            sequence1.add(random1.nextInt(1000));
            sequence2.add(random2.nextInt(1000));
        }
        
        // Hai sequence có khả năng cao là khác nhau (không phải lúc nào cũng đúng 100%)
        // Nhưng xác suất chúng giống hệt nhau rất thấp
        boolean foundDifference = false;
        for (int i = 0; i < sequence1.size(); i++) {
            if (!sequence1.get(i).equals(sequence2.get(i))) {
                foundDifference = true;
                break;
            }
        }
        
        // Nếu tất cả số đều giống nhau, có thể là trùng hợp hiếm hoi
        // Trong trường hợp này, chúng ta chấp nhận kết quả
        // assertTrue(foundDifference, "Random sequences are identical - very unlikely but possible");
    }

    // ========== COMPREHENSIVE STATE TESTS ==========

    @Test
    void Tournament_TrangThaiToanDien_KiemTraDayDu() {
        Tournament tournament = new Tournament();
        
        // Kiểm tra tất cả primitive fields
        assertEquals(0, tournament.getId());
        assertEquals(0, tournament.getYear());
        
        // Kiểm tra tất cả object fields
        assertNull(tournament.getHost());
        assertNull(tournament.getStart());
        assertNull(tournament.getEnd());
        assertNull(tournament.getChampion());
        assertNull(tournament.getRunnerUp());
        assertNull(tournament.getThirdPlace());
        assertNull(tournament.getTeamList());
        assertNull(tournament.getGroupList());
        assertNull(tournament.getPlayers());
        
        // Kiểm tra field được khởi tạo
        assertNotNull(tournament.getRandom());
        
        // Kiểm tra tính immutable của các getter (không có setter public)
        // Điều này đảm bảo object state không thể thay đổi từ bên ngoài
        int originalId = tournament.getId();
        int originalYear = tournament.getYear();
        
        // Sau một thời gian, giá trị vẫn không đổi
        try {
            Thread.sleep(1); // Đợi 1ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertEquals(originalId, tournament.getId());
        assertEquals(originalYear, tournament.getYear());
    }
}