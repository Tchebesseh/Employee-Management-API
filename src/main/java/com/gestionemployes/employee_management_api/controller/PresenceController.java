package com.gestionemployes.employee_management_api.controller;

import com.gestionemployes.employee_management_api.dto.PresenceArriveeRequest;
import com.gestionemployes.employee_management_api.dto.PresenceDepartRequest;
import com.gestionemployes.employee_management_api.dto.PresenceDto;
import com.gestionemployes.employee_management_api.mapper.PresenceMapper;
import com.gestionemployes.employee_management_api.model.Presence;
import com.gestionemployes.employee_management_api.service.PresenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/presences")
@Tag(name = "Présences", description = "API pour le suivi des présences des employés")
public class PresenceController {

	private final PresenceService presenceService;
	private final PresenceMapper presenceMapper;

	public PresenceController(PresenceService presenceService, PresenceMapper presenceMapper) {
		this.presenceService = presenceService;
		this.presenceMapper = presenceMapper;
	}

	@Operation(summary = "Enregistrer le pointage d'arrivée d'un employé", description = "Permet à un employé de pointer son arrivée pour une date donnée. Un seul pointage d'arrivée par jour est autorisé sans un pointage de départ complété.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Pointage d'arrivée enregistré avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PresenceDto.class))),

			@ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée ou pointage existant)", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"L'employé a déjà pointé son arrivée pour le 2024-06-25 et n'a pas encore pointé son départ.\",\"path\":\"/api/presences/arrivee\"}"))),
			@ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Employé non trouvé(e) avec id : '99'\",\"path\":\"/api/presences/arrivee\"}"))) })
	@PostMapping("/arrivee")
	public ResponseEntity<PresenceDto> pointageArrivee(
			@Parameter(description = "Objet PresenceArriveeRequest avec les détails du pointage d'arrivée") @Valid @RequestBody PresenceArriveeRequest request) {
		Presence presence = presenceService.pointageArrivee(request);
		return new ResponseEntity<>(presenceMapper.toDto(presence), HttpStatus.CREATED);
	}

	@Operation(summary = "Enregistrer le pointage de départ d'un employé", description = "Permet à un employé de pointer son départ pour un enregistrement de présence existant. L'heure de départ doit être après l'heure d'arrivée.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Pointage de départ enregistré avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PresenceDto.class))),

			@ApiResponse(responseCode = "400", description = "Requête invalide (validation, départ déjà enregistré ou heure invalide)", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"L'heure de départ (16:00) ne peut pas être avant l'heure d'arrivée (17:00).\",\"path\":\"/api/presences/depart\"}"))),
			@ApiResponse(responseCode = "404", description = "Enregistrement de présence non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Enregistrement de présence non trouvé(e) avec id : '99'\",\"path\":\"/api/presences/depart\"}"))) })
	@PostMapping("/depart")
	public ResponseEntity<PresenceDto> pointageDepart(
			@Parameter(description = "Objet PresenceDepartRequest avec les détails du pointage de départ") @Valid @RequestBody PresenceDepartRequest request) {
		Presence presence = presenceService.pointageDepart(request);
		return ResponseEntity.ok(presenceMapper.toDto(presence));
	}

	@Operation(summary = "Obtenir le rapport de présence mensuel pour un employé", description = "Génère un rapport de tous les enregistrements de présence pour un employé spécifique durant un mois et une année donnés.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Rapport de présence mensuel généré", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PresenceDto.class))),

			@ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Employé non trouvé(e) avec id : '99'\",\"path\":\"/api/presences/rapport/99?year=2024&month=6\"}"))) })
	@GetMapping("/rapport/{employeId}")
	public ResponseEntity<List<PresenceDto>> getMonthlyPresenceReport(
			@Parameter(description = "ID de l'employé", example = "1") @PathVariable Long employeId,
			@Parameter(description = "Année du rapport", example = "2024") @RequestParam int year,
			@Parameter(description = "Mois du rapport (1-12)", example = "6") @RequestParam int month) {
		List<Presence> report = presenceService.getMonthlyPresenceReport(employeId, year, month);
		return ResponseEntity.ok(report.stream().map(presenceMapper::toDto).collect(Collectors.toList()));
	}

	@Operation(summary = "Obtenir le résumé des présences par département", description = "Fournit un résumé de tous les enregistrements de présence pour tous les employés d'un département spécifique.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Résumé des présences du département généré", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PresenceDto.class))),

			@ApiResponse(responseCode = "404", description = "Département non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Département non trouvé(e) avec id : '99'\",\"path\":\"/api/presences/departement/99\"}"))) })
	@GetMapping("/departement/{departementId}")
	public ResponseEntity<List<PresenceDto>> getDepartementPresenceSummary(
			@Parameter(description = "ID du département", example = "1") @PathVariable Long departementId) {
		List<Presence> summary = presenceService.getDepartementPresenceSummary(departementId);
		return ResponseEntity.ok(summary.stream().map(presenceMapper::toDto).collect(Collectors.toList()));
	}
}
