package com.security.camera.repository;

import com.security.camera.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    List<User> findByIsActive(Boolean isActive);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cameras WHERE u.id = :id")
    Optional<User> findByIdWithCameras(Long id);
}
