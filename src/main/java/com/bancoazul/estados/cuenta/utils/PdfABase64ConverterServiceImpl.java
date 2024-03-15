package com.bancoazul.estados.cuenta.utils;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * This class service is used to convert PDF files to Base64 string
 *
 * @author Melvin Reyes
 */
@Service
public class PdfABase64ConverterServiceImpl implements PdfABase64ConverterService {

    /**
     * Convert a PDF file to its base64 representation
     *
     * @param pdfFile The binary PDF file to be converted
     * @return The base64 representation of the PDF file
     */
    @Override
    public String convertToBase64(File pdfFile) {
        // Read the PDF file into a byte array
        byte[] pdfBytes = null;
        try {
            pdfBytes = readPdfFile(pdfFile);
        } catch (IOException e) {
            // Handle IOException
            e.printStackTrace();
        }
        // Encode the byte array to base64 and return the result
        return encodeToBase64(pdfBytes);
    }

    /**
     * Reads the content of a PDF file into a byte array.
     *
     * @param pdfFile The PDF file to read
     * @return The byte array representation of the file
     * @throws IOException If an I/O error occurs
     */
    private byte[] readPdfFile(File pdfFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(pdfFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    /**
     * Encodes the input byte array to a base64 string.
     *
     * @param data The byte array to be encoded
     * @return The base64 representation of the input byte array
     */
    private String encodeToBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

}
