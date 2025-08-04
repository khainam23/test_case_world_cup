package com.worldcup.util;

import com.worldcup.database.DatabaseManager;
import com.worldcup.service.TournamentService;
import java.sql.*;
import java.util.Map;
import java.util.Scanner;

/**
 * Utility class để cập nhật tournament winners
 * Cung cấp interface để cập nhật champion_id, runner_up_id, third_place_id
 */
public class TournamentWinnersUpdater {
    private DatabaseManager dbManager;
    private TournamentService tournamentService;
    
    public TournamentWinnersUpdater() {
        this.dbManager = new DatabaseManager();
        this.tournamentService = new TournamentService(dbManager);
    }
    
    /**
     * Hiển thị tất cả tournaments có sẵn
     */
    public void showAvailableTournaments() throws SQLException {
        String sql = "SELECT id, name, year, host_country, status FROM tournaments ORDER BY id";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        
        System.out.println("\n=== DANH SÁCH TOURNAMENTS ===");
        System.out.printf("%-5s %-30s %-6s %-20s %-15s%n", "ID", "Name", "Year", "Host Country", "Status");
        System.out.println("-".repeat(80));
        
        while (rs.next()) {
            System.out.printf("%-5d %-30s %-6d %-20s %-15s%n",
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("year"),
                rs.getString("host_country"),
                rs.getString("status")
            );
        }
        
        rs.close();
        pstmt.close();
    }
    
    /**
     * Hiển thị tất cả teams trong một tournament
     */
    public void showTeamsInTournament(int tournamentId) throws SQLException {
        String sql = "SELECT id, name, region FROM teams WHERE tournament_id = ? ORDER BY name";
        PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql);
        pstmt.setInt(1, tournamentId);
        ResultSet rs = pstmt.executeQuery();
        
        System.out.println("\n=== TEAMS TRONG TOURNAMENT ID: " + tournamentId + " ===");
        System.out.printf("%-5s %-30s %-20s%n", "ID", "Team Name", "Region");
        System.out.println("-".repeat(60));
        
        while (rs.next()) {
            System.out.printf("%-5d %-30s %-20s%n",
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("region")
            );
        }
        
        rs.close();
        pstmt.close();
    }
    
    /**
     * Hiển thị thông tin winners hiện tại
     */
    public void showCurrentWinners(int tournamentId) throws SQLException {
        Map<String, Object> winners = tournamentService.getTournamentWinners(tournamentId);
        
        System.out.println("\n=== THÔNG TIN WINNERS HIỆN TẠI ===");
        System.out.println("Tournament ID: " + tournamentId);
        
        Object championId = winners.get("championId");
        Object runnerUpId = winners.get("runnerUpId");
        Object thirdPlaceId = winners.get("thirdPlaceId");
        
        System.out.println("Champion: " + 
            (championId != null ? winners.get("championName") + " (ID: " + championId + ")" : "Chưa có"));
        System.out.println("Runner-up: " + 
            (runnerUpId != null ? winners.get("runnerUpName") + " (ID: " + runnerUpId + ")" : "Chưa có"));
        System.out.println("Third place: " + 
            (thirdPlaceId != null ? winners.get("thirdPlaceName") + " (ID: " + thirdPlaceId + ")" : "Chưa có"));
    }
    
    /**
     * Cập nhật tournament winners với input từ user
     */
    public void updateWinnersInteractive() {
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Hiển thị tournaments
            showAvailableTournaments();
            
            System.out.print("\nNhập Tournament ID để cập nhật: ");
            int tournamentId = scanner.nextInt();
            
            // Hiển thị teams trong tournament
            showTeamsInTournament(tournamentId);
            
            // Hiển thị winners hiện tại
            showCurrentWinners(tournamentId);
            
            System.out.println("\n=== CẬP NHẬT WINNERS ===");
            System.out.println("Nhập ID của team (hoặc 0 để bỏ qua):");
            
            System.out.print("Champion ID: ");
            int championInput = scanner.nextInt();
            Integer championId = championInput == 0 ? null : championInput;
            
            System.out.print("Runner-up ID: ");
            int runnerUpInput = scanner.nextInt();
            Integer runnerUpId = runnerUpInput == 0 ? null : runnerUpInput;
            
            System.out.print("Third place ID: ");
            int thirdPlaceInput = scanner.nextInt();
            Integer thirdPlaceId = thirdPlaceInput == 0 ? null : thirdPlaceInput;
            
            // Cập nhật
            tournamentService.updateTournamentWinners(tournamentId, championId, runnerUpId, thirdPlaceId);
            
            // Hiển thị kết quả
            System.out.println("\n=== KẾT QUẢ SAU KHI CẬP NHẬT ===");
            showCurrentWinners(tournamentId);
            
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Cập nhật winners bằng code (không cần input)
     */
    public void updateWinnersProgrammatically(int tournamentId, Integer championId, Integer runnerUpId, Integer thirdPlaceId) {
        try {
            System.out.println("\n=== CẬP NHẬT TOURNAMENT WINNERS ===");
            System.out.println("Tournament ID: " + tournamentId);
            
            // Hiển thị thông tin trước khi cập nhật
            showCurrentWinners(tournamentId);
            
            // Cập nhật
            tournamentService.updateTournamentWinners(tournamentId, championId, runnerUpId, thirdPlaceId);
            
            // Hiển thị kết quả sau khi cập nhật
            System.out.println("\n=== KẾT QUẢ SAU KHI CẬP NHẬT ===");
            showCurrentWinners(tournamentId);
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật tournament winners: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Main method để test
     */
    public static void main(String[] args) {
        TournamentWinnersUpdater updater = new TournamentWinnersUpdater();
        
        try {
            if (args.length == 0) {
                // Interactive mode
                updater.updateWinnersInteractive();
            } else if (args.length == 4) {
                // Programmatic mode: tournamentId championId runnerUpId thirdPlaceId
                int tournamentId = Integer.parseInt(args[0]);
                Integer championId = args[1].equals("null") ? null : Integer.parseInt(args[1]);
                Integer runnerUpId = args[2].equals("null") ? null : Integer.parseInt(args[2]);
                Integer thirdPlaceId = args[3].equals("null") ? null : Integer.parseInt(args[3]);
                
                updater.updateWinnersProgrammatically(tournamentId, championId, runnerUpId, thirdPlaceId);
            } else {
                System.out.println("Usage:");
                System.out.println("  Interactive mode: java TournamentWinnersUpdater");
                System.out.println("  Programmatic mode: java TournamentWinnersUpdater <tournamentId> <championId> <runnerUpId> <thirdPlaceId>");
                System.out.println("  Use 'null' for any ID you want to leave empty");
            }
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            updater.close();
        }
    }
    
    public void close() {
        if (dbManager != null) {
            dbManager.close();
        }
    }
}