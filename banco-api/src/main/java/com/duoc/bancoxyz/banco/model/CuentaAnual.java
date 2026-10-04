package com.duoc.bancoxyz.banco.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CuentaAnual(
        @JsonProperty("cuenta_id") String cuentaId,
        String fecha,
        String transaccion,
        String monto,
        String descripcion
) {
}
