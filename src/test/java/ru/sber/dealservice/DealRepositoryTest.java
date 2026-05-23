package ru.sber.dealservice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.sber.dealservice.entity.Client;
import ru.sber.dealservice.entity.Deal;
import ru.sber.dealservice.repository.ClientRepository;
import ru.sber.dealservice.repository.DealRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class DealRepositoryTest {

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO clients(id, full_name, inn) VALUES(?,?,?)",
                "CLT-TEST-001", "Иванов Иван Иванович", "123456789012");
        jdbcTemplate.update(
                "INSERT INTO deals(deal_number, loan_amount_rub, interest_rate, issue_date, loan_term_months, repayment_method) VALUES(?,?,?,?,?,?)",
                "CRD-TEST-001", new BigDecimal("120000.00"), new BigDecimal("0.00"), LocalDate.of(2024, 1, 1), 12, "Дифференцированный");
        jdbcTemplate.update(
                "INSERT INTO client_deals(deal_id, client_id) VALUES(?,?)",
                "CRD-TEST-001", "CLT-TEST-001");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM client_deals WHERE deal_id = ?", "CRD-TEST-001");
        jdbcTemplate.update("DELETE FROM deals WHERE deal_number = ?", "CRD-TEST-001");
        jdbcTemplate.update("DELETE FROM clients WHERE id = ?", "CLT-TEST-001");
    }

    @Test
    void findByDealNumber_shouldReturnDeal_WhenExists() {
        Optional<Deal> found = dealRepository.findByDealNumber("CRD-TEST-001");

        assertThat(found).isPresent();
        assertThat(found.get().getDealNumber()).isEqualTo("CRD-TEST-001");
        assertThat(found.get().getLoanTermMonths()).isEqualTo(12);
        assertThat(found.get().getRepaymentMethod()).isEqualTo("Дифференцированный");
    }

    @Test
    void findByDealNumber_shouldReturnEmpty_WhenNotExists() {
        Optional<Deal> found = dealRepository.findByDealNumber("UNKNOWN");

        assertThat(found).isEmpty();
    }

    @Test
    void findByDealId_shouldReturnClientByDealId() {
        Optional<Client> found = clientRepository.findByDealId("CRD-TEST-001");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Иванов Иван Иванович");
        assertThat(found.get().getInn()).isEqualTo("123456789012");
    }
}