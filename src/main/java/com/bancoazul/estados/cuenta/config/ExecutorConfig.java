package com.bancoazul.estados.cuenta.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Value("${banco.azul.estados.cuenta.thread.maxThreads}")
    private String maxThreads;

    /**
     * Returns an ExecutorService with a fixed thread pool based on the maxThreads parameter.
     *
     * @return the ExecutorService
     */
    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(Integer.parseInt(maxThreads));
    }
}
