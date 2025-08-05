package com.worldcup.repository;

import com.worldcup.model.Substitution;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Substitution entity
 * Tuân theo Repository Pattern và Dependency Inversion Principle
 */
public interface SubstitutionRepository {
    
    /**
     * Lưu substitution vào database
     */
    void save(Substitution substitution) throws SQLException;
    
    /**
     * Cập nhật substitution trong database
     */
    void update(Substitution substitution) throws SQLException;
    
    /**
     * Tìm substitution theo ID
     */
    Optional<Substitution> findById(int id) throws SQLException;
    
    /**
     * Lấy tất cả substitutions của một match
     */
    List<Substitution> findByMatch(int matchId) throws SQLException;
    
    /**
     * Lấy substitutions của một team trong tournament
     */
    List<Substitution> findByTeamAndTournament(String teamName, int tournamentId) throws SQLException;
    
    /**
     * Đếm số substitutions của một team
     */
    int countSubstitutionsByTeam(String teamName, int tournamentId) throws SQLException;
    
    /**
     * Xóa substitution
     */
    void delete(int id) throws SQLException;
    
    /**
     * Kiểm tra substitution có tồn tại không
     */
    boolean exists(int id) throws SQLException;
}