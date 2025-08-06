package com.worldcup.service;

import com.worldcup.manager.ObjectManager;
import com.worldcup.model.*;
import com.worldcup.repository.MatchRepository;
import com.worldcup.repository.TeamRepository;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 * OOP-based Service class cho Match operations
 * Thay thế các SQL operations bằng Repository pattern
 */
public class MatchService {

    private final ObjectManager objectManager;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    public MatchService(ObjectManager objectManager) {
        this.objectManager = objectManager;
        this.matchRepository = objectManager.getMatchRepository();
        this.teamRepository = objectManager.getTeamRepository();
    }

    /**
     * Tạo và lưu match
     */
    public Match createMatch(Team teamA, Team teamB, String venue, String referee, boolean isKnockout) throws Exception {
        Match match = new Match(teamA, teamB, venue, referee, isKnockout);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String matchDate = dateFormat.format(new java.util.Date());
        match.setMatchDate(matchDate);

        int matchId = matchRepository.save(match, venue, referee);
        match.setId(matchId);

        return match;
    }

    /**
     * Cập nhật kết quả trận đấu và lưu vào database
     */
    public void updateResult(Match match, int teamAScore, int teamBScore) throws Exception {
        // Cập nhật kết quả trong Match object
        match.updateMatchResult(teamAScore, teamBScore);

        // Cập nhật thống kê teams
        match.getTeamA().updateMatchStatistics(teamAScore, teamBScore);
        match.getTeamB().updateMatchStatistics(teamBScore, teamAScore);

        // Lưu changes vào database
        objectManager.updateMatchResult(match, teamAScore, teamBScore);
        match.getTeamA().update();
        match.getTeamB().update();
    }

    /**
     * Tạo match events (goals, cards, substitutions) sử dụng OOP approach
     */
    public void generateEvents(Match match, int teamAScore, int teamBScore) throws Exception {
        // Tạo goals cho team A
        generateGoalsForTeam(match, match.getTeamA(), teamAScore);

        // Tạo goals cho team B
        generateGoalsForTeam(match, match.getTeamB(), teamBScore);

        // Tạo cards và substitutions
        generateCardsAndSubstitutions(match);
    }

    /**
     * Tạo goals cho team sử dụng ObjectManager
     */
    private void generateGoalsForTeam(Match match, Team team, int goalCount) throws Exception {
        for (int i = 0; i < goalCount; i++) {
            List<Player> startingPlayers = team.getStartingPlayers();
            if (!startingPlayers.isEmpty()) {
                Player scorer = startingPlayers.get((int) (Math.random() * startingPlayers.size()));
                
                // Đảm bảo scorer có ID trước khi tạo goal
                if (scorer.getId() == 0) {
                    Optional<Integer> scorerId = objectManager.getPlayerRepository().getPlayerId(
                        scorer.getName(), team.getName(), team.getTournamentId());
                    if (scorerId.isPresent()) {
                        scorer.setId(scorerId.get());
                    }
                }
                
                int minute = (int) (Math.random() * 90) + 1;

                Goal goal = objectManager.createGoal(scorer, team, minute, match);
                match.addGoal(goal);
            }
        }
    }

    /**
     * Tạo cards và substitutions
     */
    private void generateCardsAndSubstitutions(Match match) throws Exception {
        // Generate cards for both teams
        generateCardsForTeam(match, match.getTeamA());
        generateCardsForTeam(match, match.getTeamB());

        // Generate substitutions for both teams
        generateSubstitutionsForTeam(match, match.getTeamA());
        generateSubstitutionsForTeam(match, match.getTeamB());
    }

    /**
     * Tạo cards cho team
     */
    private void generateCardsForTeam(Match match, Team team) throws Exception {
        // Lấy starting players từ Match object để đảm bảo đồng bộ
        List<Player> startingPlayers;
        if (team.equals(match.getTeamA())) {
            startingPlayers = match.getTeamA().getStartingPlayers();
        } else {
            startingPlayers = match.getTeamB().getStartingPlayers();
        }

        // Kiểm tra có players không
        if (startingPlayers.isEmpty()) {
            return;
        }

        // Yellow cards (more common)
        if (Math.random() < 0.3) { // 30% chance
            Player player = startingPlayers.get((int) (Math.random() * startingPlayers.size()));
            
            // Đảm bảo player có ID trước khi tạo card
            if (player.getId() == 0) {
                Optional<Integer> playerId = objectManager.getPlayerRepository().getPlayerId(
                    player.getName(), team.getName(), team.getTournamentId());
                if (playerId.isPresent()) {
                    player.setId(playerId.get());
                }
            }
            
            int minute = (int) (Math.random() * 90) + 1;

            Card yellowCard = objectManager.createCard(player, team, match, minute, Card.CardType.YELLOW);
            match.addCard(player, team, "YELLOW");
        }

        // Red cards (less common)
        if (Math.random() < 0.05) { // 5% chance
            Player player = startingPlayers.get((int) (Math.random() * startingPlayers.size()));
            
            // Đảm bảo player có ID trước khi tạo card
            if (player.getId() == 0) {
                Optional<Integer> playerId = objectManager.getPlayerRepository().getPlayerId(
                    player.getName(), team.getName(), team.getTournamentId());
                if (playerId.isPresent()) {
                    player.setId(playerId.get());
                }
            }
            
            int minute = (int) (Math.random() * 90) + 1;

            Card redCard = objectManager.createCard(player, team, match, minute, Card.CardType.RED);
            match.addCard(player, team, "RED");
        }
    }

    /**
     * Tạo substitutions cho team
     */
    private boolean generateSubstitutionsForTeam(Match match, Team team) throws Exception {
        if (Math.random() < 0.7) { // 70% chance of substitutions
            int maxSubstitutions = Math.min(3, team.getSubstitutePlayers().size()); // Tối đa 3 hoặc số substitute players
            int substitutionCount = (int) (Math.random() * maxSubstitutions) + 1; // 1 đến maxSubstitutions

            for (int i = 0; i < substitutionCount; i++) {
                // Lấy danh sách players cập nhật sau mỗi substitution
                List<Player> currentStartingPlayers = team.getStartingPlayers();
                List<Player> currentSubstitutePlayers = team.getSubstitutePlayers();

                // Kiểm tra còn players để thay không
                if (currentStartingPlayers.isEmpty() || currentSubstitutePlayers.isEmpty()) {
                    break; // Không còn players để thay
                }

                // Kiểm tra đã đạt giới hạn 3 substitutions chưa
                if (match.getSubstitutionCount(team) >= 3) {
                    break; // Đã đạt giới hạn
                }

                Player playerIn = currentSubstitutePlayers.get((int) (Math.random() * currentSubstitutePlayers.size()));
                Player playerOut = currentStartingPlayers.get((int) (Math.random() * currentStartingPlayers.size()));
                int minute = (int) (Math.random() * 45) + 45; // Second half

                try {
                    // Đảm bảo players có ID trước khi tạo substitution
                    if (playerIn.getId() == 0) {
                        // Lấy ID từ database
                        Optional<Integer> playerInId = objectManager.getPlayerRepository().getPlayerId(
                            playerIn.getName(), team.getName(), team.getTournamentId());
                        if (playerInId.isPresent()) {
                            playerIn.setId(playerInId.get());
                        }
                    }
                    
                    if (playerOut.getId() == 0) {
                        // Lấy ID từ database
                        Optional<Integer> playerOutId = objectManager.getPlayerRepository().getPlayerId(
                            playerOut.getName(), team.getName(), team.getTournamentId());
                        if (playerOutId.isPresent()) {
                            playerOut.setId(playerOutId.get());
                        }
                    }

                    objectManager.createSubstitution(match, team, playerIn, playerOut, minute);
                    team.setSubstitutionCount(team.getSubstitutionCount() + 1);
                    return true;
                } catch (Exception e) {
                    System.err.println("Lỗi tạo substitution: " + e.getMessage());
                    continue;
                }
            }
        }
        return false;
    }

    /**
     * Lấy match theo ID
     */
    public Optional<Match> getMatchById(int matchId) throws Exception {
        return matchRepository.findById(matchId);
    }

    /**
     * Lấy tất cả matches của tournament
     */
    public List<Match> getMatchesByTournament(int tournamentId) throws Exception {
        return matchRepository.findByTournament(tournamentId);
    }

    /**
     * Lấy matches theo team
     */
    public List<Match> getMatchesByTeam(String teamName, int tournamentId) throws Exception {
        return matchRepository.findByTeam(teamName, tournamentId);
    }
}