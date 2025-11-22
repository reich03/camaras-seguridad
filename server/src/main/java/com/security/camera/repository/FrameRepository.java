package com.security.camera.repository;

import com.security.camera.model.Frame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FrameRepository extends JpaRepository<Frame, Long> {
    
    List<Frame> findByVideoId(Long videoId);
    
    List<Frame> findByVideoIdOrderByFrameNumberAsc(Long videoId);
}
