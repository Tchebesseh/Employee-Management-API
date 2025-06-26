package com.gestionemployes.employee_management_api.service;

import com.gestionemployes.employee_management_api.dto.EmployeRequest;
import com.gestionemployes.employee_management_api.exception.BadRequestException;
import com.gestionemployes.employee_management_api.exception.ResourceNotFoundException;
import com.gestionemployes.employee_management_api.model.Departement;
import com.gestionemployes.employee_management_api.model.Employe;
import com.gestionemployes.employee_management_api.repository.DepartementRepository;
import com.gestionemployes.employee_management_api.repository.EmployeRepository;
import com.gestionemployes.employee_management_api.repository.PresenceRepository;
import com.gestionemployes.employee_management_api.mapper.EmployeMapper; 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final PresenceRepository presenceRepository;
    private final EmployeMapper employeMapper;

    public EmployeService(EmployeRepository employeRepository,
                          DepartementRepository departementRepository,
                          PresenceRepository presenceRepository,
                          EmployeMapper employeMapper) {
        this.employeRepository = employeRepository;
        this.departementRepository = departementRepository;
        this.presenceRepository = presenceRepository;
        this.employeMapper = employeMapper;
    }

    @Transactional
    public Employe createEmploye(EmployeRequest employeRequest) {
        if (employeRepository.existsByEmail(employeRequest.getEmail())) {
            throw new BadRequestException("Un employé avec l'email '" + employeRequest.getEmail() + "' existe déjà.");
        }

        Departement departement = departementRepository.findById(employeRequest.getDepartementId())
                .orElseThrow(() -> new ResourceNotFoundException("Département", "id", employeRequest.getDepartementId()));

        if (employeRequest.getSalaire().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le salaire doit être un nombre positif.");
        }

       
        if (departement.getBudget() == null || employeRequest.getSalaire().compareTo(departement.getBudget()) > 0) {
            throw new BadRequestException("Le salaire de l'employé dépasse le budget du département ou le budget n'est pas défini.");
        }

        Employe employe = employeMapper.toEntity(employeRequest, departement);

        return employeRepository.save(employe);
    }

    @Transactional
    public Employe updateEmploye(Long id, EmployeRequest employeRequest) {
        Employe existingEmploye = employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé", "id", id));

        if (!existingEmploye.getEmail().equals(employeRequest.getEmail()) && employeRepository.existsByEmail(employeRequest.getEmail())) {
            throw new BadRequestException("Un autre employé avec l'email '" + employeRequest.getEmail() + "' existe déjà.");
        }

        Departement newDepartement = departementRepository.findById(employeRequest.getDepartementId())
                .orElseThrow(() -> new ResourceNotFoundException("Département", "id", employeRequest.getDepartementId()));

        if (employeRequest.getSalaire().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le salaire doit être un nombre positif.");
        }

        // Vérification du budget lors de la mise à jour
        if (newDepartement.getBudget() == null || employeRequest.getSalaire().compareTo(newDepartement.getBudget()) > 0) {
            throw new BadRequestException("Le salaire de l'employé dépasse le budget du nouveau département ou le budget n'est pas défini.");
        }

        existingEmploye.setPrenom(employeRequest.getPrenom());
        existingEmploye.setNom(employeRequest.getNom());
        existingEmploye.setEmail(employeRequest.getEmail());
        existingEmploye.setDepartement(newDepartement);
        existingEmploye.setSalaire(employeRequest.getSalaire());
        existingEmploye.setDateEmbauche(employeRequest.getDateEmbauche());
        existingEmploye.setStatut(employeRequest.getStatut());

        return employeRepository.save(existingEmploye);
    }

    @Transactional
    public void deactivateEmploye(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé", "id", id));

        // Règle métier : Impossible de désactiver un employé avec des enregistrements de présence
        if (presenceRepository.existsByEmployeId(id)) {
            throw new BadRequestException("Impossible de désactiver l'employé avec l'ID " + id + " car il a des enregistrements de présence associés.");
        }

        employe.setStatut("INACTIF");
        employeRepository.save(employe);
    }

    @Transactional(readOnly = true)
    public Employe getEmployeById(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<Employe> getAllEmployes(String searchTerm, Pageable pageable) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return employeRepository.findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, pageable);
        }
        return employeRepository.findAll(pageable);
    }
}
