package com.duoc.bancoxyz.movil.controller;
import com.duoc.bancoxyz.movil.service.BancoNoDisponibleException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class ResilienceErrorHandler {
    @ExceptionHandler(BancoNoDisponibleException.class)
    ResponseEntity<Map<String, Object>> unavailable(BancoNoDisponibleException exception) {
        return ResponseEntity.status(503).header("Retry-After", "5").body(Map.of(
            "estado", "DEGRADADO", "servicio", "banco-api",
            "mensaje", "Servicio temporalmente no disponible. Intente nuevamente.",
            "reintentar", true));
    }
}
