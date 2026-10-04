package com.duoc.bancoxyz.auditoria.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransaccionListener {
    private static final Logger LOG = LoggerFactory.getLogger(TransaccionListener.class);
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public TransaccionListener(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }
    @JmsListener(destination = "banco.transacciones")
    @Transactional
    public void procesar(String contenido) throws Exception {
        var evento = mapper.readTree(contenido);
        String eventoId = UUID.fromString(evento.path("eventoId").asText()).toString();
        String transaccionId = evento.path("transaccion").path("id").asText();
        String tipo = evento.path("tipoEvento").asText();
        if (transaccionId.isBlank() || !"TRANSACCION_PUBLICADA".equals(tipo)) {
            throw new IllegalArgumentException("Evento de transaccion no valido");
        }
        try {
            jdbc.update("INSERT INTO eventos_procesados "
                + "(evento_id, transaccion_id, tipo_evento, contenido, procesado_en) VALUES (?, ?, ?, ?, ?)",
                eventoId, transaccionId, tipo, contenido, Instant.now().toString());
            LOG.info("EVENTO_PROCESADO eventoId={} transaccionId={}", eventoId, transaccionId);
        } catch (DuplicateKeyException duplicate) {
            LOG.info("EVENTO_DUPLICADO_IGNORADO eventoId={}", eventoId);
        }
    }
}
