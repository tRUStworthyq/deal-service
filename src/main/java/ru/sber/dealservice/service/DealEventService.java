package ru.sber.dealservice.service;

import ru.sber.messages.DealEvent;

public interface DealEventService {
    void create(DealEvent event);
    void update(DealEvent event);
    void delete(DealEvent event);
}
