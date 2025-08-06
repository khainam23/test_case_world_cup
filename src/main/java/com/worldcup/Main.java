package com.worldcup;

import com.worldcup.automation.WorldCupAutomation;

public class Main {
    public static void main(String[] args) {
        WorldCupAutomation automation = new WorldCupAutomation();
        automation.runCompleteWorldCup();
        automation.close();
    }
}