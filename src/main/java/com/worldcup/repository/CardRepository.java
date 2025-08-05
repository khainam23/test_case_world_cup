package com.worldcup.repository;

import com.worldcup.model.Card;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Card entity
 * Tuân theo Repository Pattern và Dependency Inversion Principle
 */
public interface CardRepository {
    
    /**
     * Lưu card vào database
     */
    void save(Card card) throws SQLException;
    
    /**
     * Cập nhật card trong database
     */
    void update(Card card) throws SQLException;
    
    /**
     * Tìm card theo ID
     */
    Optional<Card> findById(int id) throws SQLException;
    
    /**
     * Lấy tất cả cards của một match
     */
    List<Card> findByMatch(int matchId) throws SQLException;
    
    /**
     * Lấy cards của một player
     */
    List<Card> findByPlayer(int playerId) throws SQLException;
    
    /**
     * Lấy cards của một team trong tournament
     */
    List<Card> findByTeamAndTournament(String teamName, int tournamentId) throws SQLException;
    
    /**
     * Lấy cards theo loại (YELLOW, RED)
     */
    List<Card> findByTeamAndTournamentAndType(String teamName, int tournamentId, String cardType) throws SQLException;
    
    /**
     * Đếm số cards của một team theo loại
     */
    int countCardsByTeamAndType(String teamName, int tournamentId, String cardType) throws SQLException;
    
    /**
     * Đếm số cards của một player theo loại
     */
    int countCardsByPlayerAndType(String playerName, String teamName, int tournamentId, String cardType) throws SQLException;
    
    /**
     * Xóa card
     */
    void delete(int id) throws SQLException;
    
    /**
     * Kiểm tra card có tồn tại không
     */
    boolean exists(int id) throws SQLException;
}