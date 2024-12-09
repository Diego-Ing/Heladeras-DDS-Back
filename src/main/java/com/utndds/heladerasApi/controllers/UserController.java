package com.utndds.heladerasApi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utndds.heladerasApi.DTOs.ContactoDTO;
import com.utndds.heladerasApi.DTOs.PersonaHumanaDTO;
import com.utndds.heladerasApi.DTOs.PersonaJuridicaDTO;
import com.utndds.heladerasApi.models.Persona.Persona;
import com.utndds.heladerasApi.models.Persona.Contacto.Contacto;
import com.utndds.heladerasApi.repositories.RolRepository;
import com.utndds.heladerasApi.services.ABM.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.utndds.heladerasApi.models.Rol.Rol;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService UserService;

    @Autowired
    private RolRepository rolRepository;

    @PostMapping("/personaHumana")
    public ResponseEntity<String> personaHumana(@RequestBody PersonaHumanaDTO personaHumanaDTO) {
        Long id = UserService.crearPersonaHumana(personaHumanaDTO);
        return ResponseEntity.ok("Alta exitosa. ID: " + id);
    }

    @PostMapping("/personaJuridica")
    public ResponseEntity<String> personaJuridica(@RequestBody PersonaJuridicaDTO personaJuridicaDTO) {
        Long id = UserService.crearPersonaJuridica(personaJuridicaDTO);
        return ResponseEntity.ok("Alta exitosa. ID: " + id);
    }
    
    @GetMapping("/contactos")
public ResponseEntity<List<ContactoDTO>> getContactos(@RequestParam("UUID") String uuid) {
    if (uuid == null || uuid.isEmpty()) {
        return ResponseEntity.badRequest().body(Collections.emptyList()); // 400 si el UUID no es válido
    }

    // Buscar el rol por UUID
    Optional<Rol> optionalRol = rolRepository.findByUUID(uuid);
    if (optionalRol.isEmpty()) {
        return ResponseEntity.notFound().build(); // 404 si no se encuentra el rol
    }

    // Obtener la persona asociada al rol
    Rol rol = optionalRol.get();
    Persona persona = rol.getPersona();
    if (persona == null) {
        return ResponseEntity.notFound().build(); // 404 si no se encuentra la persona asociada
    }

    // Obtener los medios de contacto de la persona y convertir a DTO
    List<ContactoDTO> contactosDTO = persona.getMediosContacto().stream()
        .map(contacto -> new ContactoDTO(contacto.getClass().getSimpleName(), contacto.getValor()))
        .collect(Collectors.toList());

    return ResponseEntity.ok(contactosDTO);
}


    
}
