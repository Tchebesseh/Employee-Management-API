package com.gestionemployes.employee_management_api.controller;

import com.gestionemployes.employee_management_api.dto.DepartementDto;
import com.gestionemployes.employee_management_api.dto.DepartementRequest;
import com.gestionemployes.employee_management_api.dto.EmployeDto;
import com.gestionemployes.employee_management_api.mapper.DepartementMapper;
import com.gestionemployes.employee_management_api.model.Departement;
import com.gestionemployes.employee_management_api.service.DepartementService;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/departements")
@Tag(name = "Départements", description = "API pour la gestion des départements")
public class DepartementController {

    private final DepartementService departementService;
    private final DepartementMapper departementMapper;

    public DepartementController(DepartementService departementService, DepartementMapper departementMapper) {
        this.departementService = departementService;
        this.departementMapper = departementMapper;
    }

    @Operation(summary = "Lister tous les départements avec pagination et tri")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste paginée des départements",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = DepartementDto.class))) 
    })
    @GetMapping
    public ResponseEntity<Page<DepartementDto>> getAllDepartements(
            
            @Parameter(description = "Paramètres de pagination et de tri. Format: page={numéro_page}&size={taille_page}&sort={champ},{direction}. Ex: page=0&size=10&sort=nom,asc")
            @PageableDefault(size = 10, sort = "nom") Pageable pageable) {
        Page<Departement> pageDepartements = departementService.getAllDepartements(pageable);
        Page<DepartementDto> dtoPage = pageDepartements.map(departementMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @Operation(summary = "Obtenir les détails d'un département par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Département trouvé",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = DepartementDto.class))),
            @ApiResponse(responseCode = "404", description = "Département non trouvé",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Département non trouvé(e) avec id : '1'\",\"path\":\"/api/departements/1\"}"))) 
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepartementDto> getDepartementById(
            @Parameter(description = "ID du département", example = "1")
            @PathVariable Long id) {
        Departement departement = departementService.getDepartementById(id);
        return ResponseEntity.ok(departementMapper.toDto(departement));
    }

    @Operation(summary = "Créer un nouveau département")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Département créé avec succès",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = DepartementDto.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide (validation ou nom déjà existant)",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Un département avec le nom 'Finance' existe déjà.\",\"path\":\"/api/departements\"}"))) 
    })
    @PostMapping
    public ResponseEntity<DepartementDto> createDepartement(
            @Parameter(description = "Objet DepartementRequest avec les détails du nouveau département")
            @Valid @RequestBody DepartementRequest request) {
        Departement created = departementService.createDepartement(request);
        return new ResponseEntity<>(departementMapper.toDto(created), HttpStatus.CREATED);
    }

    @Operation(summary = "Mettre à jour un département")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Département mis à jour",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = DepartementDto.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":400,\"error\":\"Validation Error\",\"message\":\"La requête contient des erreurs de validation.\",\"path\":\"/api/departements/1\",\"errors\":{\"nom\":\"Le nom du département ne peut pas être vide\"}}"))), 
            @ApiResponse(responseCode = "404", description = "Département non trouvé",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Département non trouvé(e) avec id : '99'\",\"path\":\"/api/departements/99\"}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<DepartementDto> updateDepartement(
            @Parameter(description = "ID du département à mettre à jour", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Objet DepartementRequest avec les détails mis à jour")
            @Valid @RequestBody DepartementRequest request) {
        Departement updated = departementService.updateDepartement(id, request);
        return ResponseEntity.ok(departementMapper.toDto(updated));
    }

    @Operation(summary = "Obtenir les employés d'un département spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des employés du département",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = EmployeDto.class))), 
            @ApiResponse(responseCode = "404", description = "Département non trouvé",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Département non trouvé(e) avec id : '1'\",\"path\":\"/api/departements/1/employes\"}")))
    })
    @GetMapping("/{id}/employes")
    public ResponseEntity<List<EmployeDto>> getEmployesByDepartement(
            @Parameter(description = "ID du département", example = "1")
            @PathVariable Long id) {
        List<EmployeDto> employes = departementService.getEmployesByDepartementId(id);
        return ResponseEntity.ok(employes);
    }

    @Operation(summary = "Obtenir l'analyse du budget d'un département")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analyse du budget du département",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(type = "number", format = "double", example = "250000.00"))), 
            @ApiResponse(responseCode = "404", description = "Département non trouvé",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Département non trouvé(e) avec id : '1'\",\"path\":\"/api/departements/1/rapport-budget\"}")))
    })
    @GetMapping("/{id}/rapport-budget")
    public ResponseEntity<BigDecimal> getDepartementBudgetReport(
            @Parameter(description = "ID du département", example = "1")
            @PathVariable Long id) {
        BigDecimal budget = departementService.getDepartementBudgetAnalysis(id);
        return ResponseEntity.ok(budget);
    }

    @Operation(summary = "Supprimer un département")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Département supprimé avec succès"),
            @ApiResponse(responseCode = "400", description = "Impossible de supprimer le département (employés associés)",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Impossible de supprimer le département avec l'ID 1 car il a des employés associés.\",\"path\":\"/api/departements/1\"}"))),
            @ApiResponse(responseCode = "404", description = "Département non trouvé",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(example = "{\"timestamp\":\"2025-06-25T10:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Département non trouvé(e) avec id : '99'\",\"path\":\"/api/departements/99\"}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartement(@Parameter(description = "ID du département à supprimer", example = "1") @PathVariable Long id) {
        departementService.deleteDepartement(id);
		return ResponseEntity.noContent().build();
	}
}
