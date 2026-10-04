package com.duoc.bancoxyz.banco.controller;

import com.duoc.bancoxyz.banco.model.CuentaAnual;
import com.duoc.bancoxyz.banco.model.Interes;
import com.duoc.bancoxyz.banco.model.Transaccion;
import com.duoc.bancoxyz.banco.service.CsvDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BancoDataController {

    private final CsvDataService service;

    public BancoDataController(CsvDataService service) {
        this.service = service;
    }

    @GetMapping("/cuentas")
    public List<CuentaAnual> cuentas(
            @RequestParam(required = false) String cuentaId) {
        return service.obtenerCuentas(cuentaId);
    }

    @GetMapping("/intereses")
    public List<Interes> intereses(
            @RequestParam(required = false) String cuentaId) {
        return service.obtenerIntereses(cuentaId);
    }

    @GetMapping("/transacciones")
    public List<Transaccion> transacciones() {
        return service.obtenerTransacciones();
    }
}
