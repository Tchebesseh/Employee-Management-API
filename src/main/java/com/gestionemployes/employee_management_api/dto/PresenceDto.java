package com.gestionemployes.employee_management_api.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class PresenceDto {
	private Long id;
	private Long employeId; 
	private LocalDate date;
	private LocalTime arrivee;
	private LocalTime depart;
	private String heuresTravaillees; 

	public PresenceDto() {
	}

	public PresenceDto(Long id, Long employeId, LocalDate date, LocalTime arrivee, LocalTime depart,
			String heuresTravaillees) {
		this.id = id;
		this.employeId = employeId;
		this.date = date;
		this.arrivee = arrivee;
		this.depart = depart;
		this.heuresTravaillees = heuresTravaillees;
	}

	// --- Getters et Setters ---
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public LocalTime getDepart() {
		return depart;
	}

	public void setDepart(LocalTime depart) {
		this.depart = depart;
	}

	public String getHeuresTravaillees() {
		return heuresTravaillees;
	}

	public void setHeuresTravaillees(String heuresTravaillees) {
		this.heuresTravaillees = heuresTravaillees;
	}
}
