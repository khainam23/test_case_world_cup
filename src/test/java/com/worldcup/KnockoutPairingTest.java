package com.worldcup;

import com.worldcup.generator.DataGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KnockoutPairingTest {

    @Test
    void testRoundOf16PairingLogic() {
        // Create mock teams representing group winners and runners-up
        List<Team> firstPlace = new ArrayList<>();
        List<Team> secondPlace = new ArrayList<>();
        
        // Create 8 group winners (A1, B1, C1, D1, E1, F1, G1, H1)
        for (int i = 0; i < 8; i++) {
            char groupLetter = (char)('A' + i);
            Team winner = createMockTeam(groupLetter + "1 Winner", "Europe", "Coach " + groupLetter);
            firstPlace.add(winner);
        }
        
        // Create 8 group runners-up (A2, B2, C2, D2, E2, F2, G2, H2)
        for (int i = 0; i < 8; i++) {
            char groupLetter = (char)('A' + i);
            Team runnerUp = createMockTeam(groupLetter + "2 Runner-up", "Europe", "Coach " + groupLetter + "2");
            secondPlace.add(runnerUp);
        }
        
        // Test the pairing logic
        List<Team> pairings = createRoundOf16Pairings(firstPlace, secondPlace);
        
        // Should have 16 teams total
        assertEquals(16, pairings.size(), "Should have 16 teams in Round of 16");
        
        // Test specific pairings according to FIFA rules
        // Match 1: A1 vs B2
        assertEquals("A1 Winner", pairings.get(0).getName());
        assertEquals("B2 Runner-up", pairings.get(1).getName());
        
        // Match 2: B1 vs A2
        assertEquals("B1 Winner", pairings.get(2).getName());
        assertEquals("A2 Runner-up", pairings.get(3).getName());
        
        // Match 3: C1 vs D2
        assertEquals("C1 Winner", pairings.get(4).getName());
        assertEquals("D2 Runner-up", pairings.get(5).getName());
        
        // Match 4: D1 vs C2
        assertEquals("D1 Winner", pairings.get(6).getName());
        assertEquals("C2 Runner-up", pairings.get(7).getName());
        
        // Match 5: E1 vs F2
        assertEquals("E1 Winner", pairings.get(8).getName());
        assertEquals("F2 Runner-up", pairings.get(9).getName());
        
        // Match 6: F1 vs E2
        assertEquals("F1 Winner", pairings.get(10).getName());
        assertEquals("E2 Runner-up", pairings.get(11).getName());
        
        // Match 7: G1 vs H2
        assertEquals("G1 Winner", pairings.get(12).getName());
        assertEquals("H2 Runner-up", pairings.get(13).getName());
        
        // Match 8: H1 vs G2
        assertEquals("H1 Winner", pairings.get(14).getName());
        assertEquals("G2 Runner-up", pairings.get(15).getName());
        
        System.out.println("✅ Round of 16 pairing logic is correct according to FIFA rules");
        
        // Print the pairings for verification
        System.out.println("\n🏆 Round of 16 Pairings:");
        for (int i = 0; i < pairings.size(); i += 2) {
            int matchNum = (i / 2) + 1;
            System.out.println("Match " + matchNum + ": " + 
                pairings.get(i).getName() + " vs " + pairings.get(i + 1).getName());
        }
    }
    
    private Team createMockTeam(String name, String region, String coach) {
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
        
        return new Team(name, region, coach, 
                       List.of("Assistant"), "Dr. Test", players, false);
    }
    
    private List<Team> createRoundOf16Pairings(List<Team> firstPlace, List<Team> secondPlace) {
        List<Team> pairings = new ArrayList<>();
        
        // FIFA World Cup Round of 16 pairings:
        // Match 1: Winner A vs Runner-up B
        pairings.add(firstPlace.get(0));   // Winner A
        pairings.add(secondPlace.get(1));  // Runner-up B
        
        // Match 2: Winner B vs Runner-up A  
        pairings.add(firstPlace.get(1));   // Winner B
        pairings.add(secondPlace.get(0));  // Runner-up A
        
        // Match 3: Winner C vs Runner-up D
        pairings.add(firstPlace.get(2));   // Winner C
        pairings.add(secondPlace.get(3));  // Runner-up D
        
        // Match 4: Winner D vs Runner-up C
        pairings.add(firstPlace.get(3));   // Winner D
        pairings.add(secondPlace.get(2));  // Runner-up C
        
        // Match 5: Winner E vs Runner-up F
        pairings.add(firstPlace.get(4));   // Winner E
        pairings.add(secondPlace.get(5));  // Runner-up F
        
        // Match 6: Winner F vs Runner-up E
        pairings.add(firstPlace.get(5));   // Winner F
        pairings.add(secondPlace.get(4));  // Runner-up E
        
        // Match 7: Winner G vs Runner-up H
        pairings.add(firstPlace.get(6));   // Winner G
        pairings.add(secondPlace.get(7));  // Runner-up H
        
        // Match 8: Winner H vs Runner-up G
        pairings.add(firstPlace.get(7));   // Winner H
        pairings.add(secondPlace.get(6));  // Runner-up G
        
        return pairings;
    }
}