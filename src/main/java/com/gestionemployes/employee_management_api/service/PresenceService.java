package com.gestionemployes.employee_management_api.service;

import com.gestionemployes.employee_management_api.dto.PresenceArriveeRequest;
import com.gestionemployes.employee_management_api.dto.PresenceDepartRequest;
import com.gestionemployes.employee_management_api.exception.BadRequestException;
import com.gestionemployes.employee_management_api.exception.ResourceNotFoundException;
import com.gestionemployes.employee_management_api.model.Departement; 
import com.gestionemployes.employee_management_api.model.Employe;
import com.gestionemployes.employee_management_api.model.Presence;
import com.gestionemployes.employee_management_api.repository.DepartementRepository; 
import com.gestionemployes.employee_management_api.repository.EmployeRepository;
import com.gestionemployes.employee_management_api.repository.PresenceRepository;
import org.springframework.data.domain.Pageable; 
import org.springframework.data.domain.PageRequest; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; 

@Service
public class PresenceService {

    private final PresenceRepository presenceRepository;
    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository; 

    
    public PresenceService(PresenceRepository presenceRepository, EmployeRepository employeRepository, DepartementRepository departementRepository) {
        this.presenceRepository = presenceRepository;
        this.employeRepository = employeRepository;
        this.departementRepository = departementRepository; 
    }

    @Transactional
    public Presence pointageArrivee(PresenceArriveeRequest request) {
        Employe employe = employeRepository.findById(request.getEmployeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employé", "id", request.getEmployeId()));

        
        Optional<Presence> existingPresence = presenceRepository.findByEmployeIdAndDate(employe.getId(), request.getDate());
        if (existingPresence.isPresent() && existingPresence.get().getDepart() == null) {
            throw new BadRequestException("L'employé a déjà pointé son arrivée pour le " + request.getDate() + " et n'a pas encore pointé son départ.");
        } else if (existingPresence.isPresent() && existingPresence.get().getDepart() != null) {
            throw new BadRequestException("L'employé a déjà complété un pointage pour le " + request.getDate() + ".");
        }

        Presence presence = new Presence(employe, request.getDate(), request.getArrivee());
        return presenceRepository.save(presence);
    }

    @Transactional
    public Presence pointageDepart(PresenceDepartRequest request) {
        Presence presence = presenceRepository.findById(request.getPresenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Enregistrement de présence", "id", request.getPresenceId()));

        if (presence.getDepart() != null) {
            throw new BadRequestException("Le pointage de départ a déjà été enregistré pour cet enregistrement de présence.");
        }

        
        if (request.getDepart().isBefore(presence.getArrivee())) {
            throw new BadRequestException("L'heure de départ (" + request.getDepart() + ") ne peut pas être avant l'heure d'arrivée (" + presence.getArrivee() + ").");
        }

        presence.setDepart(request.getDepart()); 
        presence.calculateHeuresTravaillees(); 

        return presenceRepository.save(presence);
    }

    @Transactional(readOnly = true)
    public List<Presence> getMonthlyPresenceReport(Long employeId, int year, int month) {
        
        if (!employeRepository.existsById(employeId)) {
            throw new ResourceNotFoundException("Employé", "id", employeId);
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Presence> allPresences = presenceRepository.findByEmployeIdOrderByDateAsc(employeId);

        return allPresences.stream()
                .filter(p -> !p.getDate().isBefore(startDate) && !p.getDate().isAfter(endDate))
                .collect(Collectors.toList()); 
    }

    @Transactional(readOnly = true)
    public List<Presence> getDepartementPresenceSummary(Long departementId) {
        Departement departement = departementRepository.findById(departementId)
                .orElseThrow(() -> new ResourceNotFoundException("Département", "id", departementId));

        
        
        List<Employe> employesInDepartement = employeRepository.findByDepartementId(departementId, Pageable.unpaged()).getContent();

        return employesInDepartement.stream()
                .flatMap(employe -> presenceRepository.findByEmployeIdOrderByDateAsc(employe.getId()).stream())
                .collect(Collectors.toList()); 
    }
}
