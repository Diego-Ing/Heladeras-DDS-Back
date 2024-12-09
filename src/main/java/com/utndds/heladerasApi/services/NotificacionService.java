package com.utndds.heladerasApi.services;

import org.springframework.beans.factory.annotation.Autowired;

import com.utndds.heladerasApi.repositories.PersonaRepository;
import com.utndds.heladerasApi.models.Persona.Contacto.Contacto;
import org.springframework.stereotype.Service;
import java.util.List;
import com.utndds.heladerasApi.models.Persona.Persona;

@Service
public class NotificacionService {

    @Autowired
    PersonaRepository personaRepository;

    public void sendNotification(String message) {
        // Obtener todas las personas
        List<Persona> personas = personaRepository.findAll();

        for (Persona persona : personas) {
            // Iterar sobre los medios de contacto de la persona
            for (Contacto medio : persona.getMediosContacto()) {
                try {
                    medio.notificar(message);
                } catch (Exception e) {
                    System.err.println("Error al notificar a través de " + medio.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }
    public void sendNotification(String message, Persona persona) {
        // Iterar sobre los medios de contacto de la persona
        for (Contacto medio : persona.getMediosContacto()) {
            try {
                medio.notificar(message);
            } catch (Exception e) {
                System.err.println("Error al notificar a través de " + medio.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}
