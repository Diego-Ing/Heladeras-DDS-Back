package com.utndds.heladerasApi.models.Persona.Contacto;

import com.utndds.heladerasApi.services.NotificacionApis.MailApi;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("EMAIL")
public class Email extends Contacto {

    public Email() {
    }

    public Email(String email) {
        this.valor = email;
    }

    @Override
    public void notificar(String mensaje) {
        MailApi.sendEmail(mensaje, this.valor);
    }
}
