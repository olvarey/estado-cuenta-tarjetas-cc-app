package com.bancoazul.estados.cuenta.services;

import com.bancoazul.estados.cuenta.pojos.Documento;

/**
 * Interface class to handle Docuware API http methods
 *
 * @author Melvin Reyes
 */
public interface DocuwareService {

	/**
	 * @param documento {@link Documento} que se desea buscar existencia
	 * @return boolean Respuesta SI o NO existe el documento
	 */
	public boolean documentExist(Documento documento);

	/**
	 * @param documento {@link Documento} a indexar
	 * @return Respuesta json string de API Docuware
	 */
	public String indexDocument(Documento documento);

	/**
	 * @param documento {@link Documento} que se desea descargar
	 * @return Response json string con el archivo en base 64
	 */
	public String downloadDocument(Documento documento);

}
