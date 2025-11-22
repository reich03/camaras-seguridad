package com.security.camera.repository;

import com.security.camera.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    
    List<Video> findByCameraId(Long cameraId);
    
    @Query("SELECT v FROM Video v WHERE v.camera.user.id = :userId ORDER BY v.uploadedAt DESC")
    List<Video> findByUserId(Long userId);
    
    List<Video> findByUploadedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(v) FROM Video v WHERE v.camera.id = :cameraId")
    long countByCameraId(Long cameraId);
    
    @Query("SELECT v FROM Video v JOIN FETCH v.frames WHERE v.id = :videoId")
    Video findByIdWithFrames(Long videoId);
}
