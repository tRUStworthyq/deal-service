package ru.sber.dealservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "deals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deal_number", unique = true, nullable = false, length = 50)
    private String dealNumber;

    @Column(name = "loan_amount_rub", nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmountRub;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "loan_term_months", nullable = false)
    private Integer loanTermMonths;

    @Column(name = "repayment_method", nullable = false, length = 50)
    private String repaymentMethod;
}