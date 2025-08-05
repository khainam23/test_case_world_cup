package com.worldcup.repository;

import com.worldcup.model.Goal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Goal entity
 * Tuân theo Repository Pattern và Dependency Inversion Principle
 */
public interface GoalRepository {
    
    /**
     * Lưu goal vào database
     */
    void save(Goal goal) throws SQLException;
    
    /**
     * Cập nhật goal trong database
     */
    void update(Goal goal) throws SQLException;
    
    /**
     * Tìm goal theo ID
     */
    Optional<Goal> findById(int id) throws SQLException;
    
    /**
     * Lấy tất cả goals của một match
     */
    List<Goal> findByMatch(int matchId) throws SQLException;
    
    /**
     * Lấy goals của một player
     */
    List<Goal> findByPlayer(int playerId) throws SQLException;
    
    /**
     * Lấy goals của một team trong tournament
     */
    List<Goal> findByTeamAndTournament(String teamName, int tournamentId) throws SQLException;
    
    /**
     * Lấy goals của một player trong tournament
     */
    List<Goal> findByPlayerAndTournament(String playerName, String teamName, int tournamentId) throws SQLException;
    
    /**
     * Đếm số goals của một player
     */
    int countGoalsByPlayer(String playerName, String teamName, int tournamentId) throws SQLException;
    
    /**
     * Đếm số goals của một team
     */
    int countGoalsByTeam(String teamName, int tournamentId) throws SQLException;
    
    /**
     * Xóa goal
     */
    void delete(int id) throws SQLException;
    
    /**
     * Kiểm tra goal có tồn tại không
     */
    boolean exists(int id) throws SQLException;
}