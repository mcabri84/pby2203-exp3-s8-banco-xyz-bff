package com.duoc.bancoxyz.banco.model;

public record Transaccion(
        String id,
        String fecha,
        String monto,
        String tipo
) {
}
