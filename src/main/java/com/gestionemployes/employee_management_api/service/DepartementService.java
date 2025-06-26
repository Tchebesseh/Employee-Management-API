package com.gestionemployes.employee_management_api.service;

import com.gestionemployes.employee_management_api.dto.DepartementRequest;
import com.gestionemployes.employee_management_api.dto.EmployeDto;
import com.gestionemployes.employee_management_api.exception.BadRequestException;
import com.gestionemployes.employee_management_api.exception.ResourceNotFoundException;
import com.gestionemployes.employee_management_api.mapper.DepartementMapper;
import com.gestionemployes.employee_management_api.mapper.EmployeMapper;
import com.gestionemployes.employee_management_api.model.Departement;
import com.gestionemployes.employee_management_api.model.Employe;
import com.gestionemployes.employee_management_api.repository.DepartementRepository;
import com.gestionemployes.employee_management_api.repository.EmployeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional; 
import java.util.stream.Collectors;

@Service
public class DepartementService {

    private final DepartementRepository departementRepository;
    private final EmployeRepository employeRepository;
    private final DepartementMapper departementMapper;
    private final EmployeMapper employeMapper;

    public DepartementService(DepartementRepository departementRepository, EmployeRepository employeRepository,
                              DepartementMapper departementMapper, EmployeMapper employeMapper) {
        this.departementRepository = departementRepository;
        this.employeRepository = employeRepository;
        this.departementMapper = departementMapper;
        this.employeMapper = employeMapper;
    }

    @Transactional
    public Departement createDepartement(DepartementRequest departementRequest) {
        if (departementRepository.existsByNom(departementRequest.getNom())) {
            throw new BadRequestException("Un département avec le nom '" + departementRequest.getNom() + "' existe déjà.");
        }

        Departement departement = departementMapper.toEntity(departementRequest);

        if (departementRequest.getManagerId() != null) {
            Optional<Employe> managerOptional = employeRepository.findById(departementRequest.getManagerId());
            if (managerOptional.isEmpty()) {
                throw new ResourceNotFoundException("Manager", "id", departementRequest.getManagerId());
            }
            departement.setManagerId(departementRequest.getManagerId());
        }

        return departementRepository.save(departement);
    }

    @Transactional
    public Departement updateDepartement(Long id, DepartementRequest request) {
        Departement existingDepartement = departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département", "id", id));

        
        
        if (!existingDepartement.getNom().equals(request.getNom())) {
            
            if (departementRepository.findByNom(request.getNom()).isPresent()) {
                throw new BadRequestException("Un autre département avec le nom '" + request.getNom() + "' existe déjà.");
            }
        }

        existingDepartement.setNom(request.getNom());
        existingDepartement.setBudget(request.getBudget());

        // Règle métier : Le manager du département doit être un employé de ce département
        if (request.getManagerId() != null) {
            Employe managerEmploye = employeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", request.getManagerId()));

            
            if (managerEmploye.getDepartement() == null || !managerEmploye.getDepartement().getId().equals(existingDepartement.getId())) {
                throw new BadRequestException("Le manager avec l'ID " + request.getManagerId() + " doit appartenir au département " + existingDepartement.getNom() + " (ID: " + existingDepartement.getId() + ").");
            }
            existingDepartement.setManagerId(request.getManagerId());
        } else {
            existingDepartement.setManagerId(null);
        }

        return departementRepository.save(existingDepartement);
    }

    @Transactional
    public void deleteDepartement(Long id) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département", "id", id));

        if (employeRepository.existsByDepartementId(id)) {
            throw new BadRequestException("Impossible de supprimer le département avec l'ID " + id + " car il a des employés associés.");
        }

        departementRepository.delete(departement);
    }

    @Transactional(readOnly = true)
    public Departement getDepartementById(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<Departement> getAllDepartements(Pageable pageable) {
        return departementRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<EmployeDto> getEmployesByDepartementId(Long departementId) {
        Departement departement = departementRepository.findById(departementId)
                .orElseThrow(() -> new ResourceNotFoundException("Département", "id", departementId));

        return employeRepository.findByDepartementId(departementId, Pageable.unpaged())
                .getContent().stream()
                .map(employeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getDepartementBudgetAnalysis(Long departementId) {
        Departement departement = departementRepository.findById(departementId)
                .orElseThrow(() -> new ResourceNotFoundException("Département", "id", departementId));

        return departement.getBudget();
    }
}
