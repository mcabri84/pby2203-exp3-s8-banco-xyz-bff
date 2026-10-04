package com.duoc.bancoxyz.banco.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Interes(
        @JsonProperty("cuenta_id") String cuentaId,
        String nombre,
        String saldo,
        String edad,
        String tipo
) {
}
