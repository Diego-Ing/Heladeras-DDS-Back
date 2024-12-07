package com.utndds.heladerasApi.services.broker;

import com.utndds.heladerasApi.models.Heladera.Heladera;
import com.utndds.heladerasApi.models.Heladera.Sensores.Sensor;
import com.utndds.heladerasApi.models.Heladera.Sensores.SensorMovimiento;
import com.utndds.heladerasApi.models.Heladera.Sensores.SensorTemperatura;
import com.utndds.heladerasApi.models.Tarjetas.Tarjeta;
import com.utndds.heladerasApi.repositories.HeladeraRepository;
import com.utndds.heladerasApi.repositories.SensoresRepositories.SensoresRepository;
import com.utndds.heladerasApi.repositories.TarjetasRepositories.TarjetaRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
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
    private RabbitTemplate rabbitTemplate; // Spring AMQP RabbitTemplate

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
        try {
            // Planificar tareas periódicas
            scheduler.scheduleAtFixedRate(this::enviarTemperaturas, 0, 500000, TimeUnit.SECONDS);
            scheduler.scheduleAtFixedRate(this::enviarSenalMovimiento, 600000, 5, TimeUnit.SECONDS);
            System.out.println("Scheduler iniciado. Enviando datos de sensores.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enviarTemperaturas() {
        System.out.println("Ejecutando enviarTemperaturas..."); // Mensaje de depuración
        List<Sensor> sensores = sensoresRepository.findAll();

        if (sensores.isEmpty()) {
            System.out.println("No hay sensores registrados.");
            return;
        }

        // Generar temperatura aleatoria entre 15 y 30
        int temperaturaAleatoria = 15 + random.nextInt(16);

        for (Sensor sensor : sensores) {
            if (sensor instanceof SensorTemperatura) {
                SensorTemperatura sensorTemperatura = (SensorTemperatura) sensor;
                String mensaje = String.format("{\"sensorId\":%d,\"temperatura\":%d}",
                        sensorTemperatura.getId(), temperaturaAleatoria);
                try {
                    rabbitTemplate.convertAndSend(TEMPERATURA_QUEUE, mensaje.getBytes(StandardCharsets.UTF_8));
                    System.out.println("Temperatura enviada al broker: " + mensaje);
                } catch (Exception e) {
                    System.err.println("Error al enviar la temperatura: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public void enviarSenalMovimiento() {
        List<Sensor> sensores = sensoresRepository.findAll();

        System.out.println("Sensores encontrados: " + sensores.size());

        if (!sensores.isEmpty()) {
            sensores.stream()
                    .filter(sensor -> sensor instanceof SensorMovimiento)
                    .findAny()
                    .ifPresent(sensorMovimiento -> {
                        SensorMovimiento sensor = (SensorMovimiento) sensorMovimiento;
                        String mensaje = String.format("{\"sensorId\":%d,\"tipo\":\"movimiento\"}",
                                sensor.getId());
                        try {
                            rabbitTemplate.convertAndSend(MOVIMIENTO_QUEUE, mensaje.getBytes(StandardCharsets.UTF_8));
                            System.out.println("Señal de movimiento enviada al broker: " + mensaje);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    public void enviarSenalApertura() {
        List<Heladera> heladeras = heladeraRepository.findAll();
        List<Tarjeta> tarjetas = tarjetaRepository.findAll();

        if (heladeras.isEmpty()) {
            System.out.println("No hay heladeras registradas.");
            return;
        }
        if (tarjetas.isEmpty()) {
            System.out.println("No hay tarjetas registradas.");
            return;
        }

        Heladera heladera = heladeras.get(random.nextInt(heladeras.size()));
        Long heladeraId = heladera.getId();

        Tarjeta tarjeta = tarjetas.get(random.nextInt(tarjetas.size()));
        Long tarjetaId = tarjeta.getId();

        String mensaje = String.format("{\"heladeraId\":%d,\"tarjetaId\":%d}", heladeraId, tarjetaId);

        try {
            rabbitTemplate.convertAndSend(TARJETA_QUEUE, mensaje.getBytes(StandardCharsets.UTF_8));
            System.out.println("Tarjeta enviada al broker: " + mensaje);
        } catch (Exception e) {
            System.err.println("Error al enviar la tarjeta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void cleanUp() {
        scheduler.shutdown();
        System.out.println("Scheduler detenido.");
    }
}
