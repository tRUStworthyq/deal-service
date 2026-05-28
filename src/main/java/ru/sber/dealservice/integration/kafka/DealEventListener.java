package ru.sber.dealservice.integration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.sber.dealservice.service.DealEventService;
import ru.sber.messages.DealEvent;
import ru.sber.messages.EventAction;

@Component
@RequiredArgsConstructor
@Slf4j
public class DealEventListener {

    private final ObjectMapper objectMapper;
    private final DealEventService dealEventService;
    private final EventResultProducer eventResultProducer;

    @KafkaListener(topics = "${app.kafka.deal-events-topic}",
                   containerFactory = "entityEventListenerContainerFactory")
    public void handleDealEvent(String message) {
        DealEvent event = null;
        EventAction action = null;
        String entityId = "unknown";
        try {
            event = objectMapper.readValue(message, DealEvent.class);
            action = event.action();
            entityId = event.dealNumber() != null ? event.dealNumber() : "unknown";

            switch (event.action()) {
                case CREATE -> dealEventService.create(event);
                case UPDATE -> dealEventService.update(event);
                case DELETE -> dealEventService.delete(event);
            }

            log.info("Processed DEAL {} dealNumber={}", action, entityId);
            eventResultProducer.publishSuccess("DEAL", action, entityId);
        } catch (Exception e) {
            log.error("Failed to process DEAL event: {}", message, e);
            eventResultProducer.publishFailure("DEAL", action, entityId, e.getMessage());
        }
    }
}
