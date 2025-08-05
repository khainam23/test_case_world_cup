package com.worldcup.repository;

import com.worldcup.model.Match;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Match entity
 * Tuân theo Repository Pattern và Dependency Inversion Principle
 */
public interface MatchRepository {
    
    /**
     * Lưu match vào database
     */
    int save(Match match, String venue, String referee) throws SQLException;
    
    /**
     * Cập nhật match trong database
     */
    void update(Match match) throws SQLException;
    
    /**
     * Tìm match theo ID
     */
    Optional<Match> findById(int id) throws SQLException;
    
    /**
     * Lấy tất cả matches của một tournament
     */
    List<Match> findByTournament(int tournamentId) throws SQLException;
    
    /**
     * Lấy matches theo loại (GROUP, KNOCKOUT)
     */
    List<Match> findByTournamentAndType(int tournamentId, String matchType) throws SQLException;
    
    /**
     * Lấy matches của một team
     */
    List<Match> findByTeam(String teamName, int tournamentId) throws SQLException;
    
    /**
     * Lấy group stage matches của một team
     */
    List<Match> findGroupStageMatchesByTeam(String teamName, int tournamentId) throws SQLException;
    
    /**
     * Xóa match
     */
    void delete(int id) throws SQLException;
    
    /**
     * Kiểm tra match có tồn tại không
     */
    boolean exists(int id) throws SQLException;
}