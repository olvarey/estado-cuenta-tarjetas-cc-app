package com.bancoazul.estados.cuenta.services;

import com.bancoazul.estados.cuenta.pojos.Documento;
import com.bancoazul.estados.cuenta.pojos.Indice;
import com.bancoazul.estados.cuenta.pojos.Registro;
import com.bancoazul.estados.cuenta.utils.PdfABase64ConverterService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service class to monitor CREATE events in a specific folder
 *
 * @author Melvin Reyes
 */
@Service
@RequiredArgsConstructor
public class MonitoreoCarpetaServiceImpl implements MonitoreoCarpetaService {

    private static final Logger LOGGER = LogManager.getLogger(MonitoreoCarpetaServiceImpl.class);
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
            if (folderExist(directoryPathEct) && folderExist(directoryPathEcc)) {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path pathEtc = Paths.get(directoryPathEct);
                Path pathEcc = Paths.get(directoryPathEcc);
                pathEtc.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                pathEcc.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                WatchKey key;
                while ((key = watchService.take()) != null) {
                    Path baseDir = (Path) key.watchable();
                    String tipoEstadoCuenta = fetchTipoEstadoCuenta(baseDir);
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Thread.sleep(3000);
                        String txtLocalPath = baseDir + File.separator + event.context();
                        if (checkTxtIsValid(txtLocalPath)) {
                            List<Documento> documentos = fetchTxtMetadata(baseDir, txtLocalPath, tipoEstadoCuenta);
                            if (!documentos.isEmpty()) {
                                for (Documento doc : documentos) {
                                    if (!docuwareService.documentExist(doc)) {
                                        LOGGER.info("Respuesta API: {}", docuwareService.indexDocument(doc));
                                    } else {
                                        LOGGER.info("El documento ya está indexado.");
                                    }
                                }
                            }
                        } else {
                            LOGGER.info("metadata TXT file not found: {}", txtLocalPath);
                        }
                    }
                    key.reset();
                }
            } else {
                LOGGER.info("Invalid folder path!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Documento composeDocumento(Path baseDir, Registro registro, String tipoEstadoCuenta) {
        Documento newDocumento = new Documento();
        newDocumento.setIdArchivador(idArchivador);
        newDocumento.setNombreArchivo(registro.getNombreArchivo());
        newDocumento.setDocumentoBase64(pdfABase64ConverterService.convertToBase64(new File(baseDir + File.separator + registro.getNombreArchivo())));
        Indice numeroTarjetaCuenta = new Indice("NUMERO_DE_CUENTA", registro.getTarjetaCuenta());
        Indice cliente = new Indice("CLIENTE", registro.getCliente());
        Indice anio = new Indice("ANIO", registro.getAnio());
        Indice mes = new Indice("MES", registro.getMes());
        Indice tipoEstadoDeCuenta = new Indice("TIPO_ESTADO_DE_CUENTA", tipoEstadoCuenta);
        newDocumento.getIndices().add(numeroTarjetaCuenta);
        newDocumento.getIndices().add(cliente);
        newDocumento.getIndices().add(anio);
        newDocumento.getIndices().add(mes);
        newDocumento.getIndices().add(tipoEstadoDeCuenta);
        return newDocumento;
    }

    private List<Documento> fetchTxtMetadata(Path baseDir, String txtFilePath, String tipoEstadoCuenta) {
        List<Documento> documentoList = new ArrayList<>();
        // Specify the path to your text file
        Path filePath = Paths.get(txtFilePath);
        try (Stream<String> stream = Files.lines(filePath)) {
            // Read each line from the file
            stream.forEach(txtLine -> {
                Registro newRegistro = composeRegistro(txtLine);
                if (newRegistro != null) {
                    if (pdfFileExist(baseDir + File.separator + newRegistro.getNombreArchivo())) {
                        documentoList.add(composeDocumento(baseDir, newRegistro, tipoEstadoCuenta));
                    } else {
                        LOGGER.info("Archivo: {} no existe!", newRegistro.getNombreArchivo());
                    }
                } else {
                    LOGGER.info("Linea malformada!");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return documentoList;
    }

    private boolean pdfFileExist(String pdfPath) {
        File newPdf = new File(pdfPath);
        return newPdf.exists();
    }

    public boolean folderExist(String folderPath) {
        File folder = new File(folderPath);
        return folder.exists() && folder.isDirectory();
    }

    public String fetchTipoEstadoCuenta(Path baseDir) {
        String tipoEstadoCuenta = "";
        if (directoryPathEct.equals(baseDir.toString())) {
            tipoEstadoCuenta = "EC01";
        }
        if (directoryPathEcc.equals(baseDir.toString())) {
            tipoEstadoCuenta = "EC02";
        }
        return tipoEstadoCuenta;
    }

    public boolean checkTxtIsValid(String txtLocalPath) {
        File txtFile = new File(txtLocalPath);
        boolean fileExist = txtFile.exists();
        boolean isNamedProperly = metaDataFileName.equals(txtFile.getName().toLowerCase());
        boolean isNotEmpty = txtFile.length() > 0;
        boolean isTxtfile = txtFile.getName().toLowerCase().endsWith(".txt");
        return fileExist && isNamedProperly && isNotEmpty && isTxtfile;
    }

    public Registro composeRegistro(String txtLine) {
        Registro registro = new Registro();
        String[] fieldsArray = txtLine.split(",");
        List<String> fields = Arrays.asList(fieldsArray);
        if (fields.size() == 4) {
            registro.setNombreArchivo(fields.get(0));
            registro.setAnio(fields.get(1).substring(0, 4));
            registro.setMes(fields.get(1).substring(4, 6));
            registro.setTarjetaCuenta(fields.get(2));
            registro.setCliente(fields.get(3));
            return registro;
        } else {
            LOGGER.info("Text line hasn't all fields needed");
            return null;
        }
    }
}
