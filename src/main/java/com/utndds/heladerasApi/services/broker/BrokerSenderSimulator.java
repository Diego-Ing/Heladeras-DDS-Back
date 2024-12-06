package com.utndds.heladerasApi.services.broker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.utndds.heladerasApi.config.RabbitMQConfig;
import com.utndds.heladerasApi.models.Heladera.Heladera;
import com.utndds.heladerasApi.models.Heladera.Sensores.Sensor;
import com.utndds.heladerasApi.models.Heladera.Sensores.SensorMovimiento;
import com.utndds.heladerasApi.models.Heladera.Sensores.SensorTemperatura;
import com.utndds.heladerasApi.models.Tarjetas.Tarjeta;
import com.utndds.heladerasApi.repositories.HeladeraRepository;
import com.utndds.heladerasApi.repositories.SensoresRepositories.*;
import com.utndds.heladerasApi.repositories.TarjetasRepositories.TarjetaRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
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
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private SensoresRepository sensoresRepository;
    @Autowired
    private HeladeraRepository heladeraRepository;
    @Autowired
    private TarjetaRepository tarjetaRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Random random = new Random();
    private Connection connection;
    private Channel channel;

    @PostConstruct
    public void init() {
        try {
            connection = rabbitMQConfig.crearConexion();
            channel = connection.createChannel();
            channel.queueDeclare(TEMPERATURA_QUEUE, false, false, false, null);
            channel.queueDeclare(MOVIMIENTO_QUEUE, false, false, false, null);
            channel.queueDeclare(TARJETA_QUEUE, false, false, false, null);

            scheduler.scheduleAtFixedRate(this::enviarTemperaturas, 0, 15, TimeUnit.SECONDS);
            scheduler.scheduleAtFixedRate(this::enviarSenalMovimiento, 0, 15, TimeUnit.SECONDS);
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
            return; // Salir si no hay sensores
        }

        // Generar temperatura aleatoria entre 15 y 30
        int temperaturaAleatoria = random.nextInt(16); // => [0, 15]

        for (Sensor sensor : sensores) {
            if (sensor instanceof SensorTemperatura) {
                SensorTemperatura sensorTemperatura = (SensorTemperatura) sensor;
                System.out.println(
                        "ID: " + sensorTemperatura.getId() + ", Temperatura generada: " + temperaturaAleatoria);

                // Enviar la temperatura al broker
                try {
                    String mensaje = String.format("{\"sensorId\":%d,\"temperatura\":%d}",
                            sensorTemperatura.getId(), temperaturaAleatoria);
                    channel.basicPublish("", TEMPERATURA_QUEUE, null, mensaje.getBytes(StandardCharsets.UTF_8));
                    System.out.println("Temperatura enviada al broker: " + mensaje);
                } catch (Exception e) {
                    System.err.println("Error al enviar la temperatura: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    // Método que envía una señal del sensor de movimiento al broker
    public void enviarSenalMovimiento() {
        List<Sensor> sensores = sensoresRepository.findAll();

        System.out.println("Sensores encontrados: " + sensores.size());

        if (!sensores.isEmpty()) {
            // Filtrar por SensorMovimiento
            sensores.stream()
                    .filter(sensor -> sensor instanceof SensorMovimiento)
                    .findAny()
                    .ifPresent(sensorMovimiento -> {
                        SensorMovimiento sensor = (SensorMovimiento) sensorMovimiento;
                        String mensaje = String.format("{\"sensorId\":%d,\"tipo\":\"movimiento\"}",
                                sensor.getId());

                        try {
                            channel.basicPublish("", MOVIMIENTO_QUEUE, null, mensaje.getBytes(StandardCharsets.UTF_8));
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

        // Pick a random Heladera
        Heladera heladera = heladeras.get(random.nextInt(heladeras.size()));
        Long heladeraId = heladera.getId();

        Tarjeta tarjeta = tarjetas.get(random.nextInt(tarjetas.size()));
        Long tarjetaId = tarjeta.getId(); // Random tarjeta ID for simulation

        String mensaje = String.format("{\"heladeraId\":%d,\"tarjetaId\":%d}", heladeraId, tarjetaId);

        try {
            channel.basicPublish("", TARJETA_QUEUE, null, mensaje.getBytes(StandardCharsets.UTF_8));
            System.out.println("Tarjeta enviada al broker: " + mensaje);
        } catch (Exception e) {
            System.err.println("Error al enviar la tarjeta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cierre de conexión y canal cuando el servicio se destruye
    @PreDestroy
    public void cleanUp() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}