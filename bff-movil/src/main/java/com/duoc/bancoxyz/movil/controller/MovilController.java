package com.duoc.bancoxyz.movil.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.duoc.bancoxyz.movil.service.BancoClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movil")
public class MovilController {

    private final BancoClient banco;

    public MovilController(BancoClient banco) {
        this.banco = banco;
    }

    @GetMapping("/cuentas/{cuentaId}")
    public List<Map<String, String>> cuenta(
            @PathVariable String cuentaId) {

        JsonNode datos = banco.get("/api/intereses", cuentaId);

        List<Map<String, String>> respuesta = new ArrayList<>();

        if (datos != null && datos.isArray()) {
            for (JsonNode dato : datos) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("cuentaId", dato.path("cuenta_id").asText());
                item.put("nombre", dato.path("nombre").asText());
                item.put("saldo", dato.path("saldo").asText());
                item.put("tipo", dato.path("tipo").asText());
                respuesta.add(item);
            }
        }

        return respuesta;
    }

    @GetMapping("/transacciones")
    public List<Map<String, String>> transacciones() {

        JsonNode datos = banco.get("/api/transacciones", null);

        List<Map<String, String>> respuesta = new ArrayList<>();

        if (datos != null && datos.isArray()) {
            for (JsonNode dato : datos) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("id", dato.path("id").asText());
                item.put("monto", dato.path("monto").asText());
                item.put("tipo", dato.path("tipo").asText());
                respuesta.add(item);
            }
        }

        return respuesta;
    }
}
