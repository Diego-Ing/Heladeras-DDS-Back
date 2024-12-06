package com.utndds.heladerasApi.services;

import org.springframework.stereotype.Service;
import com.utndds.heladerasApi.models.Persona.Persona;

import java.util.List;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;

import org.springframework.core.ParameterizedTypeReference;

@Service
public class ReconocimientosExtra {

    private final RestTemplate restTemplate;

    public ReconocimientosExtra(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Persona> recomendarColaboradores(double puntosReq, double viandasDonadasReq, int cantMaxColabs) {
        String url = String.format(
                "https://heladeras-dds-recomendador.onrender.com/service-2/recomendaciones-colaboradores?puntosReq=%f&viandasDonadasReq=%f&cantMaxColabs=%d",
                puntosReq, viandasDonadasReq, cantMaxColabs);

        try {
            // Llamada al endpoint remoto
            ResponseEntity<List<Persona>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Persona>>() {
                    });

            // Retornar la lista de personas desde el servicio remoto
            return response.getBody();
        } catch (RestClientException e) {
            // Manejo de errores
            throw new RuntimeException("Error al llamar al servicio remoto", e);
        }
    }
}