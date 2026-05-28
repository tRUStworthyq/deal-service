package ru.sber.dealservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.dealservice.entity.RiskProfile;

@Repository
public interface RiskProfileRepository extends JpaRepository<RiskProfile, String> {}
