package com.duoc.bancoxyz.cajeros.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.duoc.bancoxyz.cajeros.service.BancoClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cajeros")
public class CajeroController {

    private final BancoClient banco;

    public CajeroController(BancoClient banco) {
        this.banco = banco;
    }

    @GetMapping("/saldo/{cuentaId}")
    public List<Map<String, String>> saldo(
            @PathVariable String cuentaId) {

        JsonNode datos = banco.get("/api/intereses", cuentaId);

        List<Map<String, String>> respuesta = new ArrayList<>();

        if (datos != null && datos.isArray()) {
            for (JsonNode dato : datos) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("cuentaId", dato.path("cuenta_id").asText());
                item.put("saldo", dato.path("saldo").asText());
                item.put("tipo", dato.path("tipo").asText());
                respuesta.add(item);
            }
        }

        return respuesta;
    }

    @GetMapping("/retiros/{cuentaId}")
    public List<Map<String, String>> retiros(
            @PathVariable String cuentaId) {

        JsonNode datos = banco.get("/api/cuentas", cuentaId);

        List<Map<String, String>> respuesta = new ArrayList<>();

        if (datos != null && datos.isArray()) {
            for (JsonNode dato : datos) {
                if ("retiro".equalsIgnoreCase(
                        dato.path("transaccion").asText())) {

                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("cuentaId", dato.path("cuenta_id").asText());
                    item.put("fecha", dato.path("fecha").asText());
                    item.put("monto", dato.path("monto").asText());
                    item.put("descripcion",
                            dato.path("descripcion").asText());
                    respuesta.add(item);
                }
            }
        }

        return respuesta;
    }
}
