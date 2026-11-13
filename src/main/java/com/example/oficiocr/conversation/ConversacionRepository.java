package com.example.oficiocr.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversacionRepository
        extends JpaRepository<Conversacion, Long> {

    List<Conversacion> findByActivoTrue();
}
