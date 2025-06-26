package com.gestionemployes.employee_management_api.integration.controller;

import com.gestionemployes.employee_management_api.dto.DepartementSalarySummaryDto;
import com.gestionemployes.employee_management_api.dto.PresenceTrendReportDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class RapportControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("test_db")
            .withUsername("testuser")
            .withPassword("testpassword");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/api/rapports";
    }

    @Test
    void getTendancesPresences_shouldReturnPresenceTrendReport() {
        ResponseEntity<PresenceTrendReportDto> response = restTemplate.exchange(
                baseUrl() + "/tendances-presences",
                HttpMethod.GET,
                null,
                PresenceTrendReportDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
    }

    @Test
    void getResumeSalairesByDepartement_shouldReturnSummaryList() {
        ResponseEntity<List<DepartementSalarySummaryDto>> response = restTemplate.exchange(
                baseUrl() + "/resume-salaires",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<DepartementSalarySummaryDto>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
    }
}
