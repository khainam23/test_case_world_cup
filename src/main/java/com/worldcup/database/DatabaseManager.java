package com.worldcup.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:worldcup.db";
    private Connection connection;

    public DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initializeTables();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void initializeTables() throws SQLException {
        // Teams table
        String createTeamsTable = """
            CREATE TABLE IF NOT EXISTS teams (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                region TEXT NOT NULL,
                coach TEXT NOT NULL,
                medical_staff TEXT NOT NULL,
                is_host BOOLEAN DEFAULT FALSE,
                points INTEGER DEFAULT 0,
                goal_difference INTEGER DEFAULT 0,
                goals_for INTEGER DEFAULT 0,
                goals_against INTEGER DEFAULT 0,
                wins INTEGER DEFAULT 0,
                draws INTEGER DEFAULT 0,
                losses INTEGER DEFAULT 0,
                yellow_cards INTEGER DEFAULT 0,
                red_cards INTEGER DEFAULT 0,
                substitution_count INTEGER DEFAULT 0,
                group_id INTEGER,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        // Players table
        String createPlayersTable = """
            CREATE TABLE IF NOT EXISTS players (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                jersey_number INTEGER NOT NULL,
                position TEXT NOT NULL,
                team_id INTEGER NOT NULL,
                is_starting BOOLEAN DEFAULT FALSE,
                yellow_cards INTEGER DEFAULT 0,
                red_cards INTEGER DEFAULT 0,
                goals INTEGER DEFAULT 0,
                assists INTEGER DEFAULT 0,
                minutes_played INTEGER DEFAULT 0,
                is_eligible BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (team_id) REFERENCES teams(id),
                UNIQUE(team_id, jersey_number)
            )
        """;

        // Assistant coaches table
        String createAssistantCoachesTable = """
            CREATE TABLE IF NOT EXISTS assistant_coaches (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                team_id INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (team_id) REFERENCES teams(id)
            )
        """;

        // Groups table
        String createGroupsTable = """
            CREATE TABLE IF NOT EXISTS groups (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        // Matches table
        String createMatchesTable = """
            CREATE TABLE IF NOT EXISTS matches (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                team_a_id INTEGER NOT NULL,
                team_b_id INTEGER NOT NULL,
                team_a_score INTEGER DEFAULT 0,
                team_b_score INTEGER DEFAULT 0,
                match_type TEXT NOT NULL, -- 'GROUP', 'ROUND_16', 'QUARTER', 'SEMI', 'FINAL', 'THIRD_PLACE'
                match_date TIMESTAMP,
                venue TEXT,
                referee TEXT,
                status TEXT DEFAULT 'SCHEDULED', -- 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED'
                winner_id INTEGER,
                group_id INTEGER,
                round_number INTEGER,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (team_a_id) REFERENCES teams(id),
                FOREIGN KEY (team_b_id) REFERENCES teams(id),
                FOREIGN KEY (winner_id) REFERENCES teams(id),
                FOREIGN KEY (group_id) REFERENCES groups(id)
            )
        """;

        // Goals table
        String createGoalsTable = """
            CREATE TABLE IF NOT EXISTS goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                match_id INTEGER NOT NULL,
                player_id INTEGER NOT NULL,
                team_id INTEGER NOT NULL,
                minute INTEGER NOT NULL,
                goal_type TEXT DEFAULT 'REGULAR', -- 'REGULAR', 'PENALTY', 'OWN_GOAL', 'FREE_KICK'
                assist_player_id INTEGER,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (match_id) REFERENCES matches(id),
                FOREIGN KEY (player_id) REFERENCES players(id),
                FOREIGN KEY (team_id) REFERENCES teams(id),
                FOREIGN KEY (assist_player_id) REFERENCES players(id)
            )
        """;

        // Substitutions table
        String createSubstitutionsTable = """
            CREATE TABLE IF NOT EXISTS substitutions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                match_id INTEGER NOT NULL,
                team_id INTEGER NOT NULL,
                player_in_id INTEGER NOT NULL,
                player_out_id INTEGER NOT NULL,
                minute INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (match_id) REFERENCES matches(id),
                FOREIGN KEY (team_id) REFERENCES teams(id),
                FOREIGN KEY (player_in_id) REFERENCES players(id),
                FOREIGN KEY (player_out_id) REFERENCES players(id)
            )
        """;

        // Cards table
        String createCardsTable = """
            CREATE TABLE IF NOT EXISTS cards (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                match_id INTEGER NOT NULL,
                player_id INTEGER NOT NULL,
                team_id INTEGER NOT NULL,
                card_type TEXT NOT NULL, -- 'YELLOW', 'RED'
                minute INTEGER NOT NULL,
                reason TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (match_id) REFERENCES matches(id),
                FOREIGN KEY (player_id) REFERENCES players(id),
                FOREIGN KEY (team_id) REFERENCES teams(id)
            )
        """;

        // Tournament table
        String createTournamentTable = """
            CREATE TABLE IF NOT EXISTS tournaments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                year INTEGER NOT NULL,
                host_country TEXT NOT NULL,
                start_date DATE,
                end_date DATE,
                champion_id INTEGER,
                runner_up_id INTEGER,
                third_place_id INTEGER,
                status TEXT DEFAULT 'PREPARING', -- 'PREPARING', 'GROUP_STAGE', 'KNOCKOUT', 'COMPLETED'
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (champion_id) REFERENCES teams(id),
                FOREIGN KEY (runner_up_id) REFERENCES teams(id),
                FOREIGN KEY (third_place_id) REFERENCES teams(id)
            )
        """;

        // Tournament statistics table
        String createTournamentStatsTable = """
            CREATE TABLE IF NOT EXISTS tournament_stats (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tournament_id INTEGER NOT NULL,
                total_goals INTEGER DEFAULT 0,
                total_matches INTEGER DEFAULT 0,
                total_yellow_cards INTEGER DEFAULT 0,
                total_red_cards INTEGER DEFAULT 0,
                total_substitutions INTEGER DEFAULT 0,
                top_scorer_id INTEGER,
                top_scorer_goals INTEGER DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (tournament_id) REFERENCES tournaments(id),
                FOREIGN KEY (top_scorer_id) REFERENCES players(id)
            )
        """;

        // Execute all table creation statements
        Statement stmt = connection.createStatement();
        stmt.execute(createTeamsTable);
        stmt.execute(createPlayersTable);
        stmt.execute(createAssistantCoachesTable);
        stmt.execute(createGroupsTable);
        stmt.execute(createMatchesTable);
        stmt.execute(createGoalsTable);
        stmt.execute(createSubstitutionsTable);
        stmt.execute(createCardsTable);
        stmt.execute(createTournamentTable);
        stmt.execute(createTournamentStatsTable);
        stmt.close();
    }

    public Connection getConnection() {
        return connection;
    }

    public void clearAllData() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Disable foreign key constraints temporarily
        stmt.execute("PRAGMA foreign_keys = OFF");
        
        // Clear all tables in reverse dependency order
        stmt.execute("DELETE FROM tournament_stats");
        stmt.execute("DELETE FROM cards");
        stmt.execute("DELETE FROM substitutions");
        stmt.execute("DELETE FROM goals");
        stmt.execute("DELETE FROM matches");
        stmt.execute("DELETE FROM assistant_coaches");
        stmt.execute("DELETE FROM players");
        stmt.execute("DELETE FROM teams");
        stmt.execute("DELETE FROM groups");
        stmt.execute("DELETE FROM tournaments");
        
        // Reset auto-increment counters
        stmt.execute("DELETE FROM sqlite_sequence");
        
        // Re-enable foreign key constraints
        stmt.execute("PRAGMA foreign_keys = ON");
        
        stmt.close();
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Utility methods for common database operations
    public int getLastInsertId() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
        int id = rs.getInt(1);
        rs.close();
        stmt.close();
        return id;
    }

    public boolean tableExists(String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"});
        boolean exists = rs.next();
        rs.close();
        return exists;
    }
}