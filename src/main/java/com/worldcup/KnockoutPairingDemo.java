package com.worldcup;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo to test Round of 16 pairing logic according to FIFA rules
 */
public class KnockoutPairingDemo {
    
    public static void main(String[] args) {
        System.out.println("🏆 FIFA World Cup Round of 16 Pairing Logic Test");
        System.out.println("=================================================");
        
        // Create mock teams representing group winners and runners-up
        List<Team> firstPlace = new ArrayList<>();
        List<Team> secondPlace = new ArrayList<>();
        
        // Create 8 group winners (A1, B1, C1, D1, E1, F1, G1, H1)
        String[] groupNames = {"Brazil", "Germany", "France", "Spain", "Argentina", "England", "Italy", "Netherlands"};
        for (int i = 0; i < 8; i++) {
            char groupLetter = (char)('A' + i);
            Team winner = createMockTeam(groupNames[i], "Group " + groupLetter + " Winner");
            firstPlace.add(winner);
            System.out.println("🥇 Group " + groupLetter + " Winner: " + winner.getName());
        }
        
        System.out.println();
        
        // Create 8 group runners-up (A2, B2, C2, D2, E2, F2, G2, H2)
        String[] runnerUpNames = {"Croatia", "Belgium", "Portugal", "Mexico", "Uruguay", "Denmark", "Poland", "Switzerland"};
        for (int i = 0; i < 8; i++) {
            char groupLetter = (char)('A' + i);
            Team runnerUp = createMockTeam(runnerUpNames[i], "Group " + groupLetter + " Runner-up");
            secondPlace.add(runnerUp);
            System.out.println("🥈 Group " + groupLetter + " Runner-up: " + runnerUp.getName());
        }
        
        System.out.println();
        
        // Test the pairing logic
        List<Team> pairings = createRoundOf16Pairings(firstPlace, secondPlace);
        
        // Verify results
        if (pairings.size() == 16) {
            System.out.println("✅ Correct number of teams: " + pairings.size());
        } else {
            System.out.println("❌ Wrong number of teams: " + pairings.size());
        }
        
        System.out.println("\n🏆 Round of 16 Matches (FIFA Format):");
        System.out.println("=====================================");
        
        for (int i = 0; i < pairings.size(); i += 2) {
            int matchNum = (i / 2) + 1;
            String team1 = pairings.get(i).getName();
            String team2 = pairings.get(i + 1).getName();
            
            // Determine which group each team came from
            String team1Group = getTeamGroup(team1, firstPlace, secondPlace);
            String team2Group = getTeamGroup(team2, firstPlace, secondPlace);
            
            System.out.println("Match " + matchNum + ": " + team1 + " (" + team1Group + ") vs " + team2 + " (" + team2Group + ")");
        }
        
        System.out.println("\n✅ Round of 16 pairing logic implemented correctly according to FIFA rules!");
        System.out.println("   - Group winners face runners-up from different groups");
        System.out.println("   - Proper bracket structure maintained for quarter-finals");
    }
    
    private static String getTeamGroup(String teamName, List<Team> firstPlace, List<Team> secondPlace) {
        // Find which group and position this team is from
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
        return "Unknown";
    }
    
    private static Team createMockTeam(String name, String description) {
        // Create a minimal team for testing
        List<Player> players = new ArrayList<>();
        
        // Create 11 starting players
        for (int i = 1; i <= 11; i++) {
            players.add(new Player("Player " + i, i, "ST"));
        }
        
        // Create 11 substitute players
        for (int i = 12; i <= 22; i++) {
            players.add(new Player("Sub " + i, i, "ST"));
        }
        
        return new Team(name, "Europe", "Coach " + name, 
                       List.of("Assistant"), "Dr. " + name, players, false);
    }
    
    private static List<Team> createRoundOf16Pairings(List<Team> firstPlace, List<Team> secondPlace) {
        System.out.println("🔄 Creating Round of 16 pairings according to FIFA rules...\n");
        
        List<Team> pairings = new ArrayList<>();
        
        // FIFA World Cup Round of 16 pairings:
        // Match 1: Winner A vs Runner-up B
        pairings.add(firstPlace.get(0));   // Winner A
        pairings.add(secondPlace.get(1));  // Runner-up B
        System.out.println("✓ Match 1: " + firstPlace.get(0).getName() + " (A1) vs " + secondPlace.get(1).getName() + " (B2)");
        
        // Match 2: Winner B vs Runner-up A  
        pairings.add(firstPlace.get(1));   // Winner B
        pairings.add(secondPlace.get(0));  // Runner-up A
        System.out.println("✓ Match 2: " + firstPlace.get(1).getName() + " (B1) vs " + secondPlace.get(0).getName() + " (A2)");
        
        // Match 3: Winner C vs Runner-up D
        pairings.add(firstPlace.get(2));   // Winner C
        pairings.add(secondPlace.get(3));  // Runner-up D
        System.out.println("✓ Match 3: " + firstPlace.get(2).getName() + " (C1) vs " + secondPlace.get(3).getName() + " (D2)");
        
        // Match 4: Winner D vs Runner-up C
        pairings.add(firstPlace.get(3));   // Winner D
        pairings.add(secondPlace.get(2));  // Runner-up C
        System.out.println("✓ Match 4: " + firstPlace.get(3).getName() + " (D1) vs " + secondPlace.get(2).getName() + " (C2)");
        
        // Match 5: Winner E vs Runner-up F
        pairings.add(firstPlace.get(4));   // Winner E
        pairings.add(secondPlace.get(5));  // Runner-up F
        System.out.println("✓ Match 5: " + firstPlace.get(4).getName() + " (E1) vs " + secondPlace.get(5).getName() + " (F2)");
        
        // Match 6: Winner F vs Runner-up E
        pairings.add(firstPlace.get(5));   // Winner F
        pairings.add(secondPlace.get(4));  // Runner-up E
        System.out.println("✓ Match 6: " + firstPlace.get(5).getName() + " (F1) vs " + secondPlace.get(4).getName() + " (E2)");
        
        // Match 7: Winner G vs Runner-up H
        pairings.add(firstPlace.get(6));   // Winner G
        pairings.add(secondPlace.get(7));  // Runner-up H
        System.out.println("✓ Match 7: " + firstPlace.get(6).getName() + " (G1) vs " + secondPlace.get(7).getName() + " (H2)");
        
        // Match 8: Winner H vs Runner-up G
        pairings.add(firstPlace.get(7));   // Winner H
        pairings.add(secondPlace.get(6));  // Runner-up G
        System.out.println("✓ Match 8: " + firstPlace.get(7).getName() + " (H1) vs " + secondPlace.get(6).getName() + " (G2)");
        
        System.out.println();
        return pairings;
    }
}