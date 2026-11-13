package com.example.oficiocr.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecomendacionIARepository
        extends JpaRepository<RecomendacionIA, Long> {

    List<RecomendacionIA> findByUsuarioIdAndActivoTrueOrderByCreadoEnDesc(
            Long usuarioId
    );

    List<RecomendacionIA> findByActivoTrueOrderByCreadoEnDesc();
}
