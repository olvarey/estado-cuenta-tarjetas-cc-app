package com.bancoazul.estados.cuenta.pojos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Registro {

    private String nombreArchivo;
    private String anio;
    private String mes;
    private String tarjetaCuenta;
    private String cliente;
}
