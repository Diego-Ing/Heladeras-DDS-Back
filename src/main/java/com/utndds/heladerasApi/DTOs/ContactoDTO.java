package com.utndds.heladerasApi.DTOs;

public class ContactoDTO {
    private String tipo;
    private String valor;

    public ContactoDTO(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    // Getters y setters
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}

