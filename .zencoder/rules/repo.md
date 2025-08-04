---
description: Repository Information Overview
alwaysApply: true
---

# World Cup Management System Information

## Summary
A Java-based World Cup management system that simulates and manages football tournaments. The application follows Object-Oriented Programming (OOP) and SOLID principles, with all processing done in Java rather than SQL. It provides functionality for team statistics, player management, match results, tournament overviews, and data generation.

## Structure
- **src/main/java**: Contains all Java source code organized in packages
- **src/test/java**: Contains JUnit 5 test classes
- **lib**: External libraries
- **build**: Compiled class files
- **target**: Maven build output directory
- **worldcup.db**: SQLite database file for storing tournament data

## Language & Runtime
**Language**: Java
**Version**: Java 15 (with Java 8 compatibility)
**Build System**: Maven
**Package Manager**: Maven

## Dependencies
**Main Dependencies**:
- JUnit Jupiter 5.9.3 (Testing)
- SQLite JDBC 3.44.1.0 (Database)
- Jackson Databind 2.15.2 (JSON processing)

**Development Dependencies**:
- Maven Compiler Plugin 3.11.0
- Maven Surefire Plugin 3.2.5

## Build & Installation
```bash
mvn clean compile
mvn package
```

## Database
**Type**: SQLite
**File**: worldcup.db
**Tables**: teams, players, matches, goals, cards, substitutions, tournaments, groups, tournament_stats, assistant_coaches
**Connection**: JDBC with connection string "jdbc:sqlite:worldcup.db"

## Main Entry Points
**Main Class**: com.worldcup.Main
**Application Class**: com.worldcup.WorldCupApplication
**Key Components**:
- **Tournament**: Manages the overall tournament structure
- **Team**: Represents a football team with players and statistics
- **Match**: Handles match data and results
- **Player**: Manages player information and statistics
- **DatabaseManager**: Handles database operations

## Testing
**Framework**: JUnit Jupiter 5.9.3
**Test Location**: src/test/java/com/worldcup
**Naming Convention**: ClassNameTest.java
**Test Types**: Unit tests with boundary value analysis and equivalence partitioning
**Run Command**:
```bash
mvn test
```

## Key Features
- Team and player statistics calculation
- Match simulation and management
- Tournament group stage and knockout phase handling
- Yellow/red card tracking
- Goal tracking and statistics
- SQLite database for persistent storage
- Comprehensive test suite with JUnit 5