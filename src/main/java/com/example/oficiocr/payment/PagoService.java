package com.example.oficiocr.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;

    public PagoService(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    public List<Pago> obtenerTodos() {
        return pagoRepository.findAll();
    }

    public List<Pago> obtenerActivos() {
        return pagoRepository.findByActivoTrue();
    }

    public Pago obtenerPorId(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("No se encontró el pago con ID: " + id));
    }

    public List<Pago> obtenerPorSolicitud(Long solicitudId) {
        return pagoRepository.findBySolicitudId(solicitudId);
    }

    @Transactional
    public Pago registrar(Pago pago) {
        if (pago.getMonto() == null ||
                pago.getMonto().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero");
        }

        pago.setActivo(true);
        pago.setEsPrueba(true);

        return pagoRepository.save(pago);
    }

    @Transactional
    public Pago actualizar(Long id, Pago datos) {
        Pago pago = obtenerPorId(id);

        pago.setEstadoPagoId(datos.getEstadoPagoId());
        pago.setReferenciaExterna(datos.getReferenciaExterna());

        return pagoRepository.save(pago);
    }

    @Transactional
    public Pago activar(Long id) {
        Pago pago = obtenerPorId(id);
        pago.setActivo(true);
        return pagoRepository.save(pago);
    }

    @Transactional
    public void desactivar(Long id) {
        Pago pago = obtenerPorId(id);
        pago.setActivo(false);
        pagoRepository.save(pago);
    }
}
