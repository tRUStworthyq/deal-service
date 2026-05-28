package ru.sber.dealservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sber.dealservice.entity.Deal;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {

    Optional<Deal> findByDealNumber(String dealNumber);

    @Modifying
    @Query("DELETE FROM Deal d WHERE d.dealNumber IN :dealNumbers")
    void deleteAllByDealNumbers(@Param("dealNumbers") Collection<String> dealNumbers);

    @Modifying
    @Query("DELETE FROM Deal d WHERE d.dealNumber = :dealNumber")
    int deleteByDealNumber(@Param("dealNumber") String dealNumber);
}
