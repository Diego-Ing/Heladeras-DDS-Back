package com.utndds.heladerasApi.services.broker;

import com.utndds.heladerasApi.models.Heladera.Heladera;
import com.utndds.heladerasApi.models.Heladera.Sensores.Sensor;
import com.utndds.heladerasApi.models.Heladera.Sensores.SensorMovimiento;
import com.utndds.heladerasApi.models.Heladera.Sensores.SensorTemperatura;
import com.utndds.heladerasApi.models.Tarjetas.Tarjeta;
import com.utndds.heladerasApi.repositories.HeladeraRepository;
import com.utndds.heladerasApi.repositories.SensoresRepositories.*;
import com.utndds.heladerasApi.repositories.TarjetasRepositories.TarjetaRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class BrokerSenderSimulator {

    private static final String TEMPERATURA_QUEUE = "sensor_temperatura_queue";
    private static final String MOVIMIENTO_QUEUE = "sensor_movimiento_queue";
    private static final String TARJETA_QUEUE = "tarjeta_queue";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SensoresRepository sensoresRepository;
    @Autowired
    private HeladeraRepository heladeraRepository;
    @Autowired
    private TarjetaRepository tarjetaRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        // scheduler.scheduleAtFixedRate(this::enviarTemperaturas, 0, 120,
        // TimeUnit.SECONDS);
        // scheduler.scheduleAtFixedRate(this::enviarSenalMovimiento, 0, 120,
        // TimeUnit.SECONDS);
        System.out.println("Scheduler iniciado. Enviando datos de sensores.");
    }

    public void enviarTemperaturas() {
        System.out.println("Ejecutando enviarTemperaturas...");
        List<Sensor> sensores = sensoresRepository.findAll();

        if (sensores.isEmpty()) {
            System.out.println("No hay sensores registrados.");
            return;
        }

        for (Sensor sensor : sensores) {
            if (sensor instanceof SensorTemperatura sensorTemperatura) {
                int temperaturaAleatoria = random.nextInt(16); // Genera temperatura entre 0 y 15
                String mensaje = String.format("{\"sensorId\":%d,\"temperatura\":%d}",
                        sensorTemperatura.getId(), temperaturaAleatoria);
                try {
                    rabbitTemplate.convertAndSend(TEMPERATURA_QUEUE, mensaje);
                    System.out.println("Temperatura enviada al broker: " + mensaje);
                } catch (Exception e) {
                    System.err.println("Error al enviar la temperatura: " + e.getMessage());
                }
            }
        }
    }

    public void enviarSenalMovimiento() {
        System.out.println("Ejecutando enviarSenalMovimiento...");
        List<Sensor> sensores = sensoresRepository.findAll();

        sensores.stream()
                .filter(sensor -> sensor instanceof SensorMovimiento)
                .findAny()
                .ifPresent(sensorMovimiento -> {
                    String mensaje = String.format("{\"sensorId\":%d,\"tipo\":\"movimiento\"}",
                            sensorMovimiento.getId());
                    try {
                        rabbitTemplate.convertAndSend(MOVIMIENTO_QUEUE, mensaje);
                        System.out.println("Señal de movimiento enviada al broker: " + mensaje);
                    } catch (Exception e) {
                        System.err.println("Error al enviar la señal de movimiento: " + e.getMessage());
                    }
                });
    }

    public void enviarSenalApertura() {
        List<Heladera> heladeras = heladeraRepository.findAll();
        List<Tarjeta> tarjetas = tarjetaRepository.findAll();

        if (heladeras.isEmpty() || tarjetas.isEmpty()) {
            System.out.println("No hay heladeras o tarjetas registradas.");
            return;
        }

        Heladera heladera = heladeras.get(random.nextInt(heladeras.size()));
        Tarjeta tarjeta = tarjetas.get(random.nextInt(tarjetas.size()));

        String mensaje = String.format("{\"heladeraId\":%d,\"tarjetaId\":%d}",
                heladera.getId(), tarjeta.getId());

        try {
            rabbitTemplate.convertAndSend(TARJETA_QUEUE, mensaje);
            System.out.println("Tarjeta enviada al broker: " + mensaje);
        } catch (Exception e) {
            System.err.println("Error al enviar la tarjeta: " + e.getMessage());
        }
    }
}
