package com.duoc.bancoxyz.auditoria.controller;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auditoria/eventos")
public class AuditoriaController {
    private final JdbcTemplate jdbc;
    public AuditoriaController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    List<Map<String, Object>> listar() {
        return consultar("SELECT * FROM eventos_procesados ORDER BY procesado_en");
    }
    @GetMapping("/{id}")
    Map<String, Object> obtener(@PathVariable String id) {
        var resultados = consultar("SELECT * FROM eventos_procesados WHERE evento_id = ?", id);
        if (resultados.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento aun no procesado");
        }
        return resultados.get(0);
    }
    private List<Map<String, Object>> consultar(String sql, Object... args) {
        return jdbc.query(sql, (rs, row) -> Map.<String, Object>of(
            "eventoId", rs.getString("evento_id"),
            "transaccionId", rs.getString("transaccion_id"),
            "tipoEvento", rs.getString("tipo_evento"),
            "procesadoEn", rs.getString("procesado_en"),
            "contenido", rs.getString("contenido")), args);
    }
}
