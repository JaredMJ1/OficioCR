package com.example.oficiocr.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }


    @GetMapping
    public List<Pago> obtenerTodos() {
        return pagoService.obtenerTodos();
    }


    @GetMapping("/activos")
    public List<Pago> obtenerActivos() {
        return pagoService.obtenerActivos();
    }


    @GetMapping("/{id}")
    public Pago obtenerPorId(@PathVariable Long id) {
        return pagoService.obtenerPorId(id);
    }


    @GetMapping("/solicitud/{solicitudId}")
    public List<Pago> obtenerPorSolicitud(
            @PathVariable Long solicitudId) {
        return pagoService.obtenerPorSolicitud(solicitudId);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pago registrar(@RequestBody Pago pago) {
        return pagoService.registrar(pago);
    }


    @PutMapping("/{id}")
    public Pago actualizar(
            @PathVariable Long id,
            @RequestBody Pago pago) {
        return pagoService.actualizar(id, pago);
    }

    @PatchMapping("/{id}/activar")
    public Pago activar(@PathVariable Long id) {
        return pagoService.activar(id);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        pagoService.desactivar(id);
    }
}
