package com.bancoazul.estados.cuenta.schedule;

import com.bancoazul.estados.cuenta.services.impl.MonitoreoCarpetaServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class MetadataProcessingScheduler {

    private final MonitoreoCarpetaServiceImpl monitoreoCarpetaService;

    @Scheduled(cron = "0 * 17 * * ?")
    public void processMetadataScheduled() {
        Path baseDir = Paths.get("C:\\DOCS_TEST\\ECT");
        String txtLocalPath = "C:\\DOCS_TEST\\ECT\\metadata.txt";
        String tipoEstadoCuenta = "EC01";

        monitoreoCarpetaService.processMetadata(baseDir, txtLocalPath, tipoEstadoCuenta);
    }
}