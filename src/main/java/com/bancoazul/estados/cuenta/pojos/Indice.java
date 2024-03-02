package com.bancoazul.estados.cuenta.pojos;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * POJO class to handle Indices added as {@code List<Indice>} data in
 * {@link Documento}
 *
 * @author Melvin Reyes
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Indice {

	@SerializedName("index")
	private String indice;
	@SerializedName("value")
	private String valor;

}
