package com.utndds.heladerasApi.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Utiliza la URL AMQP proporcionada por CloudAMQP
    private static final String HOST = "jackal.rmq.cloudamqp.com";
    private static final int PORT = 5672;
    private static final String USERNAME = "beqoxnod";
    private static final String PASSWORD = "VvPUHlc3GShdRUTQ-YOUuWGa4yHC1ZPI";
    private static final String VIRTUAL_HOST = "beqoxnod";

    @Bean
    public Connection crearConexion() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST);
            factory.setPort(PORT);
            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            factory.setVirtualHost(VIRTUAL_HOST); // Configura el virtual host

            return factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException("Failed to create RabbitMQ connection", e);
        }
    }
}
