package com.bancoazul.estados.cuenta.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bancoazul.estados.cuenta.pojos.Documento;
import com.bancoazul.estados.cuenta.pojos.Indice;
import com.bancoazul.estados.cuenta.utils.PdfABase64ConverterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class to monitor CREATE events in a specific folder
 *
 * @author Melvin Reyes
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitoreoCarpetaServiceImpl implements MonitoreoCarpetaService {

    private final DocuwareService docuwareService;
    private final PdfABase64ConverterService pdfABase64ConverterService;

    @Value("${banco.azul.estados.cuenta.directorio.ect}")
    private String directoryPathEct;
    @Value("${banco.azul.estados.cuenta.directorio.ecc}")
    private String directoryPathEcc;
    @Value("${banco.azul.estados.cuenta.directorio.metadata}")
    private String metaDataFileName;
    @Value("${banco.azul.estados.cuenta.docuware.idArchivador}")
    private String idArchivador;

    @Override
    public void watchDirectory() {
        try {
            File folderEct = new File(directoryPathEct);
            File folderEcc = new File(directoryPathEcc);
            if (folderEct.isDirectory() && folderEcc.isDirectory()) {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path pathEtc = Paths.get(directoryPathEct);
                Path pathEcc = Paths.get(directoryPathEcc);
                pathEtc.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                pathEcc.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                WatchKey key;
                while ((key = watchService.take()) != null) {
                    Path baseDir = (Path) key.watchable();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        String tipoEstadoCuenta = directoryPathEct.equals(baseDir.toString()) ? "ECT" : directoryPathEcc.equals(baseDir.toString()) ? "ECC" : "";
                        String txtLocalPath = baseDir + File.separator + event.context();
                        File txtFile = new File(txtLocalPath);
                        boolean isValid = metaDataFileName.equals(txtFile.getName().toLowerCase());
                        boolean isNotEmpty = txtFile.length() > 0;
                        if (txtFile.exists() && isValid && isNotEmpty) {
                            Thread.sleep(3000);
                            List<Documento> documentos = readMetadata(baseDir, txtLocalPath, tipoEstadoCuenta);
                        } else {
                            log.info("metadata TXT file not found: " + txtLocalPath);
                        }
                    }
                    key.reset();
                }
            } else {
                log.info("Invalid folder path!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Documento composeDocumento(Path baseDir, String txtLine, String tipoEstadoCuenta) {
        String[] fieldsArray = txtLine.split(",");
        List<String> fields = Arrays.asList(fieldsArray);
        log.info("Longitud de la linea leida: " + fields.size());
        if (fields.size() == 4) {
            String nombreArchivo = fields.get(0);
            String periodo = fields.get(1);
            String tarjetaCuenta = fields.get(2);
            String cliente = fields.get(3);
        } else {

        }

        Documento newDocumento = new Documento();
        newDocumento.setIdArchivador(idArchivador);
        newDocumento.setNombreArchivo(fields.get(0));
        // newDocumento.setDocumentoBase64(pdfABase64ConverterService.convertToBase64(txtFile));
        newDocumento.setDocumentoBase64(fields.get(1));
        Indice numeroUnicoCliente = new Indice("NUMERO_UNICO_DE_CLIENTE", "010000001022024");
        Indice codigoDocumento = new Indice("CODIGO_DE_DOCUMENTO", "AN026");
        newDocumento.getIndices().add(numeroUnicoCliente);
        newDocumento.getIndices().add(codigoDocumento);
        return newDocumento;
    }

    private List<Documento> readMetadata(Path baseDir, String txtFilePath, String tipoEstadoCuenta) {
        List<Documento> documentoList = new ArrayList<>();
        // Specify the path to your text file
        Path filePath = Paths.get(txtFilePath);
        try (Stream<String> stream = Files.lines(filePath)) {
            // Read each line from the file and print it to the console
            stream.forEach(line -> {
                documentoList.add(composeDocumento(baseDir, line, tipoEstadoCuenta));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return documentoList;
    }

    private void readPDF() {

    }
}
