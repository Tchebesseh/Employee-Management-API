package com.gestionemployes.employee_management_api.controller;

import com.gestionemployes.employee_management_api.dto.DepartementSalarySummaryDto;
import com.gestionemployes.employee_management_api.dto.PresenceTrendReportDto;
import com.gestionemployes.employee_management_api.service.RapportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rapports")
@Tag(name = "Rapports & Analyses", description = "API pour générer des rapports et des analyses de données")
public class RapportController {

    private final RapportService rapportService;

    public RapportController(RapportService rapportService) {
        this.rapportService = rapportService;
    }

    @Operation(summary = "Obtenir les tendances et statistiques des présences",
               description = "Génère un rapport agrégé sur les heures de présence (par jour de semaine, mois, employé, département, etc.).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rapport de tendances de présence généré",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = PresenceTrendReportDto.class)))
    })
    @GetMapping("/tendances-presences")
    public ResponseEntity<PresenceTrendReportDto> getTendancesPresences() {
        PresenceTrendReportDto report = rapportService.getPresenceTrendsAndStats();
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Obtenir le résumé des salaires par département",
               description = "Fournit un aperçu du total des salaires, du nombre d'employés et du salaire moyen par département.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résumé des salaires par département généré",
                         content = @Content(mediaType = "application/json",
                         schema = @Schema(implementation = DepartementSalarySummaryDto.class)))
    })
    @GetMapping("/resume-salaires")
    public ResponseEntity<List<DepartementSalarySummaryDto>> getResumeSalairesByDepartement() {
        List<DepartementSalarySummaryDto> summary = rapportService.getResumeSalairesByDepartement();
		return ResponseEntity.ok(summary);
	}
}
