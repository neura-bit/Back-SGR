package com.soprint.seguimiento_mensajeros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SeguimientoMensajerosApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeguimientoMensajerosApplication.class, args);
	}

}
