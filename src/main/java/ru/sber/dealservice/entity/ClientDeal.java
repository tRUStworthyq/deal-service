package ru.sber.dealservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "client_deals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDeal {

    @Id
    @Column(name = "deal_id", length = 50)
    private String dealId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
}