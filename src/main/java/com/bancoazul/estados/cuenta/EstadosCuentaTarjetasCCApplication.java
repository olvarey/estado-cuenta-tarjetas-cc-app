package com.bancoazul.estados.cuenta;

import com.bancoazul.estados.cuenta.services.MonitoreoCarpetaService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EstadosCuentaTarjetasCCApplication: Main class responsable for monitoring
 * CREATE_EVENT changes in a specific folder in file system then index PDF
 * documents through Docuware API
 *
 * @author Melvin Reyes
 */
@SpringBootApplication
@RequiredArgsConstructor
public class EstadosCuentaTarjetasCCApplication implements CommandLineRunner {

    private static final Logger LOGGER = LogManager.getLogger(EstadosCuentaTarjetasCCApplication.class);
    private final MonitoreoCarpetaService monitoreoCarpetaService;

    public static void main(String[] args) {
        LOGGER.debug("STARTING THE APPLICATION");
        SpringApplication.run(EstadosCuentaTarjetasCCApplication.class, args);
        LOGGER.debug("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) throws Exception {
        monitoreoCarpetaService.watchDirectory();
    }
}
