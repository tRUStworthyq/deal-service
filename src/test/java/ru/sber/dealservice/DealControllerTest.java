package ru.sber.dealservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.dealservice.dto.CalculationResponse;
import ru.sber.dealservice.dto.DealData;
import ru.sber.dealservice.dto.ExchangeRateInfoDto;
import ru.sber.dealservice.exception.DealNotFoundException;
import ru.sber.dealservice.service.DealCalculationService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class DealControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DealCalculationService calculationService;

    @MockitoBean
    @SuppressWarnings("rawtypes")
    private ReplyingKafkaTemplate replyingKafkaTemplate;

    private CalculationResponse successResponse;

    @BeforeEach
    void setUp() {
        successResponse = CalculationResponse.ok(new DealData(
                "Иванов Иван Иванович",
                new BigDecimal("120000"),
                "RUB",
                new BigDecimal("60000"),
                "RUB",
                BigDecimal.ZERO,
                "RUB",
                "Дифференцированный",
                "Хорошая",
                new ExchangeRateInfoDto(new BigDecimal("90"), new BigDecimal("100"), "2024-07-01")
        ));
    }

    @Test
    void calculate_ShouldReturn200_WithValidRequest() throws Exception {
        when(calculationService.calculate(any())).thenReturn(successResponse);

        mvc.perform(post("/api/deals/calculate")
                        .with(jwt().jwt(j -> j.subject("test-user")))
                        .header("X-API-Version", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "deal_id": "CRD-TEST-001",
                                    "calculation_date": "2024-07-01",
                                    "target_currency": "RUB"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.borrower_full_name").value("Иванов Иван Иванович"))
                .andExpect(jsonPath("$.data.credit_amount_currency").value("RUB"))
                .andExpect(jsonPath("$.data.credit_history").value("Хорошая"));
    }

    @Test
    void calculate_ShouldReturn404_WhenDealNotFound() throws Exception {
        when(calculationService.calculate(any())).thenThrow(new DealNotFoundException("UNKNOWN"));

        mvc.perform(post("/api/deals/calculate")
                        .with(jwt().jwt(j -> j.subject("test-user")))
                        .header("X-API-Version", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "deal_id": "UNKNOWN",
                                    "calculation_date": "2024-07-01",
                                    "target_currency": "RUB"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Deal not found: UNKNOWN"));
    }

    @Test
    void calculate_ShouldReturn400_WhenRequestBodyIsInvalid() throws Exception {
        mvc.perform(post("/api/deals/calculate")
                        .with(jwt().jwt(j -> j.subject("test-user")))
                        .header("X-API-Version", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "calculation_date": "2024-07-01",
                                    "target_currency": "RUB"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void calculate_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mvc.perform(post("/api/deals/calculate")
                        .header("X-API-Version", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "deal_id": "CRD-TEST-001",
                                    "calculation_date": "2024-07-01",
                                    "target_currency": "RUB"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}