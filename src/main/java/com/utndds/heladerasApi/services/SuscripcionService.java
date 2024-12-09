package com.utndds.heladerasApi.services;

import com.utndds.heladerasApi.DTOs.SuscripcionDTO;
import com.utndds.heladerasApi.models.Enums.TipoEvento;
import com.utndds.heladerasApi.models.Heladera.Heladera;
import com.utndds.heladerasApi.models.Persona.Contacto.Contacto;
import com.utndds.heladerasApi.models.Persona.Contacto.Email;
import com.utndds.heladerasApi.models.Persona.Contacto.Telefono;
import com.utndds.heladerasApi.models.Persona.Contacto.Telegram;
import com.utndds.heladerasApi.models.Persona.Contacto.Whatsapp;
import com.utndds.heladerasApi.models.Rol.Colaborador;

import org.springframework.stereotype.Service;
import com.utndds.heladerasApi.models.Suscripciones.Suscripcion;
import com.utndds.heladerasApi.models.Suscripciones.Evento.Desperfecto;
import com.utndds.heladerasApi.models.Suscripciones.Evento.Evento;
import com.utndds.heladerasApi.models.Suscripciones.Evento.MuchasViandas;
import com.utndds.heladerasApi.models.Suscripciones.Evento.PocasViandas;
import com.utndds.heladerasApi.repositories.ColaboradorRepository;
import com.utndds.heladerasApi.repositories.ContactoRepository;
import com.utndds.heladerasApi.repositories.HeladeraRepository;
import com.utndds.heladerasApi.repositories.SuscripcionesRepositories.SuscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuscripcionService {

    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Autowired
    private HeladeraRepository heladeraRepository;

    @Autowired
    private ContactoRepository contactoRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private NotificacionService notificacionService;

    // Método para suscribir un colaborador a una heladera basado en los eventos y medios seleccionados
    public String suscribirColaborador(SuscripcionDTO suscripcionDTO) {
        try {
            // Buscar colaborador y heladera por ID
            Colaborador colaborador = colaboradorRepository.findByUUID(suscripcionDTO.getColaboradorUUID())
                    .orElseThrow(() -> new RuntimeException("Colaborador no encontrado"));
            Heladera heladera = heladeraRepository.findById(suscripcionDTO.getHeladeraId())
                    .orElseThrow(() -> new RuntimeException("Heladera no encontrada"));

            // Verificar si el colaborador ya está suscripto a la heladera
            Suscripcion existingSuscripcion = suscripcionRepository.findByColaboradorAndHeladera(colaborador, heladera);
            if (existingSuscripcion != null) {
                // Lanzamos una excepción si el colaborador ya está suscripto
                throw new SuscripcionExistenteException("Usted ya está suscripto a esta heladera");
            }

            // Crear la nueva suscripción
            Suscripcion suscripcion = new Suscripcion();
            suscripcion.setColaborador(colaborador);
            suscripcion.setHeladera(heladera);

            // Mapear el enum TipoContacto a las clases concretas de Contacto
            List<Class<? extends Contacto>> clasesDeContacto = suscripcionDTO.getTiposContactosSeleccionados().stream()
                    .map(tipo -> {
                        switch (tipo) {
                            case EMAIL:
                                return Email.class;
                            case TELEFONO:
                                return Telefono.class;
                            case TELEGRAM:
                                return Telegram.class;
                            case WHATSAPP:
                                return Whatsapp.class;
                            default:
                                throw new IllegalArgumentException("Tipo de contacto no soportado: " + tipo);
                        }
                    }).collect(Collectors.toList());

            // Buscar contactos del colaborador por las clases de contacto mapeadas
            List<Contacto> mediosDeseados = contactoRepository.findByPersonaAndTipoIn(colaborador.getPersona(), clasesDeContacto);

            // Crear eventos basados en los tipos seleccionados
            for (TipoEvento tipo : suscripcionDTO.getTiposEventosSeleccionados()) {
                Evento evento;
                switch (tipo) {
                    case POCAS_VIANDAS:
                        evento = new PocasViandas();
                        ((PocasViandas) evento).setCantidadMinima(suscripcionDTO.getCantidadViandasMin());
                        break;
                    case MUCHAS_VIANDAS:
                        evento = new MuchasViandas();
                        ((MuchasViandas) evento).setCantidadMaxima(suscripcionDTO.getCantidadViandasMax());
                        break;
                    case FALLA_TECNICA:
                        evento = new Desperfecto();
                        break;
                    default:
                        throw new IllegalArgumentException("Tipo de evento no reconocido: " + tipo);
                }

                evento.setMediosDeseados(mediosDeseados);
                evento.setSuscripcion(suscripcion);
                suscripcion.getNotificacionesDeseadas().add(evento);
            }

            // Guardar la suscripción en el repositorio
            suscripcionRepository.save(suscripcion);
            
            // Enviar notificación al colaborador
            notificacionService.sendNotification("Muchas gracias por suscribirse a la heladera " + heladera.getPunto().getNombrePunto() + "!",colaborador.getPersona());
            
            // Retornar "OK" si la suscripción fue guardada correctamente
            return "OK";
        } catch (SuscripcionExistenteException e) {
            // Si el colaborador ya está suscripto, lanzar el mensaje apropiado
            throw e;  // Rethrow para ser manejado en el controlador
        } catch (Exception e) {
            // En caso de cualquier otro error, devolver el mensaje de error
            throw new RuntimeException("Error al realizar la suscripción: " + e.getMessage(), e);
        }
    }
}
