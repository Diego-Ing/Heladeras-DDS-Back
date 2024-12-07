package com.utndds.heladerasApi.services.broker;

import org.springframework.stereotype.Service;

import com.utndds.heladerasApi.models.Tarjetas.TarjetaPersVuln;
import com.utndds.heladerasApi.repositories.HeladeraRepository;
import com.utndds.heladerasApi.repositories.TarjetasRepositories.TarjetaRepository;
import com.utndds.heladerasApi.models.Tarjetas.TarjetaColaborador;
import com.utndds.heladerasApi.services.AperturaService;
import com.utndds.heladerasApi.services.Sensores.SensorMovimientoService;
import com.utndds.heladerasApi.services.Sensores.SensorTemperaturaService;
import com.fasterxml.jackson.core.type.TypeReference; // Para TypeReference
import com.fasterxml.jackson.databind.ObjectMapper; // Para ObjectMapper
import org.springframework.amqp.rabbit.annotation.RabbitListener; // Para RabbitListener
import org.springframework.beans.factory.annotation.Autowired; // Para Autowired
import java.io.IOException; // Para IOException
import java.util.Map; // Para Map

@Service
public class BrokerReceiverService {

    @Autowired
    private SensorTemperaturaService sensorTemperaturaService;

    @Autowired
    private SensorMovimientoService sensorMovimientoService;
    @Autowired
    private TarjetaRepository tarjetaRepository;
    @Autowired
    private HeladeraRepository heladeraRepository;

    @Autowired
    private AperturaService aperturaService;

    @RabbitListener(queues = "sensor_temperatura_queue")
    public void recibirTemperatura(String mensaje) {
        System.out.println("Mensaje recibido (temperatura): " + mensaje);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> data = objectMapper.readValue(mensaje, new TypeReference<>() {
            });
            Long sensorId = ((Number) data.get("sensorId")).longValue();
            Double temperatura = ((Number) data.get("temperatura")).doubleValue();

            sensorTemperaturaService.procesarTemperatura(sensorId, temperatura);
        } catch (IOException e) {
            System.err.println("Error al procesar el mensaje: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "sensor_movimiento_queue")
    public void recibirMovimiento(String mensaje) {
        System.out.println("Mensaje recibido (movimiento): " + mensaje);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> data = objectMapper.readValue(mensaje, new TypeReference<>() {
            });
            Long sensorId = ((Number) data.get("sensorId")).longValue();
            String tipoAlerta = (String) data.get("tipo");

            sensorMovimientoService.generarAlerta(sensorId, tipoAlerta);
        } catch (IOException e) {
            System.err.println("Error al procesar el mensaje de movimiento: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "tarjeta_queue")
    public void recibirTarjeta(String mensaje) {
        System.out.println("Mensaje recibido (tarjeta): " + mensaje);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, Object> data = objectMapper.readValue(mensaje, new TypeReference<>() {
            });
            Long heladeraId = ((Number) data.get("heladeraId")).longValue();
            Long tarjetaId = ((Number) data.get("tarjetaId")).longValue();

            tarjetaRepository.findById(tarjetaId)
                    .ifPresentOrElse(tarjeta -> heladeraRepository.findById(heladeraId).ifPresentOrElse(heladera -> {
                        String resultado;
                        if (tarjeta instanceof TarjetaColaborador) {
                            resultado = aperturaService.manejarAperturaColaborador(heladera, tarjeta);
                        } else if (tarjeta instanceof TarjetaPersVuln) {
                            resultado = aperturaService.manejarAperturaPersonaVulnerable(heladera, tarjeta);
                        } else {
                            resultado = "Tipo de tarjeta no válido.";
                        }
                        System.out.println("Resultado: " + resultado);
                    },
                            () -> System.out.println("Heladera no encontrada.")),
                            () -> System.out.println("Tarjeta no encontrada."));
        } catch (IOException e) {
            System.err.println("Error al procesar el mensaje: " + e.getMessage());
        }
    }
}
