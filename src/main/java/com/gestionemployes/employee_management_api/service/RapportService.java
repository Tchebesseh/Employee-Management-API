package com.gestionemployes.employee_management_api.service;

import com.gestionemployes.employee_management_api.dto.DepartementSalarySummaryDto;
import com.gestionemployes.employee_management_api.dto.PresenceTrendReportDto;
import com.gestionemployes.employee_management_api.model.Departement;
import com.gestionemployes.employee_management_api.model.Employe;
import com.gestionemployes.employee_management_api.model.Presence;
import com.gestionemployes.employee_management_api.repository.DepartementRepository;
import com.gestionemployes.employee_management_api.repository.EmployeRepository;
import com.gestionemployes.employee_management_api.repository.PresenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RapportService {

    private final PresenceRepository presenceRepository;
    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;

    public RapportService(PresenceRepository presenceRepository, EmployeRepository employeRepository, DepartementRepository departementRepository) {
        this.presenceRepository = presenceRepository;
        this.employeRepository = employeRepository;
        this.departementRepository = departementRepository;
    }

    /**
     * Formatte un nombre de minutes en une chaîne "Xh Ym".
     *
     * @param totalMinutes Le nombre total de minutes.
     * @return La chaîne formatée (ex: "8h 30m").
     */
    private String formatMinutesToHoursMinutes(Long totalMinutes) {
        if (totalMinutes == null) {
            return "0h 00m";
        }
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%dh %02dm", hours, minutes);
    }

    /**
     * Génère un rapport complet des tendances et statistiques de présence.
     *
     * @return PresenceTrendReportDto contenant diverses agrégations.
     */
    @Transactional(readOnly = true)
    public PresenceTrendReportDto getPresenceTrendsAndStats() {
        List<Presence> allPresences = presenceRepository.findAll();
        List<Employe> allEmployes = employeRepository.findAll();
        List<Departement> allDepartements = departementRepository.findAll();

        PresenceTrendReportDto report = new PresenceTrendReportDto();

        
        Map<String, Long> totalMinutesByDayOfWeek = allPresences.stream()
                .filter(p -> p.getHeuresTravaillees() != null)
                .collect(Collectors.groupingBy(
                        presence -> presence.getDate().getDayOfWeek().toString(),
                        Collectors.summingLong(Presence::getHeuresTravaillees)
                ));
        report.setTotalHoursByDayOfWeek(totalMinutesByDayOfWeek.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> formatMinutesToHoursMinutes(entry.getValue()),
                        (e1, e2) -> e1, 
                        LinkedHashMap::new 
                )));

        
        Map<Integer, Long> totalMinutesByMonth = allPresences.stream()
                .filter(p -> p.getHeuresTravaillees() != null)
                .collect(Collectors.groupingBy(
                        presence -> presence.getDate().getMonthValue(),
                        Collectors.summingLong(Presence::getHeuresTravaillees)
                ));
        report.setTotalHoursByMonth(totalMinutesByMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) 
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> formatMinutesToHoursMinutes(entry.getValue()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                )));

        
        Map<String, Long> totalMinutesByMonthYear = allPresences.stream()
                .filter(p -> p.getHeuresTravaillees() != null)
                .collect(Collectors.groupingBy(
                        presence -> presence.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.summingLong(Presence::getHeuresTravaillees)
                ));
        report.setTotalHoursByMonthYear(totalMinutesByMonthYear.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) 
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> formatMinutesToHoursMinutes(entry.getValue()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                )));

        
        
        Map<LocalDate, Long> dailyTotalMinutes = allPresences.stream()
                .filter(p -> p.getHeuresTravaillees() != null)
                .collect(Collectors.groupingBy(
                        Presence::getDate,
                        Collectors.summingLong(Presence::getHeuresTravaillees)
                ));
        double averageMinutesPerDay = dailyTotalMinutes.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        report.setAverageDailyHoursAcrossAllEmployees(formatMinutesToHoursMinutes(Math.round(averageMinutesPerDay)));


        
        Map<Long, Long> totalMinutesByEmployeId = allPresences.stream()
                .filter(p -> p.getHeuresTravaillees() != null && p.getEmploye() != null)
                .collect(Collectors.groupingBy(
                        presence -> presence.getEmploye().getId(),
                        Collectors.summingLong(Presence::getHeuresTravaillees)
                ));
        report.setTotalHoursByEmployeeId(totalMinutesByEmployeId.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) 
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> formatMinutesToHoursMinutes(entry.getValue()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                )));

        
        Map<String, Long> totalMinutesByDepartementName = allPresences.stream()
                .filter(p -> p.getHeuresTravaillees() != null && p.getEmploye() != null && p.getEmploye().getDepartement() != null)
                .collect(Collectors.groupingBy(
                        presence -> presence.getEmploye().getDepartement().getNom(),
                        Collectors.summingLong(Presence::getHeuresTravaillees)
                ));
        report.setTotalHoursByDepartementName(totalMinutesByDepartementName.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) 
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> formatMinutesToHoursMinutes(entry.getValue()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                )));

        return report;
    }

    /**
     * Génère un résumé des salaires par département.
     *
     * @return Liste de DepartementSalarySummaryDto.
     */
    @Transactional(readOnly = true)
    public List<DepartementSalarySummaryDto> getResumeSalairesByDepartement() {
        return departementRepository.findAll().stream()
                .map(departement -> {
                    List<Employe> employesInDepartement = employeRepository.findByDepartementId(departement.getId());

                    BigDecimal totalSalaries = employesInDepartement.stream()
                            .map(Employe::getSalaire)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Long numberOfEmployees = (long) employesInDepartement.size();

                    BigDecimal averageSalary = BigDecimal.ZERO;
                    if (numberOfEmployees > 0) {
                        averageSalary = totalSalaries.divide(BigDecimal.valueOf(numberOfEmployees), 2, RoundingMode.HALF_UP);
                    }

                    return new DepartementSalarySummaryDto(
                            departement.getId(),
                            departement.getNom(),
                            totalSalaries,
                            numberOfEmployees,
                            averageSalary
                    );
                })
                .sorted(Comparator.comparing(DepartementSalarySummaryDto::getDepartementNom)) 
                .collect(Collectors.toList());
    }


	
    public Map<String, Map<String, BigDecimal>> getResumeSalairesParDepartement() {
        return departementRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Departement::getNom,
                        departement -> {
                            List<Employe> employes = employeRepository.findByDepartementId(departement.getId());
                            BigDecimal total = employes.stream().map(Employe::getSalaire).reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal average = employes.isEmpty() ? BigDecimal.ZERO :
                                    total.divide(BigDecimal.valueOf(employes.size()), 2, RoundingMode.HALF_UP);
                            BigDecimal max = employes.stream().map(Employe::getSalaire).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                            BigDecimal min = employes.stream().map(Employe::getSalaire).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

                            Map<String, BigDecimal> stats = new LinkedHashMap<>();
                            stats.put("totalSalary", total);
                            stats.put("averageSalary", average);
                            stats.put("maxSalary", max);
                            stats.put("minSalary", min);
                            return stats;
                        },
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


}
