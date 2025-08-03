package com.worldcup;

import com.worldcup.automation.WorldCupAutomation;

public class Main {
    public static void main(String[] args) {
        System.out.println("🏆 Khởi động FIFA World Cup Simulation...");
        
        try {
            WorldCupAutomation automation = new WorldCupAutomation();
            automation.runCompleteWorldCup();
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi chạy World Cup Automation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}