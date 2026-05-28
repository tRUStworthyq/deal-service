package ru.sber.dealservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.dealservice.entity.Client;
import ru.sber.dealservice.entity.RiskProfile;
import ru.sber.dealservice.repository.ClientDealRepository;
import ru.sber.dealservice.repository.ClientRepository;
import ru.sber.dealservice.repository.DealRepository;
import ru.sber.dealservice.repository.RiskProfileRepository;
import ru.sber.dealservice.service.ClientEventService;
import ru.sber.messages.ClientEvent;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultClientEventService implements ClientEventService {

    private final ClientRepository clientRepository;
    private final ClientDealRepository clientDealRepository;
    private final DealRepository dealRepository;
    private final RiskProfileRepository riskProfileRepository;

    @Override
    @Transactional
    public void create(ClientEvent event) {
        Client client = Client.builder()
                .id(event.id())
                .fullName(event.fullName())
                .inn(event.inn())
                .build();
        clientRepository.save(client);
        log.debug("Created client id={}", event.id());
    }

    @Override
    @Transactional
    public void update(ClientEvent event) {
        Client client = clientRepository.findById(event.id())
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + event.id()));

        String oldInn = client.getInn();
        client.setFullName(event.fullName());
        client.setInn(event.inn());

        if (!oldInn.equals(event.inn())) {
            riskProfileRepository.findById(oldInn).ifPresent(rp -> {
                String history = rp.getCreditHistory();
                riskProfileRepository.delete(rp);
                riskProfileRepository.save(RiskProfile.builder()
                        .inn(event.inn())
                        .creditHistory(history)
                        .build());
            });
        }

        log.debug("Updated client id={}", event.id());
    }

    @Override
    @Transactional
    public void delete(ClientEvent event) {
        List<String> dealNumbers = clientDealRepository.findDealIdsByClientId(event.id());
        if (!dealNumbers.isEmpty()) {
            dealRepository.deleteAllByDealNumbers(dealNumbers);
        }
        clientDealRepository.deleteAllByClientId(event.id());
        clientRepository.deleteDirectById(event.id());
        log.debug("Deleted client id={} with {} deals", event.id(), dealNumbers.size());
    }
}
