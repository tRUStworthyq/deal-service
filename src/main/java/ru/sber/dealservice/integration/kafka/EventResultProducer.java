package ru.sber.dealservice.integration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.sber.messages.EventAction;
import ru.sber.messages.EventResultMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventResultProducer {

    private final KafkaTemplate<String, EventResultMessage> eventResultKafkaTemplate;

    @Value("${app.kafka.event-results-topic}")
    private String eventResultsTopic;

    public void publishSuccess(String entityType, EventAction action, String entityId) {
        publish(EventResultMessage.builder()
                .entityType(entityType)
                .action(action)
                .entityId(entityId)
                .success(true)
                .build());
    }

    public void publishFailure(String entityType, EventAction action, String entityId, String errorMessage) {
        publish(EventResultMessage.builder()
                .entityType(entityType)
                .action(action)
                .entityId(entityId)
                .success(false)
                .errorMessage(errorMessage)
                .build());
    }

    private void publish(EventResultMessage result) {
        eventResultKafkaTemplate.send(eventResultsTopic, result.entityId(), result);
        log.debug("Published event result: {} {} success={}", result.entityType(), result.action(), result.success());
    }
}
