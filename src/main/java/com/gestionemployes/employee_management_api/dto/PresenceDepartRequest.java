package com.gestionemployes.employee_management_api.dto;

import jakarta.validation.constraints.*;
import java.time.LocalTime;

public class PresenceDepartRequest {

	@NotNull(message = "L'ID de l'enregistrement de présence ne peut pas être nul")
	@Positive(message = "L'ID de l'enregistrement de présence doit être un nombre positif")
	private Long presenceId;

	@NotNull(message = "L'heure de départ ne peut pas être nulle")
	private LocalTime depart;

	public PresenceDepartRequest() {
	}

	public PresenceDepartRequest(Long presenceId, LocalTime depart) {
		this.presenceId = presenceId;
		this.depart = depart;
	}

	public Long getPresenceId() {
		return presenceId;
	}

	public void setPresenceId(Long presenceId) {
		this.presenceId = presenceId;
	}

	public LocalTime getDepart() {
		return depart;
	}

	public void setDepart(LocalTime depart) {
		this.depart = depart;
	}
}
