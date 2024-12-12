package com.utndds.heladerasApi.services;

import com.utndds.heladerasApi.DTOs.FallaTecnicaDTO;
import com.utndds.heladerasApi.DTOs.VisitaTecnicoDTO;
import com.utndds.heladerasApi.models.Heladera.Heladera;
import com.utndds.heladerasApi.models.Heladera.Incidentes.VisitaTecnico;
import com.utndds.heladerasApi.models.Heladera.Incidentes.Incidente.Alerta;
import com.utndds.heladerasApi.models.Heladera.Incidentes.Incidente.FallaTecnica;
import com.utndds.heladerasApi.models.Heladera.Incidentes.Incidente.Incidente;
import com.utndds.heladerasApi.models.Rol.Colaborador;
import com.utndds.heladerasApi.models.Rol.Tecnico.Tecnico;
import com.utndds.heladerasApi.repositories.ColaboradorRepository;
import com.utndds.heladerasApi.repositories.FallaTecnicaRepository;
import com.utndds.heladerasApi.repositories.HeladeraRepository;
import com.utndds.heladerasApi.repositories.TecnicoRepository;
import com.utndds.heladerasApi.repositories.VisitaTecnicoRepository;
import com.utndds.heladerasApi.repositories.Incidentes.IncidenteRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IncidenteService {

    @Autowired
    private TecnicoRepository tecnicoRepository; // Asumiendo que tienes un repositorio para los técnicos

    @Autowired
    private IncidenteRepository incidenteRepository;
    @Autowired
    private FallaTecnicaRepository fallaTecnicaRepository;
    @Autowired
    private VisitaTecnicoRepository visitaTecnicoRepository; // Repositorio para registrar visitas

    @Autowired
    private HeladeraRepository heladeraRepository; // Repositorio para registrar visitas
    @Autowired
    private ColaboradorRepository colaboradorRepository; // Repositorio para registrar visitas

    public void reportarIncidente(Incidente incidente) {
        incidenteRepository.save(incidente);
    }

    public void reportarFallaTecnica(String colaboradorUUID, FallaTecnicaDTO fallaTecnicaDTO) {
        Colaborador colaborador = colaboradorRepository.findByUUID(colaboradorUUID)
                .orElseThrow(() -> new RuntimeException("Colaborador no encontrado con uuid: " + colaboradorUUID));

        // Find the heladera by ID and handle if not found
        Heladera heladera = heladeraRepository.findById(fallaTecnicaDTO.getHeladeraId())
                .orElseThrow(() -> new RuntimeException(
                        "Heladera no encontrada con ID: " + fallaTecnicaDTO.getHeladeraId()));
        
        // Create a new instance of FallaTecnica with the DTO data
        FallaTecnica fallaTecnica = new FallaTecnica(heladera, colaborador, fallaTecnicaDTO.getDescripcion(),
                fallaTecnicaDTO.getFoto());
        // Save the technical failure in the incident repository
        incidenteRepository.save(fallaTecnica);

        // Notify the nearest technician about the failure
        this.notificarTecnicoCercano(fallaTecnica);
    }

    public void notificarTecnicoCercano(FallaTecnica fallaTecnica) {
        Tecnico tecnico = this.obtenerTecnicoMasCercano(fallaTecnica.getHeladera());
        if (tecnico != null) {
            String mensaje = "NECESITAMOS TUS SERVICIOS EN LA DIRECCION: "
                    + fallaTecnica.getHeladera().getPunto().getDireccion();
            tecnico.getPersona().notificar(mensaje);

            // Relacionamos el incidente con el técnico
            VisitaTecnico nuevaVisita = new VisitaTecnico(tecnico, fallaTecnica, "Pendiente", null,
                    false);
            visitaTecnicoRepository.save(nuevaVisita);
        }
    }

    private Tecnico obtenerTecnicoMasCercano(Heladera heladera) {
        return tecnicoRepository.findTecnicoCercano(heladera.getPunto().getLatitud(),
                heladera.getPunto().getLongitud());
    }

    public void registrarVisita(String tecnicoUUID, VisitaTecnicoDTO visitaDTO) {
        try {
            // Buscar el técnico por UUID
            
            Tecnico tecnico = tecnicoRepository.findByUUID(tecnicoUUID)
                    .orElseThrow(() -> new EntityNotFoundException("Técnico no encontrado con UUID " + tecnicoUUID));
            

            // Buscar el incidente por ID
            
            FallaTecnica fallaTecnica = fallaTecnicaRepository.findById(visitaDTO.getIncidenteId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "falla tecnica no encontrado con ID " + visitaDTO.getIncidenteId()));
            

            // Crear y guardar la visita del técnico
            
            VisitaTecnico visitaTecnico = new VisitaTecnico(tecnico, fallaTecnica, visitaDTO.getComentario(),
                    visitaDTO.getFoto(), visitaDTO.isSolucionado());
            

            visitaTecnicoRepository.save(visitaTecnico);
            fallaTecnicaRepository.save(fallaTecnica);
            

        } catch (EntityNotFoundException e) {
            // Capturar errores específicos de entidades no encontradas
            System.err.println("Error al buscar entidades: " + e.getMessage());
            throw e; // Re-lanzar la excepción para que sea manejada por el controlador
        } catch (Exception e) {
            // Capturar cualquier otro error
            System.err.println("Error inesperado al registrar la visita técnica: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar la excepción
        }
    }

    public void generarAlerta(Long heladeraId, String tipoAlerta) {
        // Buscar la heladera por ID
        Heladera heladera = heladeraRepository.findById(heladeraId)
                .orElseThrow(() -> new RuntimeException("Heladera no encontrada con ID: " + heladeraId));

        // Crear la alerta
        Alerta alerta = new Alerta(heladera, tipoAlerta);

        // Guardar la alerta en la base de datos
        incidenteRepository.save(alerta);

        System.out.println("Alerta generada para la heladera ID: " + heladeraId + " de tipo: " + tipoAlerta);
    }

}