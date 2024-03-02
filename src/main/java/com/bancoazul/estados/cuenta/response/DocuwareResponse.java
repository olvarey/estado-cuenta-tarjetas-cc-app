package com.bancoazul.estados.cuenta.response;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocuwareResponse {

	@SerializedName("codigo")
	private String codigo;
	@SerializedName("mensaje")
	private String mensaje;

}
