package com.utndds.heladerasApi.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;


@Configuration
public class RabbitMQConfig {

    private static final String HOST = "jackal.rmq.cloudamqp.com";
    private static final int PORT = 5672;
    private static final String USERNAME = "beqoxnod";
    private static final String PASSWORD = "VvPUHlc3GShdRUTQ-YOUuWGa4yHC1ZPI";
    private static final String VIRTUAL_HOST = "beqoxnod";

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        factory.setVirtualHost(VIRTUAL_HOST);
        return factory;
    }
}
