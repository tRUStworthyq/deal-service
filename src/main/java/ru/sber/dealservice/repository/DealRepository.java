package ru.sber.dealservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.dealservice.entity.Deal;

import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    Optional<Deal> findByDealNumber(String dealNumber);
}