package com.gestionemployes.employee_management_api.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gestionemployes.employee_management_api.dto.DepartementDto;
import com.gestionemployes.employee_management_api.dto.DepartementRequest;
import com.gestionemployes.employee_management_api.dto.PageResponse;
import com.gestionemployes.employee_management_api.model.Departement;
import com.gestionemployes.employee_management_api.model.Employe;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class DepartementControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private EmployeRepository employeRepository;

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

    private String baseUrl;

    @BeforeEach
    @Transactional
    void setUp() {
        presenceRepository.deleteAll();
        employeRepository.deleteAll();
        departementRepository.deleteAll();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        baseUrl = "http://localhost:" + port + "/api/departements";
    }

    @Test
    void createDepartement_shouldReturnCreatedDepartementDto() {
        DepartementRequest request = new DepartementRequest("Marketing", null, BigDecimal.valueOf(300000));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DepartementRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<DepartementDto> response = restTemplate.postForEntity(baseUrl, requestEntity, DepartementDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNom()).isEqualTo("Marketing");
        assertThat(response.getBody().getBudget()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    void createDepartement_shouldReturnBadRequestForDuplicateName() throws Exception {
        departementRepository.save(new Departement("Ventes", null, BigDecimal.valueOf(100000)));

        DepartementRequest request = new DepartementRequest("Ventes", null, BigDecimal.valueOf(150000));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DepartementRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.path("message").asText()).contains("Un département avec le nom 'Ventes' existe déjà.");
    }

    @Test
    void createDepartement_shouldReturnBadRequestWhenNomIsEmpty() throws Exception {
        DepartementRequest request = new DepartementRequest("", null, BigDecimal.valueOf(100000));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DepartementRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.path("errors").path("nom").asText()).contains("Le nom du département ne peut pas être vide");
    }

    @Test
    void createDepartement_shouldReturnBadRequestWhenBudgetIsNull() throws Exception {
        DepartementRequest request = new DepartementRequest("Recherche", null, null);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DepartementRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.path("errors").path("budget").asText()).contains("Le budget ne peut pas être nul");
    }

    @Test
    void createDepartement_shouldReturnBadRequestWhenBudgetIsNegative() throws Exception {
        DepartementRequest request = new DepartementRequest("Opérations", null, BigDecimal.valueOf(-100));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DepartementRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.path("errors").path("budget").asText()).contains("Le budget doit être un nombre positif ou zéro");
    }

    @Test
    void getAllDepartements_shouldReturnListOfDepartementDto() {
        departementRepository.save(new Departement("IT", null, BigDecimal.valueOf(500000)));
        departementRepository.save(new Departement("Finance", null, BigDecimal.valueOf(700000)));

        ResponseEntity<PageResponse<DepartementDto>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PageResponse<DepartementDto>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);
        List<DepartementDto> departements = response.getBody().getContent();
        assertTrue(departements.stream().anyMatch(d -> "IT".equals(d.getNom())));
        assertTrue(departements.stream().anyMatch(d -> "Finance".equals(d.getNom())));
        assertThat(response.getBody().getTotalElements()).isEqualTo(2);
    }

    @Test
    void getDepartementById_shouldReturnDepartementDto() {
        Departement savedDepartement = departementRepository.save(new Departement("Logistique", null, BigDecimal.valueOf(200000)));

        ResponseEntity<DepartementDto> response = restTemplate.getForEntity(baseUrl + "/{id}", DepartementDto.class, savedDepartement.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNom()).isEqualTo("Logistique");
        assertThat(response.getBody().getId()).isEqualTo(savedDepartement.getId());
    }

    @Test
    void getDepartementById_shouldReturnNotFoundWhenDepartementDoesNotExist() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/{id}", String.class, 999L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateDepartement_shouldReturnUpdatedDepartementDto() {
        Departement existingDepartement = departementRepository.save(
                new Departement("RH Ancien", null, BigDecimal.valueOf(120000))
        );

        Employe manager = new Employe();
        manager.setNom("Manager");
        manager.setPrenom("Test");
        manager.setEmail("manager@test.com");
        manager.setDateEmbauche(LocalDate.now());
        manager.setStatut("ACTIF");
        manager.setSalaire(BigDecimal.valueOf(50000));
        manager.setDepartement(existingDepartement);
        manager = employeRepository.save(manager);

        DepartementRequest updatedRequest = new DepartementRequest("RH Nouveau", manager.getId(), BigDecimal.valueOf(130000));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DepartementRequest> requestEntity = new HttpEntity<>(updatedRequest, headers);

        ResponseEntity<DepartementDto> response = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.PUT,
                requestEntity,
                DepartementDto.class,
                existingDepartement.getId()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNom()).isEqualTo("RH Nouveau");
        assertThat(response.getBody().getBudget()).isEqualByComparingTo(BigDecimal.valueOf(130000));
        assertThat(response.getBody().getManagerId()).isEqualTo(manager.getId());
    }

    @Test
    void updateDepartement_shouldReturnNotFoundWhenDepartementToUpdateDoesNotExist() {
        DepartementRequest request = new DepartementRequest("Non Existant", null, BigDecimal.valueOf(100));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DepartementRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/{id}", HttpMethod.PUT, requestEntity, String.class, 999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteDepartement_shouldReturnNoContent() {
        Departement departementToDelete = departementRepository.save(new Departement("R&D à supprimer", null, BigDecimal.valueOf(100000)));

        restTemplate.delete(baseUrl + "/{id}", departementToDelete.getId());

        Optional<Departement> found = departementRepository.findById(departementToDelete.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void deleteDepartement_shouldReturnBadRequestIfEmployesAssociated() throws Exception {
        Departement departementWithEmployes = departementRepository.save(new Departement("Support Client", null, BigDecimal.valueOf(200000)));
        employeRepository.save(new Employe("Kouame", "Adjoua", "kouame.adjoua@example.com", departementWithEmployes, BigDecimal.valueOf(30000), LocalDate.now(), "ACTIF"));

        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/{id}", HttpMethod.DELETE, null, String.class, departementWithEmployes.getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.path("message").asText()).contains("Impossible de supprimer le département avec l'ID " + departementWithEmployes.getId() + " car il a des employés associés.");
    }

    @Test
    void deleteDepartement_shouldReturnNotFoundWhenDepartementDoesNotExist() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/{id}", HttpMethod.DELETE, null, String.class, 999L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
