package com.duoc.bancoxyz.movil.service;
public class BancoNoDisponibleException extends RuntimeException {
    public BancoNoDisponibleException(Throwable cause) {
        super("El servicio bancario no esta disponible temporalmente", cause);
    }
}
