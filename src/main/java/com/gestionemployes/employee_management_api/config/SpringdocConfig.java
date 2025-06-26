package com.gestionemployes.employee_management_api.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringdocConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI().info(new Info().title("API Système de Gestion des Employés").version("1.0.0")
				.description("API REST Spring Boot pour la gestion des employés, départements et présences."));
	}

	@Bean
	public GroupedOpenApi rapportsAndActuatorApi() {
		return GroupedOpenApi.builder().group("Rapports & Analyses").pathsToMatch("/api/rapports/**", "/actuator/**")
				.build();
	}

	@Bean
	public GroupedOpenApi employesApi() {
		return GroupedOpenApi.builder().group("Employés").pathsToMatch("/api/employes/**").build();
	}

	@Bean
	public GroupedOpenApi departementsApi() {
		return GroupedOpenApi.builder().group("Departements").pathsToMatch("/api/departements/**").build();
	}

	@Bean
	public GroupedOpenApi presencesApi() {
		return GroupedOpenApi.builder().group("Présences").pathsToMatch("/api/presences/**").build();
	}
}
