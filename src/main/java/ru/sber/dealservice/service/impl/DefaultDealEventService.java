package ru.sber.dealservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.dealservice.entity.ClientDeal;
import ru.sber.dealservice.entity.Deal;
import ru.sber.dealservice.repository.ClientDealRepository;
import ru.sber.dealservice.repository.ClientRepository;
import ru.sber.dealservice.repository.DealRepository;
import ru.sber.dealservice.service.DealEventService;
import ru.sber.messages.DealEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultDealEventService implements DealEventService {

    private final DealRepository dealRepository;
    private final ClientDealRepository clientDealRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public void create(DealEvent event) {
        Deal deal = Deal.builder()
                .dealNumber(event.dealNumber())
                .loanAmountRub(event.loanAmountRub())
                .interestRate(event.interestRate())
                .issueDate(event.issueDate())
                .loanTermMonths(event.loanTermMonths())
                .repaymentMethod(event.repaymentMethod())
                .build();
        dealRepository.save(deal);

        ClientDeal clientDeal = ClientDeal.builder()
                .dealId(event.dealNumber())
                .client(clientRepository.getReferenceById(event.clientId()))
                .build();
        clientDealRepository.save(clientDeal);

        log.debug("Created deal dealNumber={} clientId={}", event.dealNumber(), event.clientId());
    }

    @Override
    @Transactional
    public void update(DealEvent event) {
        Deal deal = dealRepository.findByDealNumber(event.dealNumber())
                .orElseThrow(() -> new EntityNotFoundException("Deal not found: " + event.dealNumber()));
        deal.setLoanAmountRub(event.loanAmountRub());
        deal.setInterestRate(event.interestRate());
        deal.setIssueDate(event.issueDate());
        deal.setLoanTermMonths(event.loanTermMonths());
        deal.setRepaymentMethod(event.repaymentMethod());

        clientDealRepository.findById(event.dealNumber()).ifPresent(cd ->
                cd.setClient(clientRepository.getReferenceById(event.clientId())));

        log.debug("Updated deal dealNumber={} clientId={}", event.dealNumber(), event.clientId());
    }

    @Override
    @Transactional
    public void delete(DealEvent event) {
        clientDealRepository.deleteByDealId(event.dealNumber());
        int deleted = dealRepository.deleteByDealNumber(event.dealNumber());
        if (deleted > 0) {
            log.debug("Deleted deal dealNumber={}", event.dealNumber());
        }
    }
}
