package com.bancoazul.estados.cuenta.utils;

import java.io.File;

/**
 * Interfaces with methods to convert PDF binary files to base 64
 * 
 * @author Melvin Reyes
 */
public interface PdfABase64ConverterService {

	/**
	 * @param pdfFile PDF file read from file system
	 * @return String base 64 representation of PDF file
	 */
	public String convertToBase64(File pdfFile);

}
