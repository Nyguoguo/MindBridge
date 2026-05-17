package com.mindbridge.common.repository;

import com.mindbridge.common.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
