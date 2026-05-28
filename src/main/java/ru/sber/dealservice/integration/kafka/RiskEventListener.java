package ru.sber.dealservice.integration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.sber.dealservice.service.RiskEventService;
import ru.sber.messages.EventAction;
import ru.sber.messages.RiskEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiskEventListener {

    private final ObjectMapper objectMapper;
    private final RiskEventService riskEventService;
    private final EventResultProducer eventResultProducer;

    @KafkaListener(topics = "${app.kafka.risk-events-topic}",
                   containerFactory = "entityEventListenerContainerFactory")
    public void handleRiskEvent(String message) {
        RiskEvent event = null;
        EventAction action = null;
        String entityId = "unknown";
        try {
            event = objectMapper.readValue(message, RiskEvent.class);
            action = event.action();
            entityId = event.inn() != null ? event.inn() : "unknown";

            switch (event.action()) {
                case CREATE -> riskEventService.create(event);
                case UPDATE -> riskEventService.update(event);
                case DELETE -> riskEventService.delete(event);
            }

            log.info("Processed RISK {} inn={}", action, entityId);
            eventResultProducer.publishSuccess("RISK", action, entityId);
        } catch (Exception e) {
            log.error("Failed to process RISK event: {}", message, e);
            eventResultProducer.publishFailure("RISK", action, entityId, e.getMessage());
        }
    }
}
