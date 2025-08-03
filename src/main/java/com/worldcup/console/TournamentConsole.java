package com.worldcup.console;

import com.worldcup.automation.WorldCupAutomation;
import com.worldcup.database.DatabaseManager;
import com.worldcup.database.TournamentQueries;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TournamentConsole {
    private DatabaseManager dbManager;
    private TournamentQueries queries;
    private Scanner scanner;

    public TournamentConsole() {
        this.dbManager = new DatabaseManager();
        this.queries = new TournamentQueries(dbManager);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("🏆 FIFA World Cup Tournament System 🏆");
        System.out.println("=====================================");

        while (true) {
            showMainMenu();
            int choice = getIntInput();

            try {
                switch (choice) {
                    case 1:
                        runNewTournament();
                        break;
                    case 2:
                        showTournamentSummary();
                        break;
                    case 3:
                        showAllTeams();
                        break;
                    case 4:
                        showGroupStandings();
                        break;
                    case 5:
                        showAllMatches();
                        break;
                    case 6:
                        showTopScorers();
                        break;
                    case 7:
                        showKnockoutBracket();
                        break;
                    case 8:
                        showTeamDetails();
                        break;
                    case 9:
                        showMatchDetails();
                        break;
                    case 0:
                        System.out.println("👋 Goodbye!");
                        return;
                    default:
                        System.out.println("❌ Invalid choice. Please try again.");
                }
            } catch (SQLException e) {
                System.err.println("❌ Database error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
            }

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private void showMainMenu() {
        System.out.println("\n📋 MAIN MENU:");
        System.out.println("1. 🚀 Run New Tournament");
        System.out.println("2. 📊 Show Tournament Summary");
        System.out.println("3. 🌍 Show All Teams");
        System.out.println("4. 🔤 Show Group Standings");
        System.out.println("5. ⚽ Show All Matches");
        System.out.println("6. 🥇 Show Top Scorers");
        System.out.println("7. 🏆 Show Knockout Bracket");
        System.out.println("8. 👥 Show Team Details");
        System.out.println("9. 🔍 Show Match Details");
        System.out.println("0. 🚪 Exit");
        System.out.print("\nEnter your choice: ");
    }

    private int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void runNewTournament() {
        System.out.println("\n🚀 Starting new World Cup tournament...");
        System.out.println("This will clear all existing data. Continue? (y/N): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            WorldCupAutomation automation = new WorldCupAutomation();
            try {
                automation.runCompleteWorldCup();
                System.out.println("\n✅ Tournament completed successfully!");
            } finally {
                automation.close();
            }
        } else {
            System.out.println("❌ Tournament cancelled.");
        }
    }

    private void showTournamentSummary() throws SQLException {
        System.out.println("\n📊 TOURNAMENT SUMMARY");
        System.out.println("====================");

        Map<String, Object> summary = queries.getTournamentSummary();
        
        if (summary.isEmpty()) {
            System.out.println("❌ No tournament data found. Please run a tournament first.");
            return;
        }

        System.out.println("🏆 Tournament: " + summary.get("tournamentName"));
        System.out.println("📅 Year: " + summary.get("year"));
        System.out.println("🏠 Host Country: " + summary.get("hostCountry"));
        System.out.println("📊 Status: " + summary.get("status"));
        System.out.println();
        
        if (summary.get("championName") != null) {
            System.out.println("🥇 Champion: " + summary.get("championName"));
            System.out.println("🥈 Runner-up: " + summary.get("runnerUpName"));
            System.out.println("🥉 Third Place: " + summary.get("thirdPlaceName"));
            System.out.println();
        }
        
        System.out.println("📈 Statistics:");
        System.out.println("   Total Matches: " + summary.get("totalMatches"));
        System.out.println("   Total Goals: " + summary.get("totalGoals"));
        System.out.println("   Total Substitutions: " + summary.get("totalSubstitutions"));
        
        if (summary.get("topScorerName") != null) {
            System.out.println("   Top Scorer: " + summary.get("topScorerName") + 
                             " (" + summary.get("topScorerGoals") + " goals)");
        }
    }

    private void showAllTeams() throws SQLException {
        System.out.println("\n🌍 ALL TEAMS");
        System.out.println("============");

        List<Map<String, Object>> teams = queries.getAllTeamsWithStats();
        
        if (teams.isEmpty()) {
            System.out.println("❌ No teams found. Please run a tournament first.");
            return;
        }

        System.out.printf("%-20s %-15s %-20s %-8s %s%n", 
                         "TEAM", "GROUP", "COACH", "POINTS", "RECORD (W-D-L)");
        System.out.println("-".repeat(80));

        for (Map<String, Object> team : teams) {
            String hostIndicator = (Boolean) team.get("isHost") ? " 🏠" : "";
            System.out.printf("%-20s %-15s %-20s %-8d %d-%d-%d%s%n",
                             team.get("name"),
                             team.get("groupName") != null ? "Group " + team.get("groupName") : "N/A",
                             team.get("coach"),
                             team.get("points"),
                             team.get("wins"),
                             team.get("draws"),
                             team.get("losses"),
                             hostIndicator);
        }
    }

    private void showGroupStandings() throws SQLException {
        System.out.println("\n🔤 GROUP STANDINGS");
        System.out.println("==================");

        for (char group = 'A'; group <= 'H'; group++) {
            System.out.println("\nGroup " + group + ":");
            System.out.println("-".repeat(70));
            System.out.printf("%-3s %-15s %-6s %-10s %-8s %-8s%n", 
                             "POS", "TEAM", "POINTS", "RECORD", "GOALS", "DIFF");
            System.out.println("-".repeat(70));

            List<Map<String, Object>> standings = queries.getGroupStandings(String.valueOf(group));
            
            int position = 1;
            for (Map<String, Object> team : standings) {
                String qualifier = position <= 2 ? " ✅" : "";
                System.out.printf("%-3d %-15s %-6d %d-%d-%d    %d:%d    %+d%s%n",
                                 position,
                                 team.get("name"),
                                 team.get("points"),
                                 team.get("wins"),
                                 team.get("draws"),
                                 team.get("losses"),
                                 team.get("goalsFor"),
                                 team.get("goalsAgainst"),
                                 team.get("goalDifference"),
                                 qualifier);
                position++;
            }
        }
    }

    private void showAllMatches() throws SQLException {
        System.out.println("\n⚽ ALL MATCHES");
        System.out.println("==============");

        List<Map<String, Object>> matches = queries.getAllMatches();
        
        if (matches.isEmpty()) {
            System.out.println("❌ No matches found. Please run a tournament first.");
            return;
        }

        String currentType = "";
        for (Map<String, Object> match : matches) {
            String matchType = (String) match.get("matchType");
            
            if (!matchType.equals(currentType)) {
                currentType = matchType;
                System.out.println("\n" + getMatchTypeDisplay(matchType) + ":");
                System.out.println("-".repeat(60));
            }

            System.out.printf("%-15s %d - %d %-15s",
                             match.get("teamAName"),
                             match.get("teamAScore"),
                             match.get("teamBScore"),
                             match.get("teamBName"));
            
            if (match.get("winnerName") != null) {
                System.out.print(" (Winner: " + match.get("winnerName") + ")");
            }
            
            if (match.get("groupName") != null) {
                System.out.print(" [Group " + match.get("groupName") + "]");
            }
            
            System.out.println();
        }
    }

    private String getMatchTypeDisplay(String matchType) {
        switch (matchType) {
            case "GROUP": return "🔤 Group Stage";
            case "ROUND_16": return "🏆 Round of 16";
            case "QUARTER": return "🏆 Quarter Finals";
            case "SEMI": return "🏆 Semi Finals";
            case "THIRD_PLACE": return "🥉 Third Place Match";
            case "FINAL": return "🏆 FINAL";
            default: return matchType;
        }
    }

    private void showTopScorers() throws SQLException {
        System.out.println("\n🥇 TOP SCORERS");
        System.out.println("==============");

        List<Map<String, Object>> scorers = queries.getTopScorers(10);
        
        if (scorers.isEmpty()) {
            System.out.println("❌ No goals scored yet. Please run a tournament first.");
            return;
        }

        System.out.printf("%-3s %-20s %-15s %-8s %-6s%n", 
                         "POS", "PLAYER", "TEAM", "POSITION", "GOALS");
        System.out.println("-".repeat(60));

        int position = 1;
        for (Map<String, Object> scorer : scorers) {
            System.out.printf("%-3d %-20s %-15s %-8s %-6d%n",
                             position,
                             scorer.get("playerName"),
                             scorer.get("teamName"),
                             scorer.get("position"),
                             scorer.get("goals"));
            position++;
        }
    }

    private void showKnockoutBracket() throws SQLException {
        System.out.println("\n🏆 KNOCKOUT BRACKET");
        System.out.println("===================");

        List<Map<String, Object>> knockoutMatches = queries.getKnockoutMatches();
        
        if (knockoutMatches.isEmpty()) {
            System.out.println("❌ No knockout matches found. Tournament may still be in group stage.");
            return;
        }

        String currentRound = "";
        for (Map<String, Object> match : knockoutMatches) {
            String matchType = (String) match.get("matchType");
            
            if (!matchType.equals(currentRound)) {
                currentRound = matchType;
                System.out.println("\n" + getMatchTypeDisplay(matchType) + ":");
                System.out.println("-".repeat(50));
            }

            System.out.printf("%-15s %d - %d %-15s → %s%n",
                             match.get("teamAName"),
                             match.get("teamAScore"),
                             match.get("teamBScore"),
                             match.get("teamBName"),
                             match.get("winnerName"));
        }
    }

    private void showTeamDetails() throws SQLException {
        System.out.print("\n👥 Enter team name: ");
        String teamName = scanner.nextLine().trim();
        
        if (teamName.isEmpty()) {
            System.out.println("❌ Team name cannot be empty.");
            return;
        }

        System.out.println("\n👥 TEAM DETAILS: " + teamName);
        System.out.println("=".repeat(30 + teamName.length()));

        List<Map<String, Object>> roster = queries.getTeamRoster(teamName);
        
        if (roster.isEmpty()) {
            System.out.println("❌ Team not found: " + teamName);
            return;
        }

        System.out.println("\n📋 SQUAD:");
        System.out.printf("%-3s %-20s %-8s %-8s %-6s %-6s%n", 
                         "NO.", "PLAYER", "POSITION", "STATUS", "GOALS", "CARDS");
        System.out.println("-".repeat(60));

        for (Map<String, Object> player : roster) {
            String status = (Boolean) player.get("isStarting") ? "Starting" : "Sub";
            String cards = player.get("yellowCards") + "Y/" + player.get("redCards") + "R";
            
            System.out.printf("%-3d %-20s %-8s %-8s %-6d %-6s%n",
                             player.get("jerseyNumber"),
                             player.get("name"),
                             player.get("position"),
                             status,
                             player.get("goals"),
                             cards);
        }
    }

    private void showMatchDetails() throws SQLException {
        System.out.print("\n🔍 Enter match ID: ");
        int matchId = getIntInput();
        
        if (matchId <= 0) {
            System.out.println("❌ Invalid match ID.");
            return;
        }

        Map<String, Object> matchDetails = queries.getMatchDetails(matchId);
        
        if (matchDetails.isEmpty()) {
            System.out.println("❌ Match not found with ID: " + matchId);
            return;
        }

        System.out.println("\n🔍 MATCH DETAILS");
        System.out.println("================");
        System.out.println("🏟️  Venue: " + matchDetails.get("venue"));
        System.out.println("👨‍⚖️  Referee: " + matchDetails.get("referee"));
        System.out.println("📅 Date: " + matchDetails.get("matchDate"));
        System.out.println("🏆 Type: " + getMatchTypeDisplay((String) matchDetails.get("matchType")));
        System.out.println();
        
        System.out.printf("%-15s %d - %d %-15s%n",
                         matchDetails.get("teamAName"),
                         matchDetails.get("teamAScore"),
                         matchDetails.get("teamBScore"),
                         matchDetails.get("teamBName"));
        
        if (matchDetails.get("winnerName") != null) {
            System.out.println("🏆 Winner: " + matchDetails.get("winnerName"));
        }

        // Show goals
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> goals = (List<Map<String, Object>>) matchDetails.get("goals");
        if (!goals.isEmpty()) {
            System.out.println("\n⚽ GOALS:");
            for (Map<String, Object> goal : goals) {
                System.out.printf("  %d' %s (%s)%n",
                                 goal.get("minute"),
                                 goal.get("playerName"),
                                 goal.get("teamName"));
            }
        }

        // Show cards
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cards = (List<Map<String, Object>>) matchDetails.get("cards");
        if (!cards.isEmpty()) {
            System.out.println("\n🟨🟥 CARDS:");
            for (Map<String, Object> card : cards) {
                String cardEmoji = card.get("cardType").equals("YELLOW") ? "🟨" : "🟥";
                System.out.printf("  %d' %s %s (%s)%n",
                                 card.get("minute"),
                                 cardEmoji,
                                 card.get("playerName"),
                                 card.get("teamName"));
            }
        }

        // Show substitutions
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> substitutions = (List<Map<String, Object>>) matchDetails.get("substitutions");
        if (!substitutions.isEmpty()) {
            System.out.println("\n🔄 SUBSTITUTIONS:");
            for (Map<String, Object> sub : substitutions) {
                System.out.printf("  %d' %s → %s (%s)%n",
                                 sub.get("minute"),
                                 sub.get("playerOutName"),
                                 sub.get("playerInName"),
                                 sub.get("teamName"));
            }
        }
    }

    public void close() {
        if (dbManager != null) {
            dbManager.close();
        }
        if (scanner != null) {
            scanner.close();
        }
    }

    public static void main(String[] args) {
        TournamentConsole console = new TournamentConsole();
        try {
            console.start();
        } finally {
            console.close();
        }
    }
}