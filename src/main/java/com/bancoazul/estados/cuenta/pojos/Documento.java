package com.bancoazul.estados.cuenta.pojos;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * POJO class to handle Document data use in Docuware
 *
 * @author Melvin Reyes
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Documento {

	@SerializedName("idArchivador")
	private String idArchivador;
	@SerializedName("nombreArchivo")
	private String nombreArchivo;
	@SerializedName("documentoBase64")
	private String documentoBase64;
	@SerializedName("indices")
	private List<Indice> indices = new ArrayList<>();

}
