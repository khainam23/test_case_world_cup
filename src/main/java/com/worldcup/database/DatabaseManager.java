package com.worldcup.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:worldcup.db";
    private Connection connection;

    public DatabaseManager() {
        try {
            // Load SQLite driver
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            migrateDatabase();
            initializeTables();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void initializeTables() throws SQLException {
        // Teams table
        String createTeamsTable = """
            CREATE TABLE IF NOT EXISTS teams (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
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
                tournament_id INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (tournament_id) REFERENCES tournaments(id),
                UNIQUE(name, tournament_id)
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
                name TEXT NOT NULL,
                tournament_id INTEGER NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (tournament_id) REFERENCES tournaments(id),
                UNIQUE(name, tournament_id)
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

        // Execute all table creation statements in correct order (parent tables first)
        Statement stmt = connection.createStatement();
        
        // First create tournaments table (no dependencies)
        stmt.execute(createTournamentTable);
        
        // Then create teams table (depends on tournaments)
        stmt.execute(createTeamsTable);
        
        // Then create groups table (depends on tournaments)
        stmt.execute(createGroupsTable);
        
        // Then create players table (depends on teams)
        stmt.execute(createPlayersTable);
        
        // Then create assistant_coaches table (depends on teams)
        stmt.execute(createAssistantCoachesTable);
        
        // Then create matches table (depends on teams and groups)
        stmt.execute(createMatchesTable);
        
        // Finally create tables that depend on matches and players
        stmt.execute(createGoalsTable);
        stmt.execute(createSubstitutionsTable);
        stmt.execute(createCardsTable);
        stmt.execute(createTournamentStatsTable);
        
        stmt.close();
        
        // Restore backup data if exists
        restoreBackupData();
    }

    // WARNING: This method will DELETE ALL DATA! Only use for complete database reset.
    // Use migrateDatabase() instead to preserve existing data.
    private void recreateDatabaseWithNewStructure() throws SQLException {
        System.out.println("Recreating database with new structure supporting multiple tournaments...");
        
        Statement stmt = connection.createStatement();
        
        // Disable foreign key constraints temporarily
        stmt.execute("PRAGMA foreign_keys = OFF");
        
        // Drop all existing tables to recreate with new structure
        stmt.execute("DROP TABLE IF EXISTS tournament_stats");
        stmt.execute("DROP TABLE IF EXISTS cards");
        stmt.execute("DROP TABLE IF EXISTS substitutions");
        stmt.execute("DROP TABLE IF EXISTS goals");
        stmt.execute("DROP TABLE IF EXISTS matches");
        stmt.execute("DROP TABLE IF EXISTS assistant_coaches");
        stmt.execute("DROP TABLE IF EXISTS players");
        stmt.execute("DROP TABLE IF EXISTS teams");
        stmt.execute("DROP TABLE IF EXISTS groups");
        stmt.execute("DROP TABLE IF EXISTS tournaments");
        
        // Reset auto-increment counters (only if sqlite_sequence table exists)
        try {
            stmt.execute("DELETE FROM sqlite_sequence WHERE name IN ('teams', 'groups', 'players', 'matches', 'goals', 'cards', 'substitutions', 'tournaments', 'tournament_stats', 'assistant_coaches')");
        } catch (SQLException e) {
            // sqlite_sequence table doesn't exist yet, which is fine
            System.out.println("sqlite_sequence table doesn't exist yet - will be created automatically");
        }
        
        // Re-enable foreign key constraints
        stmt.execute("PRAGMA foreign_keys = ON");
        
        stmt.close();
        
        System.out.println("Database recreated with new structure successfully");
    }

    private void migrateDatabase() throws SQLException {
        Statement stmt = connection.createStatement();
        
        try {
            // Check if tournaments table exists
            boolean tournamentsTableExists = tableExists("tournaments");
            
            if (!tournamentsTableExists) {
                System.out.println("Creating new database with multi-tournament support structure");
                return; // Let initializeTables() handle creating new tables
            }
            
            // Check if tournament_id column exists in teams table
            boolean teamsTableExists = tableExists("teams");
            boolean hasTournamentId = false;
            
            if (teamsTableExists) {
                ResultSet rs = stmt.executeQuery("PRAGMA table_info(teams)");
                while (rs.next()) {
                    if ("tournament_id".equals(rs.getString("name"))) {
                        hasTournamentId = true;
                        break;
                    }
                }
                rs.close();
            }
            
            if (teamsTableExists && !hasTournamentId) {
                System.out.println("Upgrading database structure to support multiple tournaments...");
                System.out.println("Backing up existing data...");
                
                // Disable foreign key constraints temporarily
                stmt.execute("PRAGMA foreign_keys = OFF");
                
                // Check if there's existing data
                ResultSet teamsRs = stmt.executeQuery("SELECT COUNT(*) FROM teams");
                boolean hasData = teamsRs.next() && teamsRs.getInt(1) > 0;
                teamsRs.close();
                
                if (hasData) {
                    // Create a default tournament for existing data
                    System.out.println("Creating default tournament for existing data...");
                    stmt.execute("""
                        INSERT OR IGNORE INTO tournaments (id, name, year, host_country, start_date, end_date, status)
                        VALUES (1, 'World Cup Legacy', 2023, 'Unknown', date('now'), date('now', '+30 days'), 'COMPLETED')
                    """);
                    
                    // Backup all existing data to temporary tables
                    System.out.println("Backing up teams data...");
                    stmt.execute("CREATE TEMPORARY TABLE teams_backup AS SELECT * FROM teams");
                    
                    System.out.println("Backing up groups data...");
                    try {
                        stmt.execute("CREATE TEMPORARY TABLE groups_backup AS SELECT * FROM groups");
                    } catch (SQLException e) {
                        System.out.println("Groups table doesn't exist or is empty");
                    }
                    
                    System.out.println("Backing up players data...");
                    try {
                        stmt.execute("CREATE TEMPORARY TABLE players_backup AS SELECT * FROM players");
                    } catch (SQLException e) {
                        System.out.println("Players table doesn't exist or is empty");
                    }
                    
                    System.out.println("Backing up matches data...");
                    try {
                        stmt.execute("CREATE TEMPORARY TABLE matches_backup AS SELECT * FROM matches");
                    } catch (SQLException e) {
                        System.out.println("Matches table doesn't exist or is empty");
                    }
                    
                    System.out.println("Backing up goals data...");
                    try {
                        stmt.execute("CREATE TEMPORARY TABLE goals_backup AS SELECT * FROM goals");
                    } catch (SQLException e) {
                        System.out.println("Goals table doesn't exist or is empty");
                    }
                    
                    System.out.println("Backing up cards data...");
                    try {
                        stmt.execute("CREATE TEMPORARY TABLE cards_backup AS SELECT * FROM cards");
                    } catch (SQLException e) {
                        System.out.println("Cards table doesn't exist or is empty");
                    }
                    
                    System.out.println("Backing up substitutions data...");
                    try {
                        stmt.execute("CREATE TEMPORARY TABLE substitutions_backup AS SELECT * FROM substitutions");
                    } catch (SQLException e) {
                        System.out.println("Substitutions table doesn't exist or is empty");
                    }
                    
                    // Drop existing tables in correct order (child tables first)
                    System.out.println("Dropping old tables to recreate with new structure...");
                    stmt.execute("DROP TABLE IF EXISTS substitutions");
                    stmt.execute("DROP TABLE IF EXISTS cards");
                    stmt.execute("DROP TABLE IF EXISTS goals");
                    stmt.execute("DROP TABLE IF EXISTS matches");
                    stmt.execute("DROP TABLE IF EXISTS assistant_coaches");
                    stmt.execute("DROP TABLE IF EXISTS players");
                    stmt.execute("DROP TABLE IF EXISTS teams");
                    stmt.execute("DROP TABLE IF EXISTS groups");
                    
                    // Re-enable foreign key constraints
                    stmt.execute("PRAGMA foreign_keys = ON");
                    
                    System.out.println("Data backup completed. Data will be restored after creating new tables.");
                } else {
                    System.out.println("No existing data to backup");
                    stmt.execute("PRAGMA foreign_keys = ON");
                }
                
                System.out.println("Database migration preparation completed");
            } else {
                System.out.println("Database already has new structure, no migration needed");
            }
        } catch (SQLException e) {
            System.out.println("Error checking database structure: " + e.getMessage());
            // If there's an error, assume it's a new database
            System.out.println("Creating new database with multi-tournament support structure");
        }
        
        stmt.close();
    }

    private void restoreBackupData() throws SQLException {
        Statement stmt = connection.createStatement();
        
        try {
            // Check if backup tables exist
            boolean hasTeamsBackup = false;
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM teams_backup");
                hasTeamsBackup = rs.next();
                rs.close();
            } catch (SQLException e) {
                // Backup table doesn't exist
            }
            
            if (hasTeamsBackup) {
                System.out.println("Restoring data from backup...");
                
                // Disable foreign key constraints temporarily
                stmt.execute("PRAGMA foreign_keys = OFF");
                
                // Restore teams data with tournament_id = 1 (default tournament)
                System.out.println("Restoring teams data...");
                stmt.execute("""
                    INSERT INTO teams (id, name, region, coach, medical_staff, is_host, points, 
                                     goal_difference, goals_for, goals_against, wins, draws, losses,
                                     yellow_cards, red_cards, substitution_count, group_id, tournament_id)
                    SELECT id, name, region, coach, medical_staff, is_host, points,
                           goal_difference, goals_for, goals_against, wins, draws, losses,
                           yellow_cards, red_cards, substitution_count, group_id, 1
                    FROM teams_backup
                """);
                
                // Restore groups data with tournament_id = 1
                try {
                    System.out.println("Restoring groups data...");
                    stmt.execute("""
                        INSERT INTO groups (id, name, tournament_id)
                        SELECT id, name, 1
                        FROM groups_backup
                    """);
                } catch (SQLException e) {
                    System.out.println("Cannot restore groups data: " + e.getMessage());
                }
                
                // Restore players data
                try {
                    System.out.println("Restoring players data...");
                    stmt.execute("""
                        INSERT INTO players (id, name, jersey_number, position, team_id, is_starting,
                                           yellow_cards, red_cards, goals, assists, minutes_played, is_eligible)
                        SELECT id, name, jersey_number, position, team_id, is_starting,
                               yellow_cards, red_cards, goals, assists, minutes_played, is_eligible
                        FROM players_backup
                    """);
                } catch (SQLException e) {
                    System.out.println("Cannot restore players data: " + e.getMessage());
                }
                
                // Restore matches data
                try {
                    System.out.println("Restoring matches data...");
                    stmt.execute("""
                        INSERT INTO matches (id, team_a_id, team_b_id, team_a_score, team_b_score,
                                           match_type, match_date, venue, referee, status, winner_id,
                                           group_id, round_number)
                        SELECT id, team_a_id, team_b_id, team_a_score, team_b_score,
                               match_type, match_date, venue, referee, status, winner_id,
                               group_id, round_number
                        FROM matches_backup
                    """);
                } catch (SQLException e) {
                    System.out.println("Cannot restore matches data: " + e.getMessage());
                }
                
                // Restore goals data
                try {
                    System.out.println("Restoring goals data...");
                    stmt.execute("""
                        INSERT INTO goals (id, match_id, player_id, team_id, minute, goal_type, assist_player_id)
                        SELECT id, match_id, player_id, team_id, minute, goal_type, assist_player_id
                        FROM goals_backup
                    """);
                } catch (SQLException e) {
                    System.out.println("Cannot restore goals data: " + e.getMessage());
                }
                
                // Restore cards data
                try {
                    System.out.println("Restoring cards data...");
                    stmt.execute("""
                        INSERT INTO cards (id, match_id, player_id, team_id, card_type, minute, reason)
                        SELECT id, match_id, player_id, team_id, card_type, minute, reason
                        FROM cards_backup
                    """);
                } catch (SQLException e) {
                    System.out.println("Cannot restore cards data: " + e.getMessage());
                }
                
                // Restore substitutions data
                try {
                    System.out.println("Restoring substitutions data...");
                    stmt.execute("""
                        INSERT INTO substitutions (id, match_id, team_id, player_in_id, player_out_id, minute)
                        SELECT id, match_id, team_id, player_in_id, player_out_id, minute
                        FROM substitutions_backup
                    """);
                } catch (SQLException e) {
                    System.out.println("Cannot restore substitutions data: " + e.getMessage());
                }
                
                // Re-enable foreign key constraints
                stmt.execute("PRAGMA foreign_keys = ON");
                
                // Drop backup tables
                System.out.println("Dropping temporary backup tables...");
                stmt.execute("DROP TABLE IF EXISTS teams_backup");
                stmt.execute("DROP TABLE IF EXISTS groups_backup");
                stmt.execute("DROP TABLE IF EXISTS players_backup");
                stmt.execute("DROP TABLE IF EXISTS matches_backup");
                stmt.execute("DROP TABLE IF EXISTS goals_backup");
                stmt.execute("DROP TABLE IF EXISTS cards_backup");
                stmt.execute("DROP TABLE IF EXISTS substitutions_backup");
                
                System.out.println("Data restoration completed successfully!");
                
                // Count restored data
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM teams");
                if (rs.next()) {
                    int teamCount = rs.getInt(1);
                    System.out.println("Restored " + teamCount + " teams");
                }
                rs.close();
                
                rs = stmt.executeQuery("SELECT COUNT(*) FROM players");
                if (rs.next()) {
                    int playerCount = rs.getInt(1);
                    System.out.println("Restored " + playerCount + " players");
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.out.println("Error restoring data: " + e.getMessage());
            throw e;
        }
        
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
    
    // Method to create a new tournament without affecting existing data
    public int createNewTournament(String name, int year, String hostCountry, String startDate, String endDate) throws SQLException {
        String insertTournament = """
            INSERT INTO tournaments (name, year, host_country, start_date, end_date, status)
            VALUES (?, ?, ?, ?, ?, 'PREPARING')
        """;
        
        PreparedStatement pstmt = connection.prepareStatement(insertTournament);
        pstmt.setString(1, name);
        pstmt.setInt(2, year);
        pstmt.setString(3, hostCountry);
        pstmt.setString(4, startDate);
        pstmt.setString(5, endDate);
        
        pstmt.executeUpdate();
        int tournamentId = getLastInsertId();
        pstmt.close();
        
        System.out.println("Created new tournament: " + name + " (ID: " + tournamentId + ")");
        return tournamentId;
    }
    
    // Method to safely clear data for a specific tournament only
    public void clearTournamentData(int tournamentId) throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Disable foreign key constraints temporarily
        stmt.execute("PRAGMA foreign_keys = OFF");
        
        // Clear tournament-specific data in reverse dependency order
        stmt.execute("DELETE FROM tournament_stats WHERE tournament_id = " + tournamentId);
        stmt.execute("DELETE FROM cards WHERE match_id IN (SELECT id FROM matches WHERE team_a_id IN (SELECT id FROM teams WHERE tournament_id = " + tournamentId + ") OR team_b_id IN (SELECT id FROM teams WHERE tournament_id = " + tournamentId + "))");
        stmt.execute("DELETE FROM substitutions WHERE match_id IN (SELECT id FROM matches WHERE team_a_id IN (SELECT id FROM teams WHERE tournament_id = " + tournamentId + ") OR team_b_id IN (SELECT id FROM teams WHERE tournament_id = " + tournamentId + "))");
        stmt.execute("DELETE FROM goals WHERE match_id IN (SELECT id FROM matches WHERE team_a_id IN (SELECT id FROM teams WHERE tournament_id = " + tournamentId + ") OR team_b_id IN (SELECT id FROM teams WHERE tournament_id = " + tournamentId + "))");
        stmt.execute("DELETE FROM matches WHERE team_a_id IN (SELECT id FROM teams WHERE tournament_id = " + tournamentId + ") OR team_b_id IN (SELECT id FROM teams WHERE tournament_id = " + tournamentId + ")");
        stmt.execute("DELETE FROM assistant_coaches WHERE team_id IN (SELECT id FROM teams WHERE tournament_id = " + tournamentId + ")");
        stmt.execute("DELETE FROM players WHERE team_id IN (SELECT id FROM teams WHERE tournament_id = " + tournamentId + ")");
        stmt.execute("DELETE FROM teams WHERE tournament_id = " + tournamentId);
        stmt.execute("DELETE FROM groups WHERE tournament_id = " + tournamentId);
        stmt.execute("DELETE FROM tournaments WHERE id = " + tournamentId);
        
        // Re-enable foreign key constraints
        stmt.execute("PRAGMA foreign_keys = ON");
        
        stmt.close();
        System.out.println("Cleared data for tournament ID: " + tournamentId);
    }
}