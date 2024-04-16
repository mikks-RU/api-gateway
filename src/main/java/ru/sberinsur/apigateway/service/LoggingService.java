package ru.sberinsur.apigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoggingService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.logs}")
    private String logTopic;

    public void sendLog(String serviceName, String operation, String message) {
        String eventHeader = serviceName + "_" + operation;
        kafkaTemplate.send(logTopic, eventHeader, message);
    }
}
