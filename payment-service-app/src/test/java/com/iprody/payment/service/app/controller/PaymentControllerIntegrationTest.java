package com.iprody.payment.service.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iprody.payment.service.app.AbstractPostgresIntegrationTest;
import com.iprody.payment.service.app.TestJwtFactory;
import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.persistence.PaymentRepository;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Import(PaymentControllerIntegrationTest.TestClockConfig.class)
public class PaymentControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-03-03T12:15:55Z");

    @TestConfiguration
    static class TestClockConfig {
        @Primary
        @Bean
        Clock clock() {
            return Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void shouldReturnOnlyLiquibasePayments() throws Exception {
        mockMvc.perform(get("/payment/search")
                        .with(
                                TestJwtFactory.jwtWithRole("test-user",      "reader")
                        )
                        .param("page", "0")
                        .param("size", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.guid=='00000000-0000-0000-0000-000000000001')]").exists())
                .andExpect(jsonPath("$.content[?(@.guid=='00000000-0000-0000-0000-000000000002')]").exists())
                .andExpect(jsonPath("$.content[?(@.guid=='00000000-0000-0000-0000-000000000003')]").exists());
    }

    @Test
    void shouldCreatePaymentAndVerifyInDatabase() throws Exception {
        PaymentDto dto = new PaymentDto();
        dto.setAmount(new BigDecimal("123.45"));
        dto.setInquiryRefId(UUID.randomUUID());
        dto.setCurrency("EUR");
        dto.setStatus(PaymentStatus.PENDING);

        String json = objectMapper.writeValueAsString(dto);

        String response = mockMvc.perform(post("/payment")
                        .with(
                                TestJwtFactory.jwtWithRole("test-user", "admin")
                        )

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guid").exists())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.amount").value(123.45))
                .andExpect(jsonPath("$.createdAt").value("2026-03-03T12:15:55Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-03-03T12:15:55Z"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        PaymentDto created = objectMapper.readValue(response, PaymentDto.class);

        Optional<Payment> saved = paymentRepository.findById(created.getGuid());
        assertThat(saved).isPresent();
        assertThat(saved.get().getCurrency()).isEqualTo("EUR");
        assertThat(saved.get().getAmount()).isEqualByComparingTo("123.45");
        assertThat(saved.get().getCreatedAt().toInstant()).isEqualTo(FIXED_INSTANT);
        assertThat(saved.get().getUpdatedAt().toInstant()).isEqualTo(FIXED_INSTANT);
    }
}
