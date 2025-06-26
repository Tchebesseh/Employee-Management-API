package com.gestionemployes.employee_management_api.dto;

import java.math.BigDecimal;

public class DepartementDto {
	private Long id;
	private String nom;
	private Long managerId;
	private BigDecimal budget;

	public DepartementDto() {
	}

	public DepartementDto(Long id, String nom, Long managerId, BigDecimal budget) {
		this.id = id;
		this.nom = nom;
		this.managerId = managerId;
		this.budget = budget;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
