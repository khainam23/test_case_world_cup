package com.worldcup.repository;

import com.worldcup.model.Team;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Team entity
 * Tuân theo Repository Pattern và Dependency Inversion Principle
 */
public interface TeamRepository {
    
    /**
     * Lưu team vào database
     */
    void save(Team team) throws SQLException;
    
    /**
     * Cập nhật team trong database
     */
    void update(Team team) throws SQLException;
    
    /**
     * Tìm team theo ID
     */
    Optional<Team> findById(int id) throws SQLException;
    
    /**
     * Tìm team theo tên và tournament ID
     */
    Optional<Team> findByNameAndTournament(String name, int tournamentId) throws SQLException;
    
    /**
     * Lấy tất cả teams của một tournament
     */
    List<Team> findByTournament(int tournamentId) throws SQLException;
    
    /**
     * Lấy teams theo group và tournament
     */
    List<Team> findByGroupAndTournament(String groupName, int tournamentId) throws SQLException;
    
    /**
     * Lấy teams với đầy đủ players
     */
    List<Team> findByTournamentWithPlayers(int tournamentId) throws SQLException;
    
    /**
     * Lấy teams theo group với đầy đủ players
     */
    List<Team> findByGroupAndTournamentWithPlayers(String groupName, int tournamentId) throws SQLException;
    
    /**
     * Xóa team
     */
    void delete(int id) throws SQLException;
    
    /**
     * Kiểm tra team có tồn tại không
     */
    boolean exists(int id) throws SQLException;
    
    /**
     * Lấy ID của team theo tên và tournament
     */
    Optional<Integer> getTeamId(String teamName, int tournamentId) throws SQLException;
}