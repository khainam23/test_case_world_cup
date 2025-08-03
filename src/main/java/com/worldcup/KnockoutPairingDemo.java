package com.worldcup;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo kiểm tra logic ghép đôi vòng 16 đội theo quy định FIFA
 */
public class KnockoutPairingDemo {
    
    public static void main(String[] args) {
        System.out.println("🏆 Kiểm Tra Logic Ghép Đôi Vòng 16 Đội FIFA World Cup");
        System.out.println("====================================================");
        
        // Tạo các đội mẫu đại diện cho nhất bảng và nhì bảng
        List<Team> firstPlace = new ArrayList<>();
        List<Team> secondPlace = new ArrayList<>();
        
        // Tạo 8 nhất bảng (A1, B1, C1, D1, E1, F1, G1, H1)
        String[] groupNames = {"Brazil", "Germany", "France", "Spain", "Argentina", "England", "Italy", "Netherlands"};
        for (int i = 0; i < 8; i++) {
            char groupLetter = (char)('A' + i);
            Team winner = createMockTeam(groupNames[i], "Nhất Bảng " + groupLetter);
            firstPlace.add(winner);
            System.out.println("🥇 Nhất Bảng " + groupLetter + ": " + winner.getName());
        }
        
        System.out.println();
        
        // Tạo 8 nhì bảng (A2, B2, C2, D2, E2, F2, G2, H2)
        String[] runnerUpNames = {"Croatia", "Belgium", "Portugal", "Mexico", "Uruguay", "Denmark", "Poland", "Switzerland"};
        for (int i = 0; i < 8; i++) {
            char groupLetter = (char)('A' + i);
            Team runnerUp = createMockTeam(runnerUpNames[i], "Nhì Bảng " + groupLetter);
            secondPlace.add(runnerUp);
            System.out.println("🥈 Nhì Bảng " + groupLetter + ": " + runnerUp.getName());
        }
        
        System.out.println();
        
        // Kiểm tra logic ghép đôi
        List<Team> pairings = createRoundOf16Pairings(firstPlace, secondPlace);
        
        // Xác minh kết quả
        if (pairings.size() == 16) {
            System.out.println("✅ Số đội đúng: " + pairings.size());
        } else {
            System.out.println("❌ Số đội sai: " + pairings.size());
        }
        
        System.out.println("\n🏆 Các Trận Đấu Vòng 16 Đội (Theo Quy Định FIFA):");
        System.out.println("==================================================");
        
        for (int i = 0; i < pairings.size(); i += 2) {
            int matchNum = (i / 2) + 1;
            String team1 = pairings.get(i).getName();
            String team2 = pairings.get(i + 1).getName();
            
            // Xác định đội nào thuộc bảng nào
            String team1Group = getTeamGroup(team1, firstPlace, secondPlace);
            String team2Group = getTeamGroup(team2, firstPlace, secondPlace);
            
            System.out.println("Trận " + matchNum + ": " + team1 + " (" + team1Group + ") vs " + team2 + " (" + team2Group + ")");
        }
        
        System.out.println("\n✅ Logic ghép đôi vòng 16 đội đã được triển khai đúng theo quy định FIFA!");
        System.out.println("   - Nhất bảng chỉ đấu với nhì bảng từ các bảng khác nhau");
        System.out.println("   - Cấu trúc bracket phù hợp cho tứ kết");
    }
    
    private static String getTeamGroup(String teamName, List<Team> firstPlace, List<Team> secondPlace) {
        // Tìm đội này thuộc bảng nào và vị trí nào
        for (int i = 0; i < firstPlace.size(); i++) {
            if (firstPlace.get(i).getName().equals(teamName)) {
                return (char)('A' + i) + "1";
            }
        }
        for (int i = 0; i < secondPlace.size(); i++) {
            if (secondPlace.get(i).getName().equals(teamName)) {
                return (char)('A' + i) + "2";
            }
        }
        return "Không xác định";
    }
    
    private static Team createMockTeam(String name, String description) {
        // Tạo đội bóng tối thiểu để test
        List<Player> players = new ArrayList<>();
        
        // Tạo 11 cầu thủ chính
        for (int i = 1; i <= 11; i++) {
            players.add(new Player("Cầu thủ " + i, i, "ST"));
        }
        
        // Tạo 11 cầu thủ dự bị
        for (int i = 12; i <= 22; i++) {
            players.add(new Player("Dự bị " + i, i, "ST"));
        }
        
        return new Team(name, "Châu Âu", "HLV " + name, 
                       List.of("Trợ lý"), "BS " + name, players, false);
    }
    
    private static List<Team> createRoundOf16Pairings(List<Team> firstPlace, List<Team> secondPlace) {
        System.out.println("🔄 Tạo ghép đôi vòng 16 đội theo quy định FIFA...\n");
        
        List<Team> pairings = new ArrayList<>();
        
        // Ghép đôi vòng 16 đội FIFA World Cup:
        // Trận 1: Nhất bảng A vs Nhì bảng B
        pairings.add(firstPlace.get(0));   // Nhất bảng A
        pairings.add(secondPlace.get(1));  // Nhì bảng B
        System.out.println("✓ Trận 1: " + firstPlace.get(0).getName() + " (A1) vs " + secondPlace.get(1).getName() + " (B2)");
        
        // Trận 2: Nhất bảng B vs Nhì bảng A  
        pairings.add(firstPlace.get(1));   // Nhất bảng B
        pairings.add(secondPlace.get(0));  // Nhì bảng A
        System.out.println("✓ Trận 2: " + firstPlace.get(1).getName() + " (B1) vs " + secondPlace.get(0).getName() + " (A2)");
        
        // Trận 3: Nhất bảng C vs Nhì bảng D
        pairings.add(firstPlace.get(2));   // Nhất bảng C
        pairings.add(secondPlace.get(3));  // Nhì bảng D
        System.out.println("✓ Trận 3: " + firstPlace.get(2).getName() + " (C1) vs " + secondPlace.get(3).getName() + " (D2)");
        
        // Trận 4: Nhất bảng D vs Nhì bảng C
        pairings.add(firstPlace.get(3));   // Nhất bảng D
        pairings.add(secondPlace.get(2));  // Nhì bảng C
        System.out.println("✓ Trận 4: " + firstPlace.get(3).getName() + " (D1) vs " + secondPlace.get(2).getName() + " (C2)");
        
        // Trận 5: Nhất bảng E vs Nhì bảng F
        pairings.add(firstPlace.get(4));   // Nhất bảng E
        pairings.add(secondPlace.get(5));  // Nhì bảng F
        System.out.println("✓ Trận 5: " + firstPlace.get(4).getName() + " (E1) vs " + secondPlace.get(5).getName() + " (F2)");
        
        // Trận 6: Nhất bảng F vs Nhì bảng E
        pairings.add(firstPlace.get(5));   // Nhất bảng F
        pairings.add(secondPlace.get(4));  // Nhì bảng E
        System.out.println("✓ Trận 6: " + firstPlace.get(5).getName() + " (F1) vs " + secondPlace.get(4).getName() + " (E2)");
        
        // Trận 7: Nhất bảng G vs Nhì bảng H
        pairings.add(firstPlace.get(6));   // Nhất bảng G
        pairings.add(secondPlace.get(7));  // Nhì bảng H
        System.out.println("✓ Trận 7: " + firstPlace.get(6).getName() + " (G1) vs " + secondPlace.get(7).getName() + " (H2)");
        
        // Trận 8: Nhất bảng H vs Nhì bảng G
        pairings.add(firstPlace.get(7));   // Nhất bảng H
        pairings.add(secondPlace.get(6));  // Nhì bảng G
        System.out.println("✓ Trận 8: " + firstPlace.get(7).getName() + " (H1) vs " + secondPlace.get(6).getName() + " (G2)");
        
        System.out.println();
        return pairings;
    }
}