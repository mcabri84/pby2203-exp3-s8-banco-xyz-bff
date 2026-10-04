package com.duoc.bancoxyz.banco.service;

import com.duoc.bancoxyz.banco.model.CuentaAnual;
import com.duoc.bancoxyz.banco.model.Interes;
import com.duoc.bancoxyz.banco.model.Transaccion;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class CsvDataService {

    private final CsvMapper csvMapper = new CsvMapper();

    private List<CuentaAnual> cuentas;
    private List<Interes> intereses;
    private List<Transaccion> transacciones;

    @PostConstruct
    public void cargarDatos() {
        cuentas = cargar("data/cuentas_anuales.csv", CuentaAnual.class);
        intereses = cargar("data/intereses.csv", Interes.class);
        transacciones = cargar("data/transacciones.csv", Transaccion.class);

        System.out.println("CSV cargados:");
        System.out.println("cuentas_anuales: " + cuentas.size());
        System.out.println("intereses: " + intereses.size());
        System.out.println("transacciones: " + transacciones.size());
    }

    private <T> List<T> cargar(String archivo, Class<T> tipo) {
        try (InputStream input = new ClassPathResource(archivo).getInputStream()) {
            CsvSchema schema = CsvSchema.emptySchema().withHeader();

            return csvMapper
                    .readerFor(tipo)
                    .with(schema)
                    .<T>readValues(input)
                    .readAll();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error cargando " + archivo + ": " + e.getMessage(), e
            );
        }
    }

    public List<CuentaAnual> obtenerCuentas(String cuentaId) {
        if (cuentaId == null || cuentaId.isBlank()) {
            return cuentas;
        }

        return cuentas.stream()
                .filter(c -> cuentaId.equals(c.cuentaId()))
                .toList();
    }

    public List<Interes> obtenerIntereses(String cuentaId) {
        if (cuentaId == null || cuentaId.isBlank()) {
            return intereses;
        }

        return intereses.stream()
                .filter(i -> cuentaId.equals(i.cuentaId()))
                .toList();
    }

    public List<Transaccion> obtenerTransacciones() {
        return transacciones;
    }
}
