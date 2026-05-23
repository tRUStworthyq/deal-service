package ru.sber.dealservice.controller;

import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sber.dealservice.dto.CalculationRequest;
import ru.sber.dealservice.dto.CalculationResponse;
import ru.sber.dealservice.service.DealCalculationService;

@Slf4j
@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Tag(name = "Кредитные сделки", description = "Расчёт остатка и параметров кредитной сделки")
public class DealController {

    private final DealCalculationService calculationService;

    @Operation(
            summary = "Рассчитать параметры кредитной сделки",
            description = "Возвращает остаток задолженности на дату и через год, " +
                    "сумму кредита и историю кредитных платежей заёмщика",
            operationId = "calculateDeal"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Расчёт выполнен успешно",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректная дата расчёта или параметры запроса",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Сделка не найдена",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping(value = "/calculate", version = "1",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed(value = "deal.calculate", description = "Время расчёта параметров кредитной сделки")
    public ResponseEntity<CalculationResponse> calculate(
            @Valid @RequestBody CalculationRequest request,
            Authentication authentication) {
        log.info("Deal calculation requested by user: {}", authentication.getName());
        return ResponseEntity.ok(calculationService.calculate(request));
    }
}