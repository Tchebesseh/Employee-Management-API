package com.gestionemployes.employee_management_api.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gestionemployes.employee_management_api.dto.EmployeDto;
import com.gestionemployes.employee_management_api.dto.EmployeRequest;
import com.gestionemployes.employee_management_api.dto.PageResponse;
import com.gestionemployes.employee_management_api.model.Departement;
import com.gestionemployes.employee_management_api.model.Employe;
import com.gestionemployes.employee_management_api.model.Presence;
import com.gestionemployes.employee_management_api.repository.DepartementRepository;
import com.gestionemployes.employee_management_api.repository.EmployeRepository;
import com.gestionemployes.employee_management_api.repository.PresenceRepository;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class EmployeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmployeRepository employeRepository;
    @Autowired
    private DepartementRepository departementRepository;
    @Autowired
    private PresenceRepository presenceRepository;

    private ObjectMapper objectMapper;

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

    private Departement savedDepartement;
    private String baseUrl;

    @BeforeEach
    @Transactional
    void setUp() {
        presenceRepository.deleteAll();
        employeRepository.deleteAll();
        departementRepository.deleteAll();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Departement departement = new Departement();
        departement.setNom("Comptabilité");
        departement.setBudget(BigDecimal.valueOf(250000));
        departement.setManagerId(null);
        savedDepartement = departementRepository.save(departement);

        baseUrl = "http://localhost:" + port + "/api/employes";
    }

    @Test
    void createEmploye_shouldReturnCreatedEmployeDto() {
        EmployeRequest employeRequest = new EmployeRequest(
                "Moussa", "Konate", "moussa.konate@example.com", savedDepartement.getId(),
                BigDecimal.valueOf(55000), LocalDate.of(2023, 8, 20), "ACTIF"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeRequest> requestEntity = new HttpEntity<>(employeRequest, headers);

        ResponseEntity<EmployeDto> response = restTemplate.postForEntity(baseUrl, requestEntity, EmployeDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNom()).isEqualTo("Konate");
        assertThat(response.getBody().getPrenom()).isEqualTo("Moussa");
        assertThat(response.getBody().getEmail()).isEqualTo("moussa.konate@example.com");
        assertThat(response.getBody().getDepartementId()).isEqualTo(savedDepartement.getId());
        assertThat(response.getBody().getSalaire()).isEqualByComparingTo(BigDecimal.valueOf(55000));
        assertThat(response.getBody().getStatut()).isEqualTo("ACTIF");
        assertThat(response.getBody().getId()).isNotNull();

        Optional<Employe> foundEmploye = employeRepository.findById(response.getBody().getId());
        assertTrue(foundEmploye.isPresent());
        assertThat(foundEmploye.get().getEmail()).isEqualTo("moussa.konate@example.com");
    }

    @Test
    void createEmploye_shouldReturnBadRequestForDuplicateEmail() throws Exception {
        Employe existingEmploye = new Employe(
                "Adja", "Thiam", "adja.thiam@example.com", savedDepartement,
                BigDecimal.valueOf(42000), LocalDate.of(2022, 5, 10), "ACTIF"
        );
        employeRepository.save(existingEmploye);

        EmployeRequest duplicateRequest = new EmployeRequest(
                "Aya", "Kone", "adja.thiam@example.com", savedDepartement.getId(),
                BigDecimal.valueOf(48000), LocalDate.of(2023, 1, 1), "ACTIF"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeRequest> requestEntity = new HttpEntity<>(duplicateRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.path("message").asText()).contains("Un employé avec l'email 'adja.thiam@example.com' existe déjà.");
    }

    @Test
    void createEmploye_shouldReturnBadRequestWhenDepartementIdIsNull() throws Exception {
        EmployeRequest invalidRequest = new EmployeRequest(
                "Fanta", "Camara", "fanta.camara@example.com", null,
                BigDecimal.valueOf(32000), LocalDate.of(2023, 4, 1), "ACTIF"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeRequest> requestEntity = new HttpEntity<>(invalidRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        System.out.println("Réponse JSON reçue : " + response.getBody());

        JsonNode root = objectMapper.readTree(response.getBody());

        assertThat(root.has("errors")).isTrue();

        JsonNode errorsNode = root.path("errors");

        assertThat(errorsNode.has("departementId")).isTrue();

        assertThat(errorsNode.path("departementId").asText())
                .contains("L'ID du département ne peut pas être nul");
    }

    @Test
    void createEmploye_shouldReturnBadRequestWhenNomIsEmpty() throws Exception {
        EmployeRequest invalidRequest = new EmployeRequest(
                "Mamadou", "", "mamadou.d@example.com", savedDepartement.getId(),
                BigDecimal.valueOf(35000), LocalDate.of(2023, 2, 1), "ACTIF"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeRequest> requestEntity = new HttpEntity<>(invalidRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.path("errors").path("nom").asText()).contains("Le nom ne peut pas être vide");
    }

    @Test
    void createEmploye_shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        EmployeRequest invalidRequest = new EmployeRequest(
                "Assetou", "Diakite", "invalid-email-format", savedDepartement.getId(),
                BigDecimal.valueOf(38000), LocalDate.of(2023, 7, 1), "ACTIF"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeRequest> requestEntity = new HttpEntity<>(invalidRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.path("errors").path("email").asText()).contains("L'email doit être au format valide");
    }

    @Test
    void getAllEmployes_shouldReturnListOfEmployesDto() {
        employeRepository.save(new Employe("Fatoumata", "Traore", "fatoumata.traore@example.com", savedDepartement, BigDecimal.valueOf(40000), LocalDate.of(2021, 5, 10), "ACTIF"));
        employeRepository.save(new Employe("Kouadio", "Kouassi", "kouadio.kouassi@example.com", savedDepartement, BigDecimal.valueOf(50000), LocalDate.of(2022, 7, 20), "ACTIF"));

        ResponseEntity<PageResponse<EmployeDto>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PageResponse<EmployeDto>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);
        List<EmployeDto> employes = response.getBody().getContent();
        assertTrue(employes.stream().anyMatch(e -> "fatoumata.traore@example.com".equals(e.getEmail())));
        assertTrue(employes.stream().anyMatch(e -> "kouadio.kouassi@example.com".equals(e.getEmail())));
        assertThat(response.getBody().getTotalElements()).isEqualTo(2);
        assertThat(response.getBody().getNumber()).isEqualTo(0);
    }

    @Test
    void getEmployeById_shouldReturnEmployeDto() {
        Employe employe = new Employe("Nafy", "Diop", "nafy.diop@example.com", savedDepartement, BigDecimal.valueOf(45000), LocalDate.of(2020, 1, 1), "ACTIF");
        employe = employeRepository.save(employe);

        ResponseEntity<EmployeDto> response = restTemplate.getForEntity(baseUrl + "/{id}", EmployeDto.class, employe.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("nafy.diop@example.com");
        assertThat(response.getBody().getId()).isEqualTo(employe.getId());
    }

    @Test
    void getEmployeById_shouldReturnNotFoundWhenEmployeDoesNotExist() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/{id}", String.class, 999L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateEmploye_shouldReturnUpdatedEmployeDto() {
        Employe existingEmploye = new Employe(
                "Fatim", "Ouattara", "fatim.ouattara@example.com", savedDepartement,
                BigDecimal.valueOf(30000), LocalDate.of(2020, 1, 1), "ACTIF"
        );
        existingEmploye = employeRepository.save(existingEmploye);

        EmployeRequest updatedRequest = new EmployeRequest(
                "Fatim", "Ouattara Nouveau", "fatim.ouattara.new@example.com", savedDepartement.getId(),
                BigDecimal.valueOf(37000), LocalDate.of(2020, 1, 1), "INACTIF"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeRequest> requestEntity = new HttpEntity<>(updatedRequest, headers);

        ResponseEntity<EmployeDto> response = restTemplate.exchange(baseUrl + "/{id}", HttpMethod.PUT, requestEntity, EmployeDto.class, existingEmploye.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNom()).isEqualTo("Ouattara Nouveau");
        assertThat(response.getBody().getEmail()).isEqualTo("fatim.ouattara.new@example.com");
        assertThat(response.getBody().getStatut()).isEqualTo("INACTIF");
    }

    @Test
    void updateEmploye_shouldReturnNotFoundWhenEmployeToUpdateDoesNotExist() {
        EmployeRequest request = new EmployeRequest(
                "NonExistant", "Employe", "nonexistent.employe@example.com", savedDepartement.getId(),
                BigDecimal.valueOf(100), LocalDate.now(), "ACTIF"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/{id}", HttpMethod.PUT, requestEntity, String.class, 999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deactivateEmploye_shouldReturnNoContent() {
        Employe employe = new Employe("Lassina", "Traore", "lassina.traore@example.com", savedDepartement, BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF");
        employe = employeRepository.save(employe);

        restTemplate.delete(baseUrl + "/{id}", employe.getId());

        Employe deactivatedEmploye = employeRepository.findById(employe.getId()).orElseThrow();
        assertThat(deactivatedEmploye.getStatut()).isEqualTo("INACTIF");
    }

    @Test
    void deactivateEmploye_shouldReturnBadRequestIfPresenceExists() throws Exception {
        Employe employe = new Employe("Aicha", "Coulibaly", "aicha.c@example.com", savedDepartement, BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF");
        employe = employeRepository.save(employe);
        presenceRepository.save(new Presence(employe, LocalDate.now(), LocalTime.of(9,0)));

        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/{id}", HttpMethod.DELETE, null, String.class, employe.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.path("message").asText()).contains("Impossible de désactiver l'employé avec l'ID " + employe.getId() + " car il a des enregistrements de présence associés.");
    }

    @Test
    void deactivateEmploye_shouldReturnNotFoundWhenEmployeDoesNotExist() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/{id}", HttpMethod.DELETE, null, String.class, 999L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
