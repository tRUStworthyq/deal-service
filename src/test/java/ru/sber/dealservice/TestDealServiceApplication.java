package ru.sber.dealservice;

import org.springframework.boot.SpringApplication;

public class TestDealServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(DealServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
