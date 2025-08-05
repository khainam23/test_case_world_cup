package com.worldcup.repository;

import com.worldcup.model.Player;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Player entity
 * Tuân theo Repository Pattern và Dependency Inversion Principle
 */
public interface PlayerRepository {
    
    /**
     * Lưu player vào database
     */
    void save(Player player, int teamId) throws SQLException;
    
    /**
     * Cập nhật player trong database
     */
    void update(Player player) throws SQLException;
    
    /**
     * Tìm player theo ID
     */
    Optional<Player> findById(int id) throws SQLException;
    
    /**
     * Tìm player theo tên và team
     */
    Optional<Player> findByNameAndTeam(String playerName, String teamName, int tournamentId) throws SQLException;
    
    /**
     * Lấy tất cả players của một team
     */
    List<Player> findByTeam(int teamId) throws SQLException;
    
    /**
     * Lấy players theo team name và tournament
     */
    List<Player> findByTeamNameAndTournament(String teamName, int tournamentId) throws SQLException;
    
    /**
     * Lấy starting players của một team
     */
    List<Player> findStartingPlayersByTeam(int teamId) throws SQLException;
    
    /**
     * Lấy substitute players của một team
     */
    List<Player> findSubstitutePlayersByTeam(int teamId) throws SQLException;
    
    /**
     * Cập nhật số bàn thắng của player
     */
    void updateGoals(Player player, String teamName, int tournamentId) throws SQLException;
    
    /**
     * Cập nhật thẻ phạt của player
     */
    void updateCards(Player player, String teamName, int tournamentId, String cardType) throws SQLException;
    
    /**
     * Xóa player
     */
    void delete(int id) throws SQLException;
    
    /**
     * Kiểm tra player có tồn tại không
     */
    boolean exists(int id) throws SQLException;
    
    /**
     * Lấy ID của player
     */
    Optional<Integer> getPlayerId(String playerName, String teamName, int tournamentId) throws SQLException;
}