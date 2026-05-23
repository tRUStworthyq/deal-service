package ru.sber.dealservice.integration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Component;
import ru.sber.messages.RiskRequestMessage;
import ru.sber.messages.RiskResponseMessage;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiskServiceGateway {

    private final ReplyingKafkaTemplate<String, RiskRequestMessage, RiskResponseMessage> replyingKafkaTemplate;

    @Value("${app.kafka.request-topic:risk-requests}")
    private String requestTopic;

    @Value("${app.kafka.reply-timeout-ms:10000}")
    private long replyTimeoutMs;

    public String getCreditHistory(String inn) {
        log.debug("Requesting credit history for INN: {}", inn);
        ProducerRecord<String, RiskRequestMessage> record =
                new ProducerRecord<>(requestTopic, RiskRequestMessage.builder().inn(inn).build());

        try {
            RequestReplyFuture<String, RiskRequestMessage, RiskResponseMessage> future =
                    replyingKafkaTemplate.sendAndReceive(record);
            RiskResponseMessage response = future.get(replyTimeoutMs, TimeUnit.MILLISECONDS).value();
            log.debug("Received credit history for INN {}: {}", inn, response.creditHistory());
            return response.creditHistory();
        } catch (Exception e) {
            log.error("Failed to get credit history for INN {}: {}", inn, e.getMessage());
            throw new RuntimeException("Failed to get credit history from risk-service", e);
        }
    }
}