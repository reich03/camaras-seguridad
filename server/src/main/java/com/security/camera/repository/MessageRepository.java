package com.security.camera.repository;

import com.security.camera.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByUserIdOrderBySentAtDesc(Long userId);
    
    List<Message> findByCameraIpOrderBySentAtDesc(String cameraIp);
}
