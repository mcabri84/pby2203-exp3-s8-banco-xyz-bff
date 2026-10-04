package com.duoc.bancoxyz.banco.controller;
import com.duoc.bancoxyz.banco.service.CsvDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionEventoController {
    private final CsvDataService datos;
    private final JmsTemplate jms;
    private final ObjectMapper mapper;
    public TransaccionEventoController(CsvDataService datos, JmsTemplate jms, ObjectMapper mapper) {
        this.datos = datos;
        this.jms = jms;
        this.mapper = mapper;
        this.jms.setExplicitQosEnabled(true);
        this.jms.setDeliveryPersistent(true);
    }
    @PostMapping("/{id}/eventos")
    ResponseEntity<Map<String, Object>> publicar(@PathVariable String id)
            throws JsonProcessingException {
        var transaccion = datos.obtenerTransacciones().stream()
            .filter(item -> id.equals(item.id())).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaccion inexistente"));
        String eventoId = UUID.randomUUID().toString();
        var evento = Map.of("eventoId", eventoId, "tipoEvento", "TRANSACCION_PUBLICADA",
            "emitidoEn", Instant.now().toString(), "transaccion", transaccion);
        // TextMessage JSON: no se deserializan objetos Java arbitrarios.
        jms.convertAndSend("banco.transacciones", mapper.writeValueAsString(evento));
        return ResponseEntity.accepted().body(Map.of(
            "eventoId", eventoId, "transaccionId", id, "estado", "EN_COLA"));
    }
    @ExceptionHandler(JmsException.class)
    ResponseEntity<Map<String, String>> brokerUnavailable(JmsException exception) {
        return ResponseEntity.status(503).body(Map.of("estado", "NO_ENCOLADO",
            "mensaje", "No fue posible publicar el evento. Intente nuevamente."));
    }
}
