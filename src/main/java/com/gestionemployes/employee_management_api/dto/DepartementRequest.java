package com.gestionemployes.employee_management_api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class DepartementRequest {

	@NotBlank(message = "Le nom du département ne peut pas être vide")
	@Size(max = 100, message = "Le nom du département ne peut pas dépasser 100 caractères")
	private String nom;

	@PositiveOrZero(message = "L'ID du manager doit être un nombre positif ou zéro (s'il n'y a pas de manager)")
	private Long managerId;

	@NotNull(message = "Le budget ne peut pas être nul")
	@PositiveOrZero(message = "Le budget doit être un nombre positif ou zéro")
	private BigDecimal budget;

	public DepartementRequest() {
	}

	public DepartementRequest(String nom, Long managerId, BigDecimal budget) {
		this.nom = nom;
		this.managerId = managerId;
		this.budget = budget;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public Long getManagerId() {
		return managerId;
	}

	public void setManagerId(Long managerId) {
		this.managerId = managerId;
	}

	public BigDecimal getBudget() {
		return budget;
	}

	public void setBudget(BigDecimal budget) {
		this.budget = budget;
	}
}
