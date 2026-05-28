package ru.sber.dealservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.dealservice.entity.RiskProfile;
import ru.sber.dealservice.repository.RiskProfileRepository;
import ru.sber.dealservice.service.RiskEventService;
import ru.sber.messages.RiskEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultRiskEventService implements RiskEventService {

    private final RiskProfileRepository riskProfileRepository;

    @Override
    @Transactional
    public void create(RiskEvent event) {
        RiskProfile riskProfile = RiskProfile.builder()
                .inn(event.inn())
                .creditHistory(event.creditHistory())
                .build();
        riskProfileRepository.save(riskProfile);
        log.debug("Created risk profile inn={}", event.inn());
    }

    @Override
    @Transactional
    public void update(RiskEvent event) {
        RiskProfile riskProfile = riskProfileRepository.findById(event.inn())
                .orElseThrow(() -> new EntityNotFoundException("RiskProfile not found: " + event.inn()));
        riskProfile.setCreditHistory(event.creditHistory());
        log.debug("Updated risk profile inn={}", event.inn());
    }

    @Override
    @Transactional
    public void delete(RiskEvent event) {
        riskProfileRepository.deleteById(event.inn());
        log.debug("Deleted risk profile inn={}", event.inn());
    }
}
