package com.gestionemployes.employee_management_api.dto;

import java.math.BigDecimal;

public class DepartementSalarySummaryDto {
	private Long departementId;
	private String departementNom;
	private BigDecimal totalSalaries;
	private Long numberOfEmployees;
	private BigDecimal averageSalary;

	public DepartementSalarySummaryDto() {
	}

	public DepartementSalarySummaryDto(Long departementId, String departementNom, BigDecimal totalSalaries,
			Long numberOfEmployees, BigDecimal averageSalary) {
		this.departementId = departementId;
		this.departementNom = departementNom;
		this.totalSalaries = totalSalaries;
		this.numberOfEmployees = numberOfEmployees;
		this.averageSalary = averageSalary;
	}

	// --- Getters et Setters ---
	public Long getDepartementId() {
		return departementId;
	}

	public void setDepartementId(Long departementId) {
		this.departementId = departementId;
	}

	public String getDepartementNom() {
		return departementNom;
	}

	public void setDepartementNom(String departementNom) {
		this.departementNom = departementNom;
	}

	public BigDecimal getTotalSalaries() {
		return totalSalaries;
	}

	public void setTotalSalaries(BigDecimal totalSalaries) {
		this.totalSalaries = totalSalaries;
	}

	public Long getNumberOfEmployees() {
		return numberOfEmployees;
	}

	public void setNumberOfEmployees(Long numberOfEmployees) {
		this.numberOfEmployees = numberOfEmployees;
	}

	public BigDecimal getAverageSalary() {
		return averageSalary;
	}

	public void setAverageSalary(BigDecimal averageSalary) {
		this.averageSalary = averageSalary;
	}
}
