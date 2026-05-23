package ru.sber.dealservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "inn", nullable = false, unique = true, length = 12)
    private String inn;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientDeal> deals = new ArrayList<>();
}