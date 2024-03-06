package com.bancoazul.estados.cuenta;

import com.bancoazul.estados.cuenta.services.MonitoreoCarpetaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * EstadosCuentaTarjetasCCApplication: Main class responsable for monitoring
 * CREATE_EVENT changes in a specific folder in file system then index PDF
 * documents through Docuware API
 *
 * @author Melvin Reyes
 */
@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class EstadosCuentaTarjetasCCApplication implements CommandLineRunner {

    private final MonitoreoCarpetaService monitoreoCarpetaService;

    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("STARTING THE APPLICATION");
        SpringApplication.run(EstadosCuentaTarjetasCCApplication.class, args);
        log.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) throws Exception {
        monitoreoCarpetaService.watchDirectory();
    }
}
