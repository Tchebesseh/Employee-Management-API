package com.gestionemployes.employee_management_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeDto {

	private Long id;

	private String prenom;
	private String nom;
	private String email;
	private Long departementId;
	private BigDecimal salaire;

	private LocalDate dateEmbauche;
	private String statut;

	public EmployeDto() {
	}

	public EmployeDto(Long id, String prenom, String nom, String email, Long departementId, BigDecimal salaire,
			LocalDate dateEmbauche, String statut) {
		this.id = id;
		this.prenom = prenom;
		this.nom = nom;
		this.email = email;
		this.departementId = departementId;
		this.salaire = salaire;
		this.dateEmbauche = dateEmbauche;
		this.statut = statut;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getDepartementId() {
		return departementId;
	}

	public void setDepartementId(Long departementId) {
		this.departementId = departementId;
	}

	public BigDecimal getSalaire() {
		return salaire;
	}

	public void setSalaire(BigDecimal salaire) {
		this.salaire = salaire;
	}

	public LocalDate getDateEmbauche() {
		return dateEmbauche;
	}

	public void setDateEmbauche(LocalDate dateEmbauche) {
		this.dateEmbauche = dateEmbauche;
	}

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}
}
