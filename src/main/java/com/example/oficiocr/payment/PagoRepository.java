package com.example.oficiocr.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findBySolicitudId(Long solicitudId);

    List<Pago> findByEstadoPagoId(Long estadoPagoId);

    List<Pago> findByActivoTrue();
}
