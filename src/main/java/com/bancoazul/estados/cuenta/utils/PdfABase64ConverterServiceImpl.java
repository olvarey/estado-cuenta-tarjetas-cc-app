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
	 * @param pdfFile binary file
	 * @return String base64 representation
	 */
	@Override
	public String convertToBase64(File pdfFile) {
		byte[] pdfBytes = null;
		try {
			pdfBytes = readPdfFile(pdfFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encodeToBase64(pdfBytes);
	}

	/**
	 * @param pdfFile binary file
	 * @return array of bytes file representation
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
	 * @param data array of bytes file representation
	 * @return String base64 representation
	 */
	private String encodeToBase64(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

}
