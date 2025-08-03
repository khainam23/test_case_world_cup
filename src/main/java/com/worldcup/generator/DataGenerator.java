package com.worldcup.generator;

import com.worldcup.Player;
import com.worldcup.Team;

import java.util.*;

public class DataGenerator {
    private static final Random random = new Random();
    
    // Real country names for authenticity
    private static final String[] COUNTRIES = {
        "Brazil", "Argentina", "Germany", "France", "Spain", "Italy", "England", "Netherlands",
        "Portugal", "Belgium", "Croatia", "Uruguay", "Colombia", "Mexico", "Japan", "South Korea",
        "Morocco", "Senegal", "Ghana", "Nigeria", "Australia", "Denmark", "Switzerland", "Poland",
        "Austria", "Czech Republic", "Serbia", "Ecuador", "Peru", "Chile", "Canada", "USA"
    };
    
    // Regions for World Cup
    private static final String[] REGIONS = {
        "South America", "Europe", "North America", "Africa", "Asia", "Oceania"
    };
    
    // Common first names from various countries
    private static final String[] FIRST_NAMES = {
        "Ahmed", "Ali", "Antonio", "Carlos", "David", "Diego", "Eduardo", "Fernando", "Gabriel", "Hassan",
        "Ivan", "James", "João", "Jose", "Juan", "Kevin", "Luis", "Marco", "Mario", "Miguel",
        "Mohamed", "Nicolas", "Omar", "Pablo", "Pedro", "Rafael", "Roberto", "Samuel", "Sebastian", "Victor",
        "Alexander", "Andreas", "Christian", "Daniel", "Erik", "Felix", "Hans", "Henrik", "Jan", "Klaus",
        "Lars", "Magnus", "Nils", "Oliver", "Patrick", "Stefan", "Thomas", "Tobias", "Ulf", "Wolfgang",
        "Adrien", "Antoine", "Baptiste", "Clement", "Florian", "Guillaume", "Hugo", "Julien", "Lucas", "Maxime",
        "Nathan", "Olivier", "Pierre", "Quentin", "Romain", "Sebastien", "Theo", "Vincent", "Xavier", "Yann"
    };
    
    // Common last names from various countries
    private static final String[] LAST_NAMES = {
        "Silva", "Santos", "Oliveira", "Pereira", "Costa", "Rodrigues", "Martins", "Jesus", "Sousa", "Fernandes",
        "Garcia", "Rodriguez", "Martinez", "Lopez", "Gonzalez", "Hernandez", "Perez", "Sanchez", "Ramirez", "Torres",
        "Mueller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker", "Schulz", "Hoffmann",
        "Martin", "Bernard", "Dubois", "Thomas", "Robert", "Petit", "Durand", "Leroy", "Moreau", "Simon",
        "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Garcia", "Rodriguez", "Wilson", "Martinez",
        "Anderson", "Taylor", "Thomas", "Hernandez", "Moore", "Martin", "Jackson", "Thompson", "White", "Lopez"
    };
    
    // Football positions
    private static final String[] POSITIONS = {"GK", "CB", "LB", "RB", "CDM", "CM", "CAM", "LM", "RM", "LW", "RW", "ST"};
    
    // Coach names
    private static final String[] COACH_NAMES = {
        "Antonio Conte", "Carlo Ancelotti", "Diego Simeone", "Frank Lampard", "Gareth Southgate",
        "Hansi Flick", "Jurgen Klopp", "Luis Enrique", "Mauricio Pochettino", "Pep Guardiola",
        "Roberto Martinez", "Ronald Koeman", "Tite", "Zinedine Zidane", "Jose Mourinho",
        "Thomas Tuchel", "Julian Nagelsmann", "Erik ten Hag", "Xavi Hernandez", "Mikel Arteta"
    };
    
    // Referee names
    private static final String[] REFEREE_NAMES = {
        "Bjorn Kuipers", "Felix Brych", "Daniele Orsato", "Antonio Mateu Lahoz", "Clement Turpin",
        "Michael Oliver", "Stephanie Frappart", "Cuneyt Cakir", "Nestor Pitana", "Wilton Sampaio",
        "Raphael Claus", "Jesus Gil Manzano", "Slavko Vincic", "Istvan Kovacs", "Orel Grinfeeld"
    };
    
    // Stadium names
    private static final String[] VENUES = {
        "Stadium 974", "Lusail Stadium", "Al Bayt Stadium", "Al Thumama Stadium", "Education City Stadium",
        "Ahmad bin Ali Stadium", "Al Janoub Stadium", "Al Rayyan Stadium", "Khalifa International Stadium",
        "Maracana Stadium", "Wembley Stadium", "Camp Nou", "Santiago Bernabeu", "Allianz Arena",
        "Old Trafford", "Emirates Stadium", "Anfield", "Stamford Bridge", "San Siro", "Juventus Stadium"
    };

    public static List<Team> generateRandomTeams(int count) {
        if (count > COUNTRIES.length) {
            throw new IllegalArgumentException("Cannot generate more teams than available countries");
        }
        
        List<String> availableCountries = new ArrayList<>(Arrays.asList(COUNTRIES));
        Collections.shuffle(availableCountries);
        
        List<Team> teams = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String countryName = availableCountries.get(i);
            String region = getRandomRegion();
            String coach = getRandomCoach();
            List<String> assistants = generateAssistantCoaches();
            String medicalStaff = "Dr. " + getRandomName();
            
            // Generate players
            List<Player> startingPlayers = generateStartingPlayers();
            List<Player> substitutePlayers = generateSubstitutePlayers();
            
            boolean isHost = (i == 0); // First team is host
            
            Team team = new Team(countryName, region, coach, assistants, medicalStaff, 
                               startingPlayers, substitutePlayers, isHost);
            teams.add(team);
        }
        
        return teams;
    }
    
    private static List<Player> generateStartingPlayers() {
        List<Player> players = new ArrayList<>();
        Set<Integer> usedNumbers = new HashSet<>();
        
        // Generate 11 starting players with specific positions
        String[] startingPositions = {"GK", "CB", "CB", "LB", "RB", "CDM", "CM", "CAM", "LW", "RW", "ST"};
        
        for (int i = 0; i < 11; i++) {
            int jerseyNumber = generateUniqueJerseyNumber(usedNumbers, 1, 11);
            String name = getRandomName();
            String position = startingPositions[i];
            
            Player player = new Player(name, jerseyNumber, position);
            players.add(player);
        }
        
        return players;
    }
    
    private static List<Player> generateSubstitutePlayers() {
        List<Player> players = new ArrayList<>();
        Set<Integer> usedNumbers = new HashSet<>();
        
        // Add numbers 1-11 as used (for starting players)
        for (int i = 1; i <= 11; i++) {
            usedNumbers.add(i);
        }
        
        // Generate 11 substitute players (positions 12-22)
        for (int i = 0; i < 11; i++) {
            int jerseyNumber = generateUniqueJerseyNumber(usedNumbers, 12, 22);
            String name = getRandomName();
            String position = getRandomPosition();
            
            Player player = new Player(name, jerseyNumber, position);
            players.add(player);
        }
        
        return players;
    }
    
    private static int generateUniqueJerseyNumber(Set<Integer> usedNumbers, int min, int max) {
        int number;
        do {
            number = random.nextInt(max - min + 1) + min;
        } while (usedNumbers.contains(number));
        
        usedNumbers.add(number);
        return number;
    }
    
    private static String getRandomName() {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        return firstName + " " + lastName;
    }
    
    private static String getRandomPosition() {
        return POSITIONS[random.nextInt(POSITIONS.length)];
    }
    
    private static String getRandomRegion() {
        return REGIONS[random.nextInt(REGIONS.length)];
    }
    
    private static String getRandomCoach() {
        return COACH_NAMES[random.nextInt(COACH_NAMES.length)];
    }
    
    private static List<String> generateAssistantCoaches() {
        List<String> assistants = new ArrayList<>();
        int count = random.nextInt(3) + 1; // 1-3 assistants
        
        for (int i = 0; i < count; i++) {
            assistants.add("Assistant " + getRandomName());
        }
        
        return assistants;
    }
    
    public static String getRandomReferee() {
        return REFEREE_NAMES[random.nextInt(REFEREE_NAMES.length)];
    }
    
    public static String getRandomVenue() {
        return VENUES[random.nextInt(VENUES.length)];
    }
    
    // Generate random match events
    public static int generateRandomGoals() {
        // Most matches have 0-4 goals, with 1-2 being most common
        double rand = random.nextDouble();
        if (rand < 0.15) return 0;      // 15% chance of 0 goals
        else if (rand < 0.35) return 1; // 20% chance of 1 goal
        else if (rand < 0.60) return 2; // 25% chance of 2 goals
        else if (rand < 0.80) return 3; // 20% chance of 3 goals
        else if (rand < 0.95) return 4; // 15% chance of 4 goals
        else return 5 + random.nextInt(3); // 5% chance of 5-7 goals
    }
    
    public static int generateRandomMinute() {
        // Goals more likely in certain periods
        double rand = random.nextDouble();
        if (rand < 0.4) {
            return random.nextInt(45) + 1; // First half
        } else if (rand < 0.8) {
            return random.nextInt(45) + 46; // Second half
        } else {
            return random.nextInt(15) + 91; // Extra time
        }
    }
    
    public static boolean shouldHaveYellowCard() {
        return random.nextDouble() < 0.3; // 30% chance
    }
    
    public static boolean shouldHaveRedCard() {
        return random.nextDouble() < 0.05; // 5% chance
    }
    
    public static boolean shouldHaveSubstitution() {
        return random.nextDouble() < 0.8; // 80% chance of at least one substitution
    }
    
    public static int generateSubstitutionMinute() {
        // Substitutions more common in second half
        return random.nextInt(60) + 30; // Between minute 30-90
    }
    
    // Generate realistic match results
    public static int[] generateMatchScore() {
        int totalGoals = generateRandomGoals();
        
        if (totalGoals == 0) {
            return new int[]{0, 0};
        }
        
        // Distribute goals between teams
        int teamAGoals = random.nextInt(totalGoals + 1);
        int teamBGoals = totalGoals - teamAGoals;
        
        return new int[]{teamAGoals, teamBGoals};
    }
    
    // Utility method to get random element from list
    public static <T> T getRandomElement(List<T> list) {
        if (list.isEmpty()) return null;
        return list.get(random.nextInt(list.size()));
    }
    
    // Generate tournament name
    public static String generateTournamentName(int year) {
        return "FIFA World Cup " + year;
    }
    
    // Generate random date within tournament period
    public static java.sql.Date generateRandomMatchDate(java.sql.Date startDate, java.sql.Date endDate) {
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long randomTime = startTime + (long) (random.nextDouble() * (endTime - startTime));
        return new java.sql.Date(randomTime);
    }
}