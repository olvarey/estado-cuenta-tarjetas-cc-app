package com.bancoazul.estados.cuenta.services.impl;

import com.bancoazul.estados.cuenta.pojos.Documento;
import com.bancoazul.estados.cuenta.pojos.Indice;
import com.bancoazul.estados.cuenta.pojos.Registro;
import com.bancoazul.estados.cuenta.services.DocuwareService;
import com.bancoazul.estados.cuenta.services.MonitoreoCarpetaService;
import com.bancoazul.estados.cuenta.utils.PdfABase64ConverterService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
    private final ExecutorService executor;

    @Value("${banco.azul.estados.cuenta.directorio.ect}")
    private String directoryPathEct;
    @Value("${banco.azul.estados.cuenta.directorio.ecc}")
    private String directoryPathEcc;
    @Value("${banco.azul.estados.cuenta.directorio.indexados}")
    private String destination;
    @Value("${banco.azul.estados.cuenta.directorio.metadata}")
    private String metaDataFileName;
    @Value("${banco.azul.estados.cuenta.docuware.idArchivador}")
    private String idArchivador;
    @Value("${banco.azul.estados.cuenta.schedule.maxExecutionTime}")
    private String maxExecutionTime;

    /**
     * Scheduled task to process metadata for estado de cuenta files
     */
    @Scheduled(cron = "${banco.azul.estados.cuenta.schedule.cron}")
    public void scheduleTask() {
        // Set base directory paths
        Path baseDir1 = Paths.get(directoryPathEct);
        Path baseDir2 = Paths.get(directoryPathEcc);

        // Set local file paths
        String txtLocalPath1 = directoryPathEct + File.separator + metaDataFileName;
        String txtLocalPath2 = directoryPathEcc + File.separator + metaDataFileName;

        // Set tipoEstadoCuenta values
        String tipoEstadoCuenta1 = "EC01";
        String tipoEstadoCuenta2 = "EC02";

        if (checkBaseDirs(baseDir1)) {
            LOGGER.info("Process metadata ECT: {}", txtLocalPath1);
            this.processMetadata(baseDir1, txtLocalPath1, tipoEstadoCuenta1, true);
        } else {
            LOGGER.info("Base directory ECT not found or empty");
        }
        if (checkBaseDirs(baseDir2)) {
            LOGGER.info("Process metadata ECC: {}", txtLocalPath2);
            this.processMetadata(baseDir2, txtLocalPath2, tipoEstadoCuenta2, true);
        } else {
            LOGGER.info("Base directory ECC not found or empty");
        }
    }

    /**
     * Checks if the provided base directory exists and contains files.
     *
     * @param baseDir the base directory path to check
     * @return true if the base directory exists and contains files, false otherwise
     */
    private boolean checkBaseDirs(Path baseDir) {
        return Objects.nonNull(baseDir) && Files.exists(baseDir) && Objects.requireNonNull(baseDir.toFile().listFiles()).length > 0;
    }

    /**
     * Watches the specified directories for new file creation events and processes
     * the new files.
     */
    @Override
    public void watchDirectory() {
        try {
            if (checkDirectoriesExist()) {
                try (WatchService watchService = createWatchService()) {
                    WatchKey key;
                    while ((key = watchService.take()) != null) {
                        this.processEvents(key);
                        key.reset();
                    }
                }
            } else {
                LOGGER.fatal("Directories not found: {}, {}", directoryPathEct, directoryPathEcc);
            }
        } catch (Exception e) {
            LOGGER.warn("Error in watchDirectory method: ", e);
        }
    }

    /**
     * Checks if the required directories exist.
     *
     * @return true if both directories exist, false otherwise
     */
    private boolean checkDirectoriesExist() {
        return folderExist(directoryPathEct) && folderExist(directoryPathEcc);
    }

    /**
     * Creates a new WatchService and registers the specified directories for entry
     * creation events.
     *
     * @return the created WatchService
     * @throws IOException if an I/O error occurs
     */
    private WatchService createWatchService() throws IOException {
        // Create a new WatchService
        WatchService watchService = FileSystems.getDefault().newWatchService();

        // Register the specified directories for entry creation events
        Path pathEtc = Paths.get(directoryPathEct);
        Path pathEcc = Paths.get(directoryPathEcc);
        pathEtc.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        pathEcc.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        return watchService;
    }

    /**
     * Processes events related to a specific WatchKey
     *
     * @param key The WatchKey to process events for
     */
    private void processEvents(WatchKey key) {
        // Get the base directory from the WatchKey
        Path baseDir = (Path) key.watchable();

        // Fetch the type of Estado de Cuenta
        String tipoEstadoCuenta = fetchTipoEstadoCuenta(baseDir);

        // Iterate through each event in the WatchKey
        for (WatchEvent<?> event : key.pollEvents()) {
            // Construct the local path for the event
            String txtLocalPath = baseDir + File.separator + event.context();

            // Check if the TXT file is valid
            if (checkTxtIsValid(txtLocalPath)) {
                // Fetch metadata for the TXT file and process the documents
                executor.submit(() -> this.processMetadata(baseDir, txtLocalPath, tipoEstadoCuenta, false));
            } else {
                // Log a warning for invalid TXT files
                LOGGER.warn("Invalid TXT file: {}", txtLocalPath);
            }
        }
    }

    /**
     * Process metadata by fetching txt metadata and submitting tasks to the executor.
     *
     * @param baseDir          the base directory path
     * @param txtLocalPath     the local path of the txt file
     * @param tipoEstadoCuenta the type of the estado cuenta
     * @param isScheduled      whether the process is scheduled or not
     */
    private void processMetadata(Path baseDir, String txtLocalPath, String tipoEstadoCuenta, boolean isScheduled) {
        LOGGER.debug("Process metadata: {}", txtLocalPath);

        // Fetch txt metadata
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<Documento> documentos = fetchTxtMetadata(baseDir, txtLocalPath, tipoEstadoCuenta);
        LOGGER.info("Number of documents to process: {}", documentos.size());

        // Set start and end time for task execution
        Instant startTime = Instant.now();
        LOGGER.info("Start time: {}", startTime);
        Instant endTime = startTime.plusSeconds(Long.parseLong(maxExecutionTime));
        LOGGER.info("End time: {}", endTime);

        // Submit tasks to the executor
        if (!documentos.isEmpty()) {
            for (Documento doc : documentos) {
                if (isScheduled && !Instant.now().isBefore(endTime)) {
                    LOGGER.info("Task execution time exceeded: {}", maxExecutionTime);
                    break;
                }
                executor.submit(() -> this.processDocument(doc, baseDir, tipoEstadoCuenta));
            }
        } else {
            LOGGER.debug("Documents not found!");
        }
    }

    /**
     * Processes a document by checking if it exists in the document management
     * service. If the document doesn't exist, it indexes it and moves the file to a
     * specified destination based on certain criteria.
     *
     * @param doc              The document to process
     * @param baseDir          The base directory path where the document is located
     * @param tipoEstadoCuenta The type of account state
     */
    private void processDocument(Documento doc, Path baseDir, String tipoEstadoCuenta) {
        // Log the start of document processing
        LOGGER.info("Start processing document {}", doc.getNombreArchivo());

        // Record the start time for performance measurement
        long startTime = System.currentTimeMillis();

        // Check if the document exists in the document management service
        if (!docuwareService.documentExist(doc)) {
            // Index the document in the document management service
            String status = docuwareService.indexDocument(doc);

            // Log the API response after indexing
            LOGGER.debug("API response after indexing: {}", status);

            // Move the file to the destination folder based on certain criteria
            moveFiles(baseDir + File.separator + doc.getNombreArchivo(), destination, tipoEstadoCuenta, doc.getIndices().get(3).getValor());
        } else {
            // Log a warning if the document is already indexed
            LOGGER.warn("Document already indexed: {}", doc.getNombreArchivo());
        }

        // Calculate the time taken for document processing
        long endTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
        LOGGER.info("Finish processing document {} in {} seconds", doc.getNombreArchivo(), endTime);
    }

    /**
     * Composes a Documento object based on the provided information.
     *
     * @param baseDir          The base directory path.
     * @param registro         The Registro object containing information.
     * @param tipoEstadoCuenta The type of account status.
     * @return The composed Documento object.
     */
    private Documento composeDocumento(Path baseDir, Registro registro, String tipoEstadoCuenta) {
        LOGGER.debug("composeDocumento {}{}{}", baseDir, File.separator, registro.getNombreArchivo());
        // Create a new Documento object
        Documento newDocumento = new Documento();

        // Set the idArchivador on the new Documento
        newDocumento.setIdArchivador(idArchivador);

        // Set the name of the file on the new Documento
        newDocumento.setNombreArchivo(registro.getNombreArchivo());

        // Convert the PDF file to Base64 and set it on the new Documento
        newDocumento.setDocumentoBase64(pdfABase64ConverterService.convertToBase64(new File(baseDir + File.separator + registro.getNombreArchivo())));

        // Add indices to the new Documento
        newDocumento.getIndices().add(new Indice("NUMERO_DE_CUENTA", registro.getTarjetaCuenta()));
        newDocumento.getIndices().add(new Indice("CLIENTE", registro.getCliente()));
        newDocumento.getIndices().add(new Indice("ANIO", registro.getAnio()));
        newDocumento.getIndices().add(new Indice("MES", registro.getMes()));
        newDocumento.getIndices().add(new Indice("TIPO_ESTADO_DE_CUENTA", tipoEstadoCuenta));

        return newDocumento;
    }

    /**
     * Fetches metadata from a TXT file.
     *
     * @param baseDir          the base directory path
     * @param txtFilePath      the path of the TXT file
     * @param tipoEstadoCuenta the type of account statement
     * @return a list of Documento objects containing the metadata
     */
    private List<Documento> fetchTxtMetadata(Path baseDir, String txtFilePath, String tipoEstadoCuenta) {
        LOGGER.debug("fetch metadata lines:");
        try (Stream<String> stream = Files.lines(Paths.get(txtFilePath))) {
            return stream.map(this::composeRegistro).filter(Objects::nonNull).filter(newRegistro -> pdfFileExist(baseDir + File.separator + newRegistro.getNombreArchivo())).map(newRegistro -> composeDocumento(baseDir, newRegistro, tipoEstadoCuenta)).collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.warn("Error in fetchTxtMetadata method: ", e);
            return new ArrayList<>();
        }
    }

    /**
     * Check if a PDF file exists at the given path.
     *
     * @param pdfPath The path to the PDF file
     * @return true if the PDF file exists, false otherwise
     */
    private boolean pdfFileExist(String pdfPath) {
        File newPdf = new File(pdfPath);
        return newPdf.exists();
    }

    /**
     * Check if the folder exists at the specified path.
     *
     * @param folderPath the path of the folder to check
     * @return true if the folder exists and is a directory, false otherwise
     */
    public boolean folderExist(String folderPath) {
        File folder = new File(folderPath);
        return folder.exists() && folder.isDirectory();
    }

    /**
     * Fetches the tipoEstadoCuenta based on the directory path provided.
     *
     * @param baseDir The base directory path to compare against
     * @return The tipoEstadoCuenta corresponding to the directory path
     */
    public String fetchTipoEstadoCuenta(Path baseDir) {
        String tipoEstadoCuenta = "";

        // Check if the directory path matches directoryPathEct and assign
        // tipoEstadoCuenta accordingly
        if (directoryPathEct.equals(baseDir.toString())) {
            tipoEstadoCuenta = "EC01";
        }

        // Check if the directory path matches directoryPathEcc and assign
        // tipoEstadoCuenta accordingly
        if (directoryPathEcc.equals(baseDir.toString())) {
            tipoEstadoCuenta = "EC02";
        }

        return tipoEstadoCuenta;
    }

    /**
     * Checks if the provided text file is valid based on certain criteria.
     * Criteria: 1. The file must exist. 2. The file name must match a specified
     * metadata file name. 3. The file must have a .txt extension.
     *
     * @param txtLocalPath The local path of the text file to be checked.
     * @return true if the text file is valid, false otherwise.
     */
    public boolean checkTxtIsValid(String txtLocalPath) {
        // Create a File object from the provided local path
        File txtFile = new File(txtLocalPath);

        // Check if the file exists
        boolean fileExist = txtFile.exists();

        // Check if the file name matches the specified metadata file name
        // (case-insensitive)
        boolean isNamedProperly = metaDataFileName.equals(txtFile.getName().toLowerCase());

        // Check if the file has a .txt extension (case-insensitive)
        boolean isTxtfile = txtFile.getName().toLowerCase().endsWith(".txt");

        // Return true if all criteria are met, false otherwise
        return fileExist && isNamedProperly && isTxtfile;
    }

    /**
     * Composes a Registro object from a text line.
     *
     * @param txtLine The text line containing fields separated by commas.
     * @return A Registro object if the text line contains all required fields, null
     * otherwise.
     */
    public Registro composeRegistro(String txtLine) {
        // Create a new Registro object
        Registro registro = new Registro();

        // Split the text line by commas to get individual fields
        String[] fieldsArray = txtLine.split(",");
        List<String> fields = Arrays.asList(fieldsArray);

        // Check if the text line contains all required fields
        if (fields.size() == 4) {
            // Set values to the Registro object based on the fields
            registro.setNombreArchivo(fields.get(0));
            registro.setAnio(fields.get(1).substring(0, 4));
            registro.setMes(convertMonthNumberToName(fields.get(1).substring(4, 6)));
            registro.setTarjetaCuenta(fields.get(2));
            registro.setCliente(fields.get(3));
            return registro;
        } else {
            // Log a message if the text line doesn't have all fields
            LOGGER.debug("Text line doesn't have all required fields: {}", txtLine);
            return null;
        }
    }

    /**
     * Moves a file from one folder to another.
     *
     * @param source           The path of the source file.
     * @param destination      The path of the destination folder.
     * @param tipoEstadoCuenta The type of account statement.
     * @param mes              The month.
     */
    public void moveFiles(String source, String destination, String tipoEstadoCuenta, String mes) {
        // Create Path objects for the source and destination
        Path sourcePath = Paths.get(source);
        Path destinationPath = Paths.get(destination, tipoEstadoCuenta, mes);

        try {
            // Check if the destination directory exists, if not create it
            if (!Files.exists(destinationPath)) {
                Files.createDirectories(destinationPath);
            }

            // Move the file from source to destination
            Files.move(sourcePath, destinationPath.resolve(sourcePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Handle any IOException that occurs during the file moving process
            LOGGER.warn("Error in moveFiles method: ", e);
        }
    }

    /**
     * Converts the month from a number to its Spanish name
     *
     * @param monthNumber the number of the month (e.g., "01" for Enero)
     * @return the Spanish name of the month, or an empty string if the input is
     * invalid
     */
    private String convertMonthNumberToName(String monthNumber) {
        switch (monthNumber) {
            case "01":
                return "ENERO";
            case "02":
                return "FEBRERO";
            case "03":
                return "MARZO";
            case "04":
                return "ABRIL";
            case "05":
                return "MAYO";
            case "06":
                return "JUNIO";
            case "07":
                return "JULIO";
            case "08":
                return "AGOSTO";
            case "09":
                return "SEPTIEMBRE";
            case "10":
                return "OCTUBRE";
            case "11":
                return "NOVIEMBRE";
            case "12":
                return "DICIEMBRE";
            default:
                return "";
        }
    }

}