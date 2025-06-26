package com.gestionemployes.employee_management_api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeRequest {

	@NotBlank(message = "Le prénom ne peut pas être vide")
	@Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
	private String prenom;

	@NotBlank(message = "Le nom ne peut pas être vide")
	@Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
	private String nom;

	@NotBlank(message = "L'email ne peut pas être vide")
	@Email(message = "L'email doit être au format valide")
	@Size(max = 255, message = "L'email ne peut pas dépasser 255 caractères")
	private String email;

	@NotNull(message = "L'ID du département ne peut pas être nul")
	@Positive(message = "L'ID du département doit être un nombre positif")
	private Long departementId;

	@NotNull(message = "Le salaire ne peut pas être nul")
	@Positive(message = "Le salaire doit être un nombre positif")
	private BigDecimal salaire;

	@NotNull(message = "La date d'embauche ne peut pas être nulle")
	@PastOrPresent(message = "La date d'embauche ne peut pas être dans le futur")
	private LocalDate dateEmbauche;

	@NotBlank(message = "Le statut ne peut pas être vide")
	@Size(max = 50, message = "Le statut ne peut pas dépasser 50 caractères")
	private String statut;

	public EmployeRequest() {
	}

	public EmployeRequest(String prenom, String nom, String email, Long departementId, BigDecimal salaire,
			LocalDate dateEmbauche, String statut) {
		this.prenom = prenom;
		this.nom = nom;
		this.email = email;
		this.departementId = departementId;
		this.salaire = salaire;
		this.dateEmbauche = dateEmbauche;
		this.statut = statut;
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
