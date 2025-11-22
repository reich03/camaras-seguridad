package com.security.camera.repository;

import com.security.camera.model.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CameraRepository extends JpaRepository<Camera, Long> {
    
    List<Camera> findByUserId(Long userId);
    
    List<Camera> findByUserIdAndIsActive(Long userId, Boolean isActive);
    
    @Query("SELECT COUNT(c) FROM Camera c WHERE c.user.id = :userId AND c.isActive = true")
    long countActiveCamerasByUserId(Long userId);
    
    boolean existsByCameraNameAndUserId(String cameraName, Long userId);
}
