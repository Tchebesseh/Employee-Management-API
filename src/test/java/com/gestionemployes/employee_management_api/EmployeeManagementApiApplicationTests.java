package com.gestionemployes.employee_management_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // Importez cette annotation
import org.springframework.test.context.TestPropertySource; // Importez cette annotation

@SpringBootTest
@ActiveProfiles("test") // Active explicitement le profil 'test' pour ce test
@TestPropertySource(properties = {
    // Surcharge les propriétés de la base de données spécifiquement pour ce test
    // Cela garantit que le test tente de se connecter au service PostgreSQL Docker
    "spring.datasource.url=jdbc:postgresql://postgres-test:5432/test_db",
    "spring.datasource.username=testuser",
    "spring.datasource.password=testpassword",
    "spring.jpa.hibernate.ddl-auto=create-drop" // Assure que le schéma est créé/détruit
})
class EmployeeManagementApiApplicationTests {

    @Test
    void contextLoads() {
        // Ce test vérifie simplement que le contexte Spring Boot peut démarrer.
        // Si le contexte se charge, cela signifie que la connexion à la base de données est réussie.
    }

}
