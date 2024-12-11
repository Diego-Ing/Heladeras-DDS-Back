package com.utndds.heladerasApi.services.ABM;

import com.utndds.heladerasApi.DTOs.HeladeraDTO;
import com.utndds.heladerasApi.models.Heladera.Heladera;
import com.utndds.heladerasApi.repositories.HeladeraRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HeladeraService {

    @Autowired
    private HeladeraRepository heladeraRepository;

    // Alta: Crear una nueva heladera desde el DTO
    public void crearHeladera(HeladeraDTO heladeraDTO) {
        Heladera heladera = new Heladera();
        heladera.setCapacidad(heladeraDTO.getCapacidad());
        heladera.setTempMax(heladeraDTO.getTemperaturaMaxima());
        heladera.setTempMin(heladeraDTO.getTemperaturaMinima());

        heladeraRepository.save(heladera);
    }

    // Baja: Eliminar una heladera por ID
    public void eliminarHeladera(Long id) {
        heladeraRepository.deleteById(id);
    }

    // Modificación: Actualizar una heladera existente desde el DTO
    public void actualizarHeladera(Long id, HeladeraDTO heladeraDTO) {
        Optional<Heladera> heladeraOptional = heladeraRepository.findById(id);
        if (heladeraOptional.isPresent()) {
            Heladera heladera = heladeraOptional.get();
            heladera.setCapacidad(heladeraDTO.getCapacidad());
            heladera.setTempMax(heladeraDTO.getTemperaturaMaxima());
            heladera.setTempMin(heladeraDTO.getTemperaturaMinima());

            heladeraRepository.save(heladera);
        } else {
            throw new EntityNotFoundException("Heladera no encontrada con el ID: " + id);
        }
    }

    public List<HeladeraDTO> obtenerHeladerasDTO() {
        return heladeraRepository.findAll().stream()
                .map(heladera -> new HeladeraDTO(heladera.getId(), heladera.getPunto().getNombrePunto()))
                .collect(Collectors.toList());
    }

    public void activarTodasLasHeladeras() {
        heladeraRepository.activarTodas();
    }

}