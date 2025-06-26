package com.gestionemployes.employee_management_api.controller;

import com.gestionemployes.employee_management_api.dto.EmployeRequest;
import com.gestionemployes.employee_management_api.dto.EmployeDto;
import com.gestionemployes.employee_management_api.model.Employe;
import com.gestionemployes.employee_management_api.service.EmployeService;
import com.gestionemployes.employee_management_api.mapper.EmployeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employes")
@Tag(name = "Employés", description = "API pour la gestion des employés")
public class EmployeController {

	private final EmployeService employeService;
	private final EmployeMapper employeMapper;

	public EmployeController(EmployeService employeService, EmployeMapper employeMapper) {
		this.employeService = employeService;
		this.employeMapper = employeMapper;
	}

	@Operation(summary = "Lister tous les employés avec pagination, tri et filtrage", description = "Récupère une liste paginée d'employés. Le paramètre 'searchTerm' permet de filtrer par nom ou email. Si 'searchTerm' est vide, tous les employés sont listés.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Liste paginée des employés", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeDto.class))) })
	@GetMapping
	public ResponseEntity<Page<EmployeDto>> getAllEmployes(
			@Parameter(description = "Terme de recherche pour le nom ou l'email de l'employé", example = "Dupont") @RequestParam(required = false) String searchTerm,
			@Parameter(description = "Paramètres de pagination et de tri (page, taille, tri). Ex: page=0&size=10&sort=nom,asc") @PageableDefault(size = 10, sort = "nom") Pageable pageable) {
		Page<Employe> employesPage = employeService.getAllEmployes(searchTerm, pageable);
		Page<EmployeDto> employeDtoPage = employesPage.map(employeMapper::toDto);
		return ResponseEntity.ok(employeDtoPage);
	}

	@Operation(summary = "Obtenir les détails d'un employé par son ID", description = "Récupère les informations détaillées d'un employé spécifique en utilisant son ID unique.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Employé trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeDto.class))),
			@ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Employé non trouvé(e) avec id : '1'\",\"path\":\"/api/employes/1\"}"))) })
	@GetMapping("/{id}")
	public ResponseEntity<EmployeDto> getEmployeById(
			@Parameter(description = "ID de l'employé à récupérer", example = "1") @PathVariable Long id) {
		Employe employe = employeService.getEmployeById(id);
		return ResponseEntity.ok(employeMapper.toDto(employe));
	}

	@Operation(summary = "Ajouter un nouvel employé", description = "Crée un nouvel enregistrement d'employé dans le système. Nécessite un Département ID valide.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Employé créé avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeDto.class))),
			@ApiResponse(responseCode = "400", description = "Requête invalide (validation échouée ou règle métier violée)", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Un employé avec l'email 'email@example.com' existe déjà.\",\"path\":\"/api/employes\"}"))) })
	@PostMapping
	public ResponseEntity<EmployeDto> createEmploye(
			@Parameter(description = "Objet EmployeRequest avec les détails du nouvel employé") @Valid @RequestBody EmployeRequest employeRequest) {
		Employe newEmploye = employeService.createEmploye(employeRequest);
		return new ResponseEntity<>(employeMapper.toDto(newEmploye), HttpStatus.CREATED);
	}

	@Operation(summary = "Mettre à jour les détails d'un employé existant", description = "Met à jour les informations d'un employé spécifié par son ID. Permet de modifier le département, le salaire, le statut, etc.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Employé mis à jour avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeDto.class))),
			@ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":400,\"error\":\"Validation Error\",\"message\":\"La requête contient des erreurs de validation.\",\"path\":\"/api/employes/1\",\"errors\":{\"email\":\"L'email doit être au format valide\"}}"))),
			@ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Employé non trouvé(e) avec id : '99'\",\"path\":\"/api/employes/99\"}"))) })
	@PutMapping("/{id}")
	public ResponseEntity<EmployeDto> updateEmploye(
			@Parameter(description = "ID de l'employé à mettre à jour", example = "1") @PathVariable Long id,
			@Parameter(description = "Objet EmployeRequest avec les détails mis à jour") @Valid @RequestBody EmployeRequest employeRequest) {
		Employe updatedEmploye = employeService.updateEmploye(id, employeRequest);
		return ResponseEntity.ok(employeMapper.toDto(updatedEmploye));
	}

	@Operation(summary = "Désactiver un employé par son ID", description = "Désactive un employé, changeant son statut à 'INACTIF'. Un employé ne peut pas être désactivé s'il a des enregistrements de présence.")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Employé désactivé avec succès"),
			@ApiResponse(responseCode = "400", description = "Impossible de désactiver l'employé (règle métier: présence existante)", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Impossible de désactiver l'employé avec l'ID 1 car il a des enregistrements de présence associés.\",\"path\":\"/api/employes/1\"}"))),
			@ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Employé non trouvé(e) avec id : '99'\",\"path\":\"/api/employes/99\"}"))) })
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deactivateEmploye(
			@Parameter(description = "ID de l'employé à désactiver", example = "1") @PathVariable Long id) {
		employeService.deactivateEmploye(id);
		return ResponseEntity.noContent().build();
	}
}
