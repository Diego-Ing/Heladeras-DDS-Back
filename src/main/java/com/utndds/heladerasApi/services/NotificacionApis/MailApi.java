package com.utndds.heladerasApi.services.NotificacionApis;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.MultiValueMap;

public class MailApi {

    private static final String BASE_URL = "https://mockapi-31uk.onrender.com/mockAPI/enviar-correo";

    public static void sendEmail(String message, String correo) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("mensaje", message);
            params.add("correo", correo);

            restTemplate.postForObject(BASE_URL, params, String.class);
            System.out.println("Correo enviado con éxito a " + correo);
        } catch (Exception e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}