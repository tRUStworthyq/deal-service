package ru.sber.dealservice.integration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.sber.dealservice.service.ClientEventService;
import ru.sber.messages.ClientEvent;
import ru.sber.messages.EventAction;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientEventListener {

    private final ObjectMapper objectMapper;
    private final ClientEventService clientEventService;
    private final EventResultProducer eventResultProducer;

    @KafkaListener(topics = "${app.kafka.client-events-topic}",
                   containerFactory = "entityEventListenerContainerFactory")
    public void handleClientEvent(String message) {
        ClientEvent event = null;
        EventAction action = null;
        String entityId = "unknown";
        try {
            event = objectMapper.readValue(message, ClientEvent.class);
            action = event.action();
            entityId = event.id() != null ? event.id() : "unknown";

            switch (event.action()) {
                case CREATE -> clientEventService.create(event);
                case UPDATE -> clientEventService.update(event);
                case DELETE -> clientEventService.delete(event);
            }

            log.info("Processed CLIENT {} id={}", action, entityId);
            eventResultProducer.publishSuccess("CLIENT", action, entityId);
        } catch (Exception e) {
            log.error("Failed to process CLIENT event: {}", message, e);
            eventResultProducer.publishFailure("CLIENT", action, entityId, e.getMessage());
        }
    }
}
