package com.worldcup.generator;

import com.worldcup.constant.Constant;
import com.worldcup.model.Group;
import com.worldcup.model.Player;
import com.worldcup.model.Team;

import java.time.LocalDate;
import java.util.*;

import static com.worldcup.constant.Constant.*;

public class DataGenerator {
    public static final Random random = new Random();


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


    // Utility method to get random element from list
    public static <T> T getRandomElement(List<T> list) {
        if (list.isEmpty()) return null;
        return list.get(random.nextInt(list.size()));
    }


    // Generate random date within tournament period
    public java.sql.Date generateRandomMatchDate(java.sql.Date startDate, java.sql.Date endDate) {
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long randomTime = startTime + (long) (random.nextDouble() * (endTime - startTime));
        return new java.sql.Date(randomTime);
    }

    // Các năm World Cup thực tế từ 1990 đến 2026
    public final int[] WORLD_CUP_YEARS = {1990, 1994, 1998, 2002, 2006, 2010, 2014, 2018, 2022, 2026};

    // Danh sách các quốc gia có thể tổ chức World Cup
    public final String[] HOST_COUNTRIES = {"Brazil", "Argentina", "Germany", "France", "Spain", "Italy", "England", "Netherlands", "Portugal", "Belgium", "Croatia", "Uruguay", "Colombia", "Mexico", "Japan", "South Korea", "Morocco", "Senegal", "Ghana", "Nigeria", "Australia", "Denmark", "Switzerland", "Poland", "Austria", "Czech Republic", "Serbia", "Ecuador", "Peru", "Chile", "Canada", "USA", "Russia", "Qatar", "China", "India", "Thailand", "Vietnam", "Indonesia", "Malaysia", "Saudi Arabia", "UAE", "Egypt", "South Africa", "Kenya", "Algeria", "Tunisia", "Cameroon"};

    /**
     * Random một năm World Cup thực tế
     */
    public int getRandomYear() {
        return WORLD_CUP_YEARS[random.nextInt(WORLD_CUP_YEARS.length)];
    }

    /**
     * Tạo ngày bắt đầu và kết thúc cho tournament dựa trên năm
     * World Cup thường diễn ra vào tháng 6-7
     */

    public void randomizeTournament(int currentTournamentId, String tournamentName) {
    }

    public static List<Group> getGroups() {
        List<Group> groups = new ArrayList<>();

        for (String groupName : Constant.GROUP_NAME) {
            groups.add(new Group(groupName));
        }
        return groups;
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

    /**
     * Random một năm World Cup thực tế
     */
    public static int getRandomWorldCupYear() {
        int[] WORLD_CUP_YEARS = {1990, 1994, 1998, 2002, 2006, 2010, 2014, 2018, 2022, 2026};
        return WORLD_CUP_YEARS[random.nextInt(WORLD_CUP_YEARS.length)];
    }

    /**
     * Random một quốc gia tổ chức
     */
    public static String getRandomHostCountry() {
        String[] HOST_COUNTRIES = {"Brazil", "Argentina", "Germany", "France", "Spain", "Italy", "England", "Netherlands", "Portugal", "Belgium", "Croatia", "Uruguay", "Colombia", "Mexico", "Japan", "South Korea", "Morocco", "Senegal", "Ghana", "Nigeria", "Australia", "Denmark", "Switzerland", "Poland", "Austria", "Czech Republic", "Serbia", "Ecuador", "Peru", "Chile", "Canada", "USA", "Russia", "Qatar", "China", "India", "Thailand", "Vietnam", "Indonesia", "Malaysia", "Saudi Arabia", "UAE", "Egypt", "South Africa", "Kenya", "Algeria", "Tunisia", "Cameroon"};
        return HOST_COUNTRIES[random.nextInt(HOST_COUNTRIES.length)];
    }

    /**
     * Tạo ngày bắt đầu và kết thúc cho tournament dựa trên năm
     * World Cup thường diễn ra vào tháng 6-7 (hoặc 11-12 cho Qatar 2022)
     */
    public static LocalDate[] generateTournamentDates(int year) {
        // Ngày bắt đầu ngẫu nhiên từ 10 đến 20/6
        int startDay = 10 + random.nextInt(11); // 10-20
        LocalDate startDate = LocalDate.of(year, 6, startDay);

        // Giải kéo dài 28 đến 32 ngày
        LocalDate endDate = startDate.plusDays(28 + random.nextInt(5)); // 28-32 ngày

        // Trả về mảng LocalDate
        return new LocalDate[]{startDate, endDate};
    }

    // Generate tournament name
    public static String generateTournamentName(int year) {
        return "FIFA World Cup " + year;
    }

    public static String getRandomVenue() {
        return VENUES[random.nextInt(VENUES.length)];
    }

    public static String getRandomReferee() {
        return REFEREE_NAMES[random.nextInt(REFEREE_NAMES.length)];
    }


    public static List<Team> generateTeams(int count) {
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

            boolean isHost = (i == 0); // First team is host

            Team team = new Team(countryName, region, coach, assistants, medicalStaff, isHost);
            teams.add(team);
        }

        return DataGenerator.generatePlayers(teams);
    }

    public static List<Team> generatePlayers(List<Team> teams) {
        for (Team team : teams) {
            List<Player> players = generatePlayers();
            team.setPlayers(players);
        }
        return teams;
    }

    private static List<Player> generatePlayers() {
        List<Player> players = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();

        for (int i = 0; i < 22; i++) {
            int jerseyNumber = i + 1;
            String name = getRandomName();
            
            // Đảm bảo tên không trùng lặp trong team
            while (usedNames.contains(name)) {
                name = getRandomName();
            }
            usedNames.add(name);
            
            String position = getRandomPosition();
            Player player = new Player(name, jerseyNumber, position);
            players.add(player);
        }

        return players;
    }

    public static String getRandomRegion() {
        return REGIONS[random.nextInt(REGIONS.length)];
    }

    public static String getRandomCoach() {
        return COACH_NAMES[random.nextInt(COACH_NAMES.length)];
    }

    public static List<String> generateAssistantCoaches() {
        List<String> assistants = new ArrayList<>();
        int count = random.nextInt(3) + 1; // 1-3 assistants

        for (int i = 0; i < count; i++) {
            assistants.add("Assistant " + getRandomName());
        }

        return assistants;
    }


    public static String getRandomName() {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        return firstName + " " + lastName;
    }

    // random trong một team
    public static String getRandomName(Team team) {
        String name = team.getPlayers().get(random.nextInt(team.getPlayers().size())).getName();
        return name;
    }

    public static String getRandomPosition() {
        return POSITIONS[random.nextInt(POSITIONS.length)];
    }

    public static List<Player> generateStartingPlayers(Team team) {
        List<Player> players = new ArrayList<>();

        // Use fixed jersey numbers 1-11 for starting players to avoid conflicts
        for (int i = 0; i < 11; i++) {
            int jerseyNumber = i + 1; // Jersey numbers 1, 2, 3, ..., 11
            String name = getRandomName(team);
            String position = getRandomPosition();

            Player player = new Player(name, jerseyNumber, position);
            players.add(player);
        }

        return players;
    }

    public static List<Player> generateSubstitutePlayers(Team team) {
        List<Player> players = new ArrayList<>();

        // Generate 11 substitute players with fixed jersey numbers 12-22
        for (int i = 0; i < 5; i++) {
            int jerseyNumber = i + 12; // Jersey numbers 12, 13, 14, ..., 22
            String name = getRandomName(team);
            String position = getRandomPosition();

            Player player = new Player(name, jerseyNumber, position);
            players.add(player);
        }

        return players;
    }
}