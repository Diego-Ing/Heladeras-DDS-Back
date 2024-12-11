package com.utndds.heladerasApi.controllers;

import com.utndds.heladerasApi.DTOs.AreaRecomendacionDTO;
import com.utndds.heladerasApi.DTOs.HeladeraDTO;
import com.utndds.heladerasApi.DTOs.RecomendacionDTO;
import com.utndds.heladerasApi.services.RecomendacionPuntosService;
import com.utndds.heladerasApi.services.ABM.HeladeraService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/heladeras")
public class HeladeraController {

    @Autowired
    private HeladeraService heladeraService;

    @Autowired
    private RecomendacionPuntosService recomendacionService;

    @PostMapping("/activar-heladeras")
    public void activarHeladeras() {
        heladeraService.activarTodasLasHeladeras();
    }

    // Endpoint para recomendar puntos de colocación de heladeras
    @PreAuthorize("hasAuthority('SCOPE_ROLE_COLLABORATOR')")
    @PostMapping("/recomendarPuntos")
    public List<RecomendacionDTO> recomendarPuntosColocacion(@RequestBody AreaRecomendacionDTO data) {
        return recomendacionService.getRecomendaciones(data.getLatitud(), data.getLongitud(), data.getRadio());
    }

    // Alta: Agregar una nueva heladera
    @PreAuthorize("hasAuthority('SCOPE_ROLE_COLLABORATOR')")
    @PostMapping("/agregar")
    public ResponseEntity<String> agregarHeladera(@RequestBody HeladeraDTO heladeraDTO) {
        heladeraService.crearHeladera(heladeraDTO);
        return ResponseEntity.ok("Heladera agregada exitosamente.");
    }

    // Baja: Eliminar una heladera por ID
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarHeladera(@PathVariable Long id) {
        try {
            heladeraService.eliminarHeladera(id);
            return ResponseEntity.ok("Heladera eliminada exitosamente.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Modificación: Actualizar una heladera existente por ID
    @PreAuthorize("hasAuthority('SCOPE_ROLE_COLLABORATOR')")
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> actualizarHeladera(@PathVariable Long id, @RequestBody HeladeraDTO heladeraDTO) {
        try {
            heladeraService.actualizarHeladera(id, heladeraDTO);
            return ResponseEntity.ok("Heladera actualizada exitosamente.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/listaHeladeras")
    public List<HeladeraDTO> obtenerHeladeras() {
        return heladeraService.obtenerHeladerasDTO();
    }
}