package com.example.oficiocr.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByConversacionIdOrderByEnviadoEnAsc(Long conversacionId);

    List<Mensaje> findByConversacionIdAndActivoTrueOrderByEnviadoEnAsc(Long conversacionId);
}
