package ru.sber.dealservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "risk_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskProfile {

    @Id
    @Column(name = "inn", length = 12)
    private String inn;

    @Column(name = "credit_history", nullable = false, length = 10)
    private String creditHistory;
}
