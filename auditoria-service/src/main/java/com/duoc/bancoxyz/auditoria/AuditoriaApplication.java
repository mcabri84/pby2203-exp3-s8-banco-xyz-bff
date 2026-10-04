package com.duoc.bancoxyz.auditoria;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
@SpringBootApplication
@EnableJms
public class AuditoriaApplication {
    public static void main(String[] args) { SpringApplication.run(AuditoriaApplication.class, args); }
}
