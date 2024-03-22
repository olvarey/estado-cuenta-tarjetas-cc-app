package com.bancoazul.estados.cuenta;

import com.bancoazul.estados.cuenta.services.MonitoreoCarpetaService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * EstadosCuentaTarjetasCCApplication: Main class responsable for monitoring
 * CREATE_EVENT changes in a specific folder in file system then index PDF
 * documents through Docuware API
 *
 * @author Melvin Reyes
 */
@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class EstadosCuentaTarjetasCCApplication implements CommandLineRunner {

    private static final Logger LOGGER = LogManager.getLogger(EstadosCuentaTarjetasCCApplication.class);
    private final MonitoreoCarpetaService monitoreoCarpetaService;

    /**
     * This method starts the application by running the Spring Boot application.
     * It logs the starting and finishing of the application.
     *
     * @param args The command line arguments passed to the application
     */
    public static void main(String[] args) {
        LOGGER.debug("APPLICATION STARTED");
        SpringApplication.run(EstadosCuentaTarjetasCCApplication.class, args);
        LOGGER.debug("APPLICATION FINISHED");
    }

    /**
     * This method runs the directory monitoring service.
     *
     * @param args Command line arguments
     * @throws Exception if an error occurs during directory monitoring
     */
    @Override
    public void run(String... args) throws Exception {
        //monitoreoCarpetaService.watchDirectory();
    }
}
