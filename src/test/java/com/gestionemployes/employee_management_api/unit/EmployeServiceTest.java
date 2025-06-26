package com.gestionemployes.employee_management_api.unit;

import com.gestionemployes.employee_management_api.dto.EmployeRequest;
import com.gestionemployes.employee_management_api.exception.BadRequestException;
import com.gestionemployes.employee_management_api.exception.ResourceNotFoundException;
import com.gestionemployes.employee_management_api.model.Departement;
import com.gestionemployes.employee_management_api.model.Employe;
import com.gestionemployes.employee_management_api.repository.DepartementRepository;
import com.gestionemployes.employee_management_api.repository.EmployeRepository;
import com.gestionemployes.employee_management_api.repository.PresenceRepository;
import com.gestionemployes.employee_management_api.service.EmployeService;
import com.gestionemployes.employee_management_api.mapper.EmployeMapper; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmployeServiceTest {

    @Mock
    private EmployeRepository employeRepository;
    @Mock
    private DepartementRepository departementRepository;
    @Mock
    private PresenceRepository presenceRepository;
    @Mock 
    private EmployeMapper employeMapper;

    @InjectMocks
    private EmployeService employeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEmploye_shouldCreateSuccessfully() {
        
        Departement departement = new Departement("IT", null, BigDecimal.valueOf(100000));
        departement.setId(1L);
        EmployeRequest request = new EmployeRequest(
                "Koffi", "Kouassi", "koffi.kouassi@example.com", 1L,
                BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF"
        );
        Employe newEmploye = new Employe("Koffi", "Kouassi", "koffi.kouassi@example.com", departement, BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF");
        newEmploye.setId(1L);

        when(employeRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(departementRepository.findById(request.getDepartementId())).thenReturn(Optional.of(departement));
        
        
        when(employeMapper.toEntity(any(EmployeRequest.class), any(Departement.class))).thenReturn(newEmploye);
        when(employeRepository.save(any(Employe.class))).thenReturn(newEmploye);

        
        Employe createdEmploye = employeService.createEmploye(request);

        
        assertNotNull(createdEmploye);
        assertEquals("Koffi", createdEmploye.getPrenom());
        verify(employeRepository, times(1)).existsByEmail(request.getEmail());
        verify(departementRepository, times(1)).findById(request.getDepartementId());
        verify(employeMapper, times(1)).toEntity(any(EmployeRequest.class), any(Departement.class)); 
        verify(employeRepository, times(1)).save(any(Employe.class));
    }

    @Test
    void createEmploye_shouldThrowExceptionIfEmailExists() {
        
        EmployeRequest request = new EmployeRequest(
                "Koffi", "Kouassi", "koffi.kouassi@example.com", 1L,
                BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF"
        );

        when(employeRepository.existsByEmail(request.getEmail())).thenReturn(true);

        
        assertThrows(BadRequestException.class, () -> employeService.createEmploye(request));
        verify(employeRepository, times(1)).existsByEmail(request.getEmail());
        verify(departementRepository, never()).findById(anyLong());
        verify(employeMapper, never()).toEntity(any(), any()); 
        verify(employeRepository, never()).save(any(Employe.class));
    }

    @Test
    void createEmploye_shouldThrowExceptionIfDepartementNotFound() {
        
        EmployeRequest request = new EmployeRequest(
                "Koffi", "Kouassi", "koffi.kouassi@example.com", 99L, 
                BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF"
        );

        when(employeRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(departementRepository.findById(request.getDepartementId())).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> employeService.createEmploye(request));
        verify(employeRepository, times(1)).existsByEmail(request.getEmail());
        verify(departementRepository, times(1)).findById(request.getDepartementId());
        verify(employeMapper, never()).toEntity(any(), any()); 
        verify(employeRepository, never()).save(any(Employe.class));
    }

    @Test
    void createEmploye_shouldThrowExceptionIfSalaryNotPositive() {
        
        Departement departement = new Departement("IT", null, BigDecimal.valueOf(100000));
        departement.setId(1L);
        EmployeRequest request = new EmployeRequest(
                "Koffi", "Kouassi", "koffi.kouassi@example.com", 1L,
                BigDecimal.valueOf(-100), LocalDate.now(), "ACTIF" 
        );

        when(employeRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(departementRepository.findById(request.getDepartementId())).thenReturn(Optional.of(departement));

        
        assertThrows(BadRequestException.class, () -> employeService.createEmploye(request));
        verify(employeRepository, times(1)).existsByEmail(request.getEmail());
        verify(departementRepository, times(1)).findById(request.getDepartementId());
        verify(employeMapper, never()).toEntity(any(), any()); 
        verify(employeRepository, never()).save(any(Employe.class));
    }

    @Test
    void createEmploye_shouldThrowExceptionIfSalaryExceedsDepartmentBudget() {
        
        Departement departement = new Departement("Petit Dépt", null, BigDecimal.valueOf(10000)); 
        departement.setId(1L);
        EmployeRequest request = new EmployeRequest(
                "Koffi", "Kouassi", "koffi.kouassi@example.com", 1L,
                BigDecimal.valueOf(15000), LocalDate.now(), "ACTIF" 
        );

        when(employeRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(departementRepository.findById(request.getDepartementId())).thenReturn(Optional.of(departement));

        
        assertThrows(BadRequestException.class, () -> employeService.createEmploye(request));
        verify(employeRepository, times(1)).existsByEmail(request.getEmail());
        verify(departementRepository, times(1)).findById(request.getDepartementId());
        verify(employeMapper, never()).toEntity(any(), any()); 
        verify(employeRepository, never()).save(any(Employe.class));
    }

    @Test
    void updateEmploye_shouldUpdateSuccessfully() {
        
        Long employeId = 1L;
        Departement oldDepartement = new Departement("Ancien Dépt", null, BigDecimal.valueOf(50000));
        oldDepartement.setId(10L);
        Employe existingEmploye = new Employe("Ancien", "Utilisateur", "ancien.utilisateur@example.com", oldDepartement, BigDecimal.valueOf(30000), LocalDate.now(), "ACTIF");
        existingEmploye.setId(employeId);

        Departement newDepartement = new Departement("Nouveau Dépt", null, BigDecimal.valueOf(70000));
        newDepartement.setId(20L);
        EmployeRequest request = new EmployeRequest(
                "Nouveau", "Utilisateur", "nouveau.utilisateur@example.com", 20L,
                BigDecimal.valueOf(40000), LocalDate.now().minusDays(1), "INACTIF"
        );

        when(employeRepository.findById(employeId)).thenReturn(Optional.of(existingEmploye));
        when(employeRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(departementRepository.findById(request.getDepartementId())).thenReturn(Optional.of(newDepartement));
        when(employeRepository.save(any(Employe.class))).thenReturn(existingEmploye); 

        
        Employe updatedEmploye = employeService.updateEmploye(employeId, request);

        
        assertNotNull(updatedEmploye);
        assertEquals("Nouveau", updatedEmploye.getPrenom());
        assertEquals("nouveau.utilisateur@example.com", updatedEmploye.getEmail());
        assertEquals(newDepartement.getId(), updatedEmploye.getDepartement().getId());
        verify(employeRepository, times(1)).findById(employeId);
        verify(employeRepository, times(1)).existsByEmail(request.getEmail());
        verify(departementRepository, times(1)).findById(request.getDepartementId());
        verify(employeRepository, times(1)).save(existingEmploye);
    }

    @Test
    void updateEmploye_shouldThrowExceptionIfEmployeNotFound() {
        
        Long employeId = 99L;
        EmployeRequest request = new EmployeRequest("Test", "Test", "test@test.com", 1L, BigDecimal.valueOf(100), LocalDate.now(), "ACTIF");

        when(employeRepository.findById(employeId)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> employeService.updateEmploye(employeId, request));
        verify(employeRepository, times(1)).findById(employeId);
        verify(employeRepository, never()).existsByEmail(anyString());
        verify(departementRepository, never()).findById(anyLong());
        verify(employeRepository, never()).save(any(Employe.class));
    }

    @Test
    void updateEmploye_shouldThrowExceptionIfEmailExistsForAnotherEmploye() {
        
        Long employeId = 1L;
        Departement departement = new Departement("IT", null, BigDecimal.valueOf(100000));
        departement.setId(1L);
        Employe existingEmploye = new Employe("Ancien", "Utilisateur", "ancien.utilisateur@example.com", departement, BigDecimal.valueOf(30000), LocalDate.now(), "ACTIF");
        existingEmploye.setId(employeId);

        EmployeRequest request = new EmployeRequest(
                "Aya", "Koné", "aya.kone@example.com", 1L, 
                BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF"
        );

        when(employeRepository.findById(employeId)).thenReturn(Optional.of(existingEmploye));
        when(employeRepository.existsByEmail(request.getEmail())).thenReturn(true); 

        
        assertThrows(BadRequestException.class, () -> employeService.updateEmploye(employeId, request));
        verify(employeRepository, times(1)).findById(employeId);
        verify(employeRepository, times(1)).existsByEmail(request.getEmail());
        verify(departementRepository, never()).findById(anyLong());
        verify(employeRepository, never()).save(any(Employe.class));
    }

    @Test
    void deactivateEmploye_shouldDeactivateSuccessfully() {
        
        Long employeId = 1L;
        Departement departement = new Departement("RH", null, BigDecimal.valueOf(50000));
        departement.setId(1L);
        Employe employe = new Employe("Moussa", "Traoré", "moussa.traore@example.com", departement, BigDecimal.valueOf(40000), LocalDate.now(), "ACTIF");
        employe.setId(employeId);

        when(employeRepository.findById(employeId)).thenReturn(Optional.of(employe));
        when(presenceRepository.existsByEmployeId(employeId)).thenReturn(false); 

        
        employeService.deactivateEmploye(employeId);

        
        assertEquals("INACTIF", employe.getStatut()); 
        verify(employeRepository, times(1)).findById(employeId);
        verify(presenceRepository, times(1)).existsByEmployeId(employeId);
        verify(employeRepository, times(1)).save(employe); 
    }

    @Test
    void deactivateEmploye_shouldThrowExceptionIfEmployeNotFound() {
        
        Long employeId = 99L;

        when(employeRepository.findById(employeId)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> employeService.deactivateEmploye(employeId));
        verify(employeRepository, times(1)).findById(employeId);
        verify(presenceRepository, never()).existsByEmployeId(anyLong());
        verify(employeRepository, never()).save(any(Employe.class));
    }

    @Test
    void deactivateEmploye_shouldThrowExceptionIfPresenceExists() {
        
        Long employeId = 1L;
        Departement departement = new Departement("RH", null, BigDecimal.valueOf(50000));
        departement.setId(1L);
        Employe employe = new Employe("Moussa", "Traoré", "moussa.traore@example.com", departement, BigDecimal.valueOf(40000), LocalDate.now(), "ACTIF");
        employe.setId(employeId);

        when(employeRepository.findById(employeId)).thenReturn(Optional.of(employe));
        when(presenceRepository.existsByEmployeId(employeId)).thenReturn(true); 

        
        assertThrows(BadRequestException.class, () -> employeService.deactivateEmploye(employeId));
        verify(employeRepository, times(1)).findById(employeId);
        verify(presenceRepository, times(1)).existsByEmployeId(employeId);
        verify(employeRepository, never()).save(any(Employe.class));
    }

    @Test
    void getEmployeById_shouldReturnEmploye() {
        
        Long employeId = 1L;
        Departement departement = new Departement("IT", null, BigDecimal.valueOf(100000));
        departement.setId(1L);
        Employe employe = new Employe("Fatou", "Camara", "fatou.camara@example.com", departement, BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF");
        employe.setId(employeId);

        when(employeRepository.findById(employeId)).thenReturn(Optional.of(employe));

        
        Employe foundEmploye = employeService.getEmployeById(employeId);

        
        assertNotNull(foundEmploye);
        assertEquals(employeId, foundEmploye.getId());
        verify(employeRepository, times(1)).findById(employeId);
    }

    @Test
    void getEmployeById_shouldThrowExceptionIfNotFound() {
        
        Long employeId = 99L;

        when(employeRepository.findById(employeId)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> employeService.getEmployeById(employeId));
        verify(employeRepository, times(1)).findById(employeId);
    }

    @Test
    void getAllEmployes_shouldReturnPageOfEmployesWithoutSearchTerm() {
        
        Pageable pageable = Pageable.unpaged();
        Departement departement = new Departement("IT", null, BigDecimal.valueOf(100000));
        departement.setId(1L);
        Employe emp1 = new Employe("Adama", "Diallo", "adama.diallo@example.com", departement, BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF");
        Employe emp2 = new Employe("Mariam", "Doumbia", "mariam.doumbia@example.com", departement, BigDecimal.valueOf(60000), LocalDate.now(), "ACTIF");
        List<Employe> employes = Arrays.asList(emp1, emp2);
        Page<Employe> employePage = new PageImpl<>(employes, pageable, employes.size());

        when(employeRepository.findAll(pageable)).thenReturn(employePage);

        
        Page<Employe> result = employeService.getAllEmployes(null, pageable);

        
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(employeRepository, times(1)).findAll(pageable);
        verify(employeRepository, never()).findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void getAllEmployes_shouldReturnPageOfEmployesWithSearchTerm() {
        
        Pageable pageable = Pageable.unpaged();
        String searchTerm = "Koffi";
        Departement departement = new Departement("IT", null, BigDecimal.valueOf(100000));
        departement.setId(1L);
        Employe emp1 = new Employe("Koffi", "Kouassi", "koffi.kouassi@example.com", departement, BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF");
        List<Employe> employes = Collections.singletonList(emp1);
        Page<Employe> employePage = new PageImpl<>(employes, pageable, employes.size());

        when(employeRepository.findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, pageable)).thenReturn(employePage);

        
        Page<Employe> result = employeService.getAllEmployes(searchTerm, pageable);

        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeRepository, times(1)).findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, pageable);
        verify(employeRepository, never()).findAll(any(Pageable.class));
    }
}
