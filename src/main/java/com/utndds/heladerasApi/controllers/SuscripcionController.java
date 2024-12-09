package com.utndds.heladerasApi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.utndds.heladerasApi.DTOs.SuscripcionDTO;
import com.utndds.heladerasApi.services.SuscripcionService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/suscripciones")
public class SuscripcionController {

    @Autowired
    private SuscripcionService suscripcionService;

    @PostMapping("/suscribir")
    public ResponseEntity<String> suscribirColaborador(@RequestBody SuscripcionDTO suscripcionDTO) {
        try {
            // Llamamos al servicio que maneja la lógica de la suscripción
            String mensaje = suscripcionService.suscribirColaborador(suscripcionDTO);

            // Si la respuesta del servicio es "OK", devolvemos 200 OK
            if ("OK".equals(mensaje)) {
                return ResponseEntity.ok(mensaje);
            } else {
                // Si no es "OK", retornamos un error con el mensaje
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mensaje);
            }
        } catch (RuntimeException e) {
            // Si ya está suscrito, se lanza una excepción para devolver un código 409 Conflict
            if (e.getMessage().contains("ya está suscripto")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Usted ya está suscripto a esta heladera");
            }
            // En caso de otros errores, se devuelve un error genérico
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al realizar la suscripción: " + e.getMessage());
        } catch (Exception e) {
            // En caso de un error general
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al realizar la suscripción: " + e.getMessage());
        }
    }
}
