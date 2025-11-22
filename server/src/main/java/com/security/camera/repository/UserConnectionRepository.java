package com.security.camera.repository;

import com.security.camera.model.UserConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {
    
    List<UserConnection> findByUserId(Long userId);
    
    @Query("SELECT uc FROM UserConnection uc WHERE uc.user.id = :userId AND uc.disconnectedAt IS NULL")
    List<UserConnection> findActiveConnectionsByUserId(Long userId);
    
    @Query("SELECT COUNT(uc) FROM UserConnection uc WHERE uc.user.id = :userId AND uc.disconnectedAt IS NULL")
    long countActiveConnectionsByUserId(Long userId);
    
    @Query("SELECT uc FROM UserConnection uc WHERE uc.disconnectedAt IS NULL")
    List<UserConnection> findAllActiveConnections();
}
