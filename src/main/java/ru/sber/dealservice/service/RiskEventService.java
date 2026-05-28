package ru.sber.dealservice.service;

import ru.sber.messages.RiskEvent;

public interface RiskEventService {
    void create(RiskEvent event);
    void update(RiskEvent event);
    void delete(RiskEvent event);
}
