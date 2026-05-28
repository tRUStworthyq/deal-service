package ru.sber.dealservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.dealservice.entity.ClientDeal;

import java.util.List;

@Repository
public interface ClientDealRepository extends JpaRepository<ClientDeal, String> {

    @Query("SELECT cd.dealId FROM ClientDeal cd WHERE cd.client.id = :clientId")
    List<String> findDealIdsByClientId(String clientId);

    @Modifying
    @Query("DELETE FROM ClientDeal cd WHERE cd.client.id = :clientId")
    void deleteAllByClientId(String clientId);

    @Modifying
    @Query("DELETE FROM ClientDeal cd WHERE cd.dealId = :dealId")
    void deleteByDealId(String dealId);
}
