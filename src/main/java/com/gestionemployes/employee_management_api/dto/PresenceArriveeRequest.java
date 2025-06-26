package com.gestionemployes.employee_management_api.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class PresenceArriveeRequest {

	@NotNull(message = "L'ID de l'employé ne peut pas être nul")
	@Positive(message = "L'ID de l'employé doit être un nombre positif")
	private Long employeId;

	@NotNull(message = "La date ne peut pas être nulle")
	@PastOrPresent(message = "La date ne peut pas être dans le futur")
	private LocalDate date;

	@NotNull(message = "L'heure d'arrivée ne peut pas être nulle")
	private LocalTime arrivee;

	public PresenceArriveeRequest() {
	}

	public PresenceArriveeRequest(Long employeId, LocalDate date, LocalTime arrivee) {
		this.employeId = employeId;
		this.date = date;
		this.arrivee = arrivee;
	}

	public Long getEmployeId() {
		return employeId;
	}

	public void setEmployeId(Long employeId) {
		this.employeId = employeId;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getArrivee() {
		return arrivee;
	}

	public void setArrivee(LocalTime arrivee) {
		this.arrivee = arrivee;
	}
}
