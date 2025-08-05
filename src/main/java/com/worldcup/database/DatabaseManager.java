 package com.worldcup.database;

 import java.sql.*;

 public class DatabaseManager {
     private static final String DB_URL = "jdbc:sqlite:worldcup.db";
     private Connection connection;

     public DatabaseManager() {
         try {
             // Load SQLite driver
             Class.forName("org.sqlite.JDBC");
             connection = DriverManager.getConnection(DB_URL);
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
                 group_id INTEGER,
                 tournament_id INTEGER NOT NULL,
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
                 is_eligible BOOLEAN DEFAULT TRUE,
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
                 FOREIGN KEY (team_id) REFERENCES teams(id)
             )
         """;

         // Groups table
         String createGroupsTable = """
             CREATE TABLE IF NOT EXISTS groups (
                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                 name TEXT NOT NULL,
                 tournament_id INTEGER NOT NULL,
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
                 match_date TEXT, -- Format: yyyy/mm/dd
                 venue TEXT,
                 referee TEXT,
                 winner_id INTEGER, -- ID of winning team, NULL for draws
                 FOREIGN KEY (team_a_id) REFERENCES teams(id),
                 FOREIGN KEY (team_b_id) REFERENCES teams(id),
                 FOREIGN KEY (winner_id) REFERENCES teams(id)
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
                 FOREIGN KEY (match_id) REFERENCES matches(id),
                 FOREIGN KEY (player_id) REFERENCES players(id),
                 FOREIGN KEY (team_id) REFERENCES teams(id)
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
                 end_date DATE
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
                 champion_id INTEGER,
                 runner_up_id INTEGER,
                 third_place_id_01 INTEGER,
                 third_place_id_02 INTEGER,
                 FOREIGN KEY (tournament_id) REFERENCES tournaments(id),
                 FOREIGN KEY (top_scorer_id) REFERENCES players(id),
                 FOREIGN KEY (champion_id) REFERENCES teams(id),
                 FOREIGN KEY (runner_up_id) REFERENCES teams(id),
                 FOREIGN KEY (third_place_id_01) REFERENCES teams(id),
                 FOREIGN KEY (third_place_id_02) REFERENCES teams(id)
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
         
         // Run migrations for existing databases
         runMigrations();
     }
     
     /**
      * Run database migrations for schema updates
      */
     private void runMigrations() throws SQLException {
         // Check if winner_id column exists in matches table
         try {
             String checkColumn = "SELECT winner_id FROM matches LIMIT 1";
             PreparedStatement pstmt = connection.prepareStatement(checkColumn);
             pstmt.executeQuery();
             pstmt.close();
         } catch (SQLException e) {
             // Column doesn't exist, add it
             String addWinnerColumn = "ALTER TABLE matches ADD COLUMN winner_id INTEGER REFERENCES teams(id)";
             Statement stmt = connection.createStatement();
             stmt.execute(addWinnerColumn);
             stmt.close();
             
         }
     }

     public Connection getConnection() {
         return connection;
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
 }