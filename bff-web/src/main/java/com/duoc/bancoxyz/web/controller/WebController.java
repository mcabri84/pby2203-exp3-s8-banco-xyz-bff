package com.duoc.bancoxyz.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.duoc.bancoxyz.web.service.BancoClient;

@RestController
@RequestMapping("/web")
public class WebController {

    private final BancoClient banco;

    public WebController(BancoClient banco) {
        this.banco = banco;
    }

    @GetMapping("/cuentas")
    public JsonNode cuentas(
            @RequestParam(required = false) String cuentaId) {

        return banco.get("/api/cuentas", cuentaId);
    }

    @GetMapping("/intereses")
    public JsonNode intereses(
            @RequestParam(required = false) String cuentaId) {

        return banco.get("/api/intereses", cuentaId);
    }

    @GetMapping("/transacciones")
    public JsonNode transacciones() {
        return banco.get("/api/transacciones", null);
    }
}
