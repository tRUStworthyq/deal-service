package ru.sber.dealservice.service;

import ru.sber.messages.ClientEvent;

public interface ClientEventService {
    void create(ClientEvent event);
    void update(ClientEvent event);
    void delete(ClientEvent event);
}
