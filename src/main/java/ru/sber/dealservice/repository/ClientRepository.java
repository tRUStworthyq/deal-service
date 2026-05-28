package ru.sber.dealservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.dealservice.entity.Client;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    @Query("SELECT c FROM Client c JOIN c.deals d WHERE d.dealId = :dealId")
    Optional<Client> findByDealId(String dealId);

    @Modifying
    @Query("DELETE FROM Client c WHERE c.id = :id")
    void deleteDirectById(String id);
}
