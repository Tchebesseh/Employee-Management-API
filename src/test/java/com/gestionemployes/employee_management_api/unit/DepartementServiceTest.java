package com.gestionemployes.employee_management_api.unit;

import com.gestionemployes.employee_management_api.dto.DepartementDto;
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
import com.gestionemployes.employee_management_api.service.DepartementService;
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

public class DepartementServiceTest {

    @Mock 
    private DepartementRepository departementRepository;

    @Mock 
    private EmployeRepository employeRepository;

    @Mock 
    private DepartementMapper departementMapper;

    @Mock 
    private EmployeMapper employeMapper;

    @InjectMocks 
    private DepartementService departementService;

    @BeforeEach 
    void setUp() {
        MockitoAnnotations.openMocks(this); 
        
        
        
        
        when(departementMapper.toEntity(any(DepartementRequest.class)))
                .thenAnswer(invocation -> {
                    DepartementRequest req = invocation.getArgument(0);
                    Departement d = new Departement(req.getNom(), req.getManagerId(), req.getBudget());
                    
                    
                    return d;
                });
        when(departementMapper.toDto(any(Departement.class)))
                .thenAnswer(invocation -> {
                    Departement d = invocation.getArgument(0);
                    return new DepartementDto(d.getId(), d.getNom(), d.getManagerId(), d.getBudget());
                });
        when(employeMapper.toDto(any(Employe.class)))
                .thenAnswer(invocation -> {
                    Employe e = invocation.getArgument(0);
                    
                    return new EmployeDto(e.getId(), e.getPrenom(), e.getNom(), e.getEmail(),
                                          e.getDepartement() != null ? e.getDepartement().getId() : null,
                                          e.getSalaire(), e.getDateEmbauche(), e.getStatut());
                });
    }

    @Test
    void createDepartement_shouldCreateSuccessfully() {
        
        DepartementRequest request = new DepartementRequest("Ventes", null, BigDecimal.valueOf(50000));
        Departement newDepartement = new Departement("Ventes", null, BigDecimal.valueOf(50000));
        newDepartement.setId(1L);

        
        when(departementRepository.existsByNom(request.getNom())).thenReturn(false); 
        when(departementRepository.save(any(Departement.class))).thenReturn(newDepartement); 

        
        Departement createdDepartement = departementService.createDepartement(request);

        assertNotNull(createdDepartement);
        assertEquals("Ventes", createdDepartement.getNom());
        assertEquals(BigDecimal.valueOf(50000), createdDepartement.getBudget());
        assertNull(createdDepartement.getManagerId());
        verify(departementRepository, times(1)).existsByNom(request.getNom());
        verify(departementRepository, times(1)).save(any(Departement.class));
        verify(employeRepository, never()).findById(anyLong()); 
        verify(departementMapper, times(1)).toEntity(request); 
    }

    @Test
    void createDepartement_shouldThrowExceptionIfNameExists() {
        
        DepartementRequest request = new DepartementRequest("Ventes", null, BigDecimal.valueOf(50000));

        
        when(departementRepository.existsByNom(request.getNom())).thenReturn(true); 

        
        assertThrows(BadRequestException.class, () -> departementService.createDepartement(request));
        verify(departementRepository, times(1)).existsByNom(request.getNom());
        verify(departementRepository, never()).save(any(Departement.class));
        verify(employeRepository, never()).findById(anyLong());
        verify(departementMapper, never()).toEntity(any(DepartementRequest.class)); 
    }

    @Test
    void createDepartement_shouldSetManagerIfProvidedAndExists() {
        
        DepartementRequest request = new DepartementRequest("Marketing", 2L, BigDecimal.valueOf(70000));
        Departement newDepartement = new Departement("Marketing", 2L, BigDecimal.valueOf(70000));
        newDepartement.setId(1L);

        Employe manager = new Employe("Jane", "Doe", "jane.doe@example.com", null, BigDecimal.valueOf(60000), LocalDate.now(), "ACTIF");
        manager.setId(2L);

        
        when(departementRepository.existsByNom(request.getNom())).thenReturn(false);
        when(employeRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(departementRepository.save(any(Departement.class))).thenReturn(newDepartement);

        
        Departement createdDepartement = departementService.createDepartement(request);

        assertNotNull(createdDepartement);
        assertEquals(2L, createdDepartement.getManagerId());
        verify(departementRepository, times(1)).existsByNom(request.getNom());
        verify(employeRepository, times(1)).findById(2L);
        verify(departementRepository, times(1)).save(any(Departement.class));
        verify(departementMapper, times(1)).toEntity(request);
    }

    @Test
    void createDepartement_shouldThrowExceptionIfManagerDoesNotExist() {
        
        DepartementRequest request = new DepartementRequest("Marketing", 99L, BigDecimal.valueOf(70000));

        
        when(departementRepository.existsByNom(request.getNom())).thenReturn(false);
        when(employeRepository.findById(99L)).thenReturn(Optional.empty()); 

        
        assertThrows(ResourceNotFoundException.class, () -> departementService.createDepartement(request));
        verify(departementRepository, times(1)).existsByNom(request.getNom());
        verify(employeRepository, times(1)).findById(99L);
        verify(departementRepository, never()).save(any(Departement.class));
        verify(departementMapper, times(1)).toEntity(request); 
    }

    @Test
    void updateDepartement_shouldUpdateSuccessfully() {
        Long departementId = 1L;
        
        Departement existingDepartement = new Departement("Anciennes Ventes", null, BigDecimal.valueOf(40000));
        existingDepartement.setId(departementId);

        DepartementRequest request = new DepartementRequest("Nouvelles Ventes", null, BigDecimal.valueOf(60000));

        
        when(departementRepository.findById(departementId)).thenReturn(Optional.of(existingDepartement));
        when(departementRepository.findByNom(request.getNom())).thenReturn(Optional.empty()); 
        when(departementRepository.save(any(Departement.class))).thenReturn(existingDepartement);

        
        Departement updatedDepartement = departementService.updateDepartement(departementId, request);

        
        assertNotNull(updatedDepartement);
        assertEquals("Nouvelles Ventes", updatedDepartement.getNom());
        assertEquals(BigDecimal.valueOf(60000), updatedDepartement.getBudget());

        verify(departementRepository, times(1)).findById(departementId);
        verify(departementRepository, times(1)).findByNom(request.getNom()); 
        verify(departementRepository, times(1)).save(existingDepartement);
    }


    @Test
    void updateDepartement_shouldThrowExceptionIfDepartementNotFound() {
        
        Long departementId = 99L;
        DepartementRequest request = new DepartementRequest("Nom Test", null, BigDecimal.valueOf(1000));

        
        when(departementRepository.findById(departementId)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> departementService.updateDepartement(departementId, request));
        verify(departementRepository, times(1)).findById(departementId);
        verify(departementRepository, never()).existsByNom(anyString());
        verify(departementRepository, never()).save(any(Departement.class));
    }

    @Test
    void updateDepartement_shouldThrowExceptionIfNewNameExists() {
        Long departementId = 1L;

        Departement existingDepartement = new Departement("Ancien Nom", null, BigDecimal.valueOf(1000));
        existingDepartement.setId(departementId);

        DepartementRequest request = new DepartementRequest("Nom Existant", null, BigDecimal.valueOf(2000));

        Departement departementAvecNomExistant = new Departement("Nom Existant", null, BigDecimal.valueOf(5000));
        departementAvecNomExistant.setId(2L); 

        
        when(departementRepository.findById(departementId)).thenReturn(Optional.of(existingDepartement));
        when(departementRepository.findByNom(request.getNom())).thenReturn(Optional.of(departementAvecNomExistant)); 

        
        assertThrows(BadRequestException.class, () -> departementService.updateDepartement(departementId, request));

        verify(departementRepository, times(1)).findById(departementId);
        verify(departementRepository, times(1)).findByNom(request.getNom());
        verify(departementRepository, never()).save(any(Departement.class));
    }


    @Test
    void updateDepartement_shouldUpdateManagerIfProvidedAndExists() {
        Long departementId = 1L;
        Departement existingDepartement = new Departement("IT", 1L, BigDecimal.valueOf(50000));
        existingDepartement.setId(departementId);

        Employe newManager = new Employe("Alice", "Smith", "alice.smith@example.com", existingDepartement, BigDecimal.valueOf(70000), LocalDate.now(), "ACTIF");
        newManager.setId(3L);

        DepartementRequest request = new DepartementRequest("IT", 3L, BigDecimal.valueOf(55000));

        when(departementRepository.findById(departementId)).thenReturn(Optional.of(existingDepartement));
        when(departementRepository.findByNom("IT")).thenReturn(Optional.of(existingDepartement)); 
        when(employeRepository.findById(3L)).thenReturn(Optional.of(newManager));
        when(departementRepository.save(any(Departement.class))).thenReturn(existingDepartement);

        Departement updatedDepartement = departementService.updateDepartement(departementId, request);

        assertNotNull(updatedDepartement);
        assertEquals(3L, updatedDepartement.getManagerId());

        verify(departementRepository, times(1)).findById(departementId);
        verify(employeRepository, times(1)).findById(3L);
        verify(departementRepository, times(1)).save(existingDepartement);
    }

    @Test
    void deleteDepartement_shouldDeleteSuccessfully() {
        
        Long departementId = 1L;
        Departement departement = new Departement("HR à supprimer", null, BigDecimal.valueOf(50000));
        departement.setId(departementId);

        when(departementRepository.findById(departementId)).thenReturn(Optional.of(departement));
        when(employeRepository.existsByDepartementId(departementId)).thenReturn(false); 
        doNothing().when(departementRepository).delete(departement);

        
        departementService.deleteDepartement(departementId);

        
        verify(departementRepository, times(1)).findById(departementId);
        verify(employeRepository, times(1)).existsByDepartementId(departementId);
        verify(departementRepository, times(1)).delete(departement);
    }

    @Test
    void deleteDepartement_shouldThrowExceptionIfDepartementNotFound() {
        
        Long departementId = 99L;

        when(departementRepository.findById(departementId)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> departementService.deleteDepartement(departementId));
        verify(departementRepository, times(1)).findById(departementId);
        verify(employeRepository, never()).existsByDepartementId(anyLong());
        verify(departementRepository, never()).delete(any(Departement.class));
    }

    @Test
    void deleteDepartement_shouldThrowExceptionIfEmployesAssociated() {
        
        Long departementId = 1L;
        Departement departement = new Departement("Ventes avec employés", null, BigDecimal.valueOf(50000));
        departement.setId(departementId);

        when(departementRepository.findById(departementId)).thenReturn(Optional.of(departement));
        when(employeRepository.existsByDepartementId(departementId)).thenReturn(true); 

        
        assertThrows(BadRequestException.class, () -> departementService.deleteDepartement(departementId));
        verify(departementRepository, times(1)).findById(departementId);
        verify(employeRepository, times(1)).existsByDepartementId(departementId);
        verify(departementRepository, never()).delete(any(Departement.class));
    }


    @Test
    void getDepartementById_shouldReturnDepartement() {
        
        Long departementId = 1L;
        Departement departement = new Departement("Ventes", null, BigDecimal.valueOf(50000));
        departement.setId(departementId);

        
        when(departementRepository.findById(departementId)).thenReturn(Optional.of(departement));

        
        Departement foundDepartement = departementService.getDepartementById(departementId);

        assertNotNull(foundDepartement);
        assertEquals(departementId, foundDepartement.getId());
        verify(departementRepository, times(1)).findById(departementId);
    }

    @Test
    void getDepartementById_shouldThrowExceptionIfNotFound() {
        
        Long departementId = 99L;

        
        when(departementRepository.findById(departementId)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> departementService.getDepartementById(departementId));
        verify(departementRepository, times(1)).findById(departementId);
    }

    @Test
    void getAllDepartements_shouldReturnPageOfDepartements() {
        
        Pageable pageable = Pageable.unpaged(); 
        Departement d1 = new Departement("HR", null, BigDecimal.valueOf(30000));
        d1.setId(1L);
        Departement d2 = new Departement("Finance", null, BigDecimal.valueOf(80000));
        d2.setId(2L);
        List<Departement> departements = Arrays.asList(d1, d2);
        Page<Departement> departementPage = new PageImpl<>(departements, pageable, departements.size());

        
        when(departementRepository.findAll(pageable)).thenReturn(departementPage);

        
        Page<Departement> result = departementService.getAllDepartements(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("HR", result.getContent().get(0).getNom());
        verify(departementRepository, times(1)).findAll(pageable);
    }

    @Test
    void getEmployesByDepartementId_shouldReturnListOfEmployesDto() { 
        
        Long departementId = 1L;
        Departement departement = new Departement("IT", null, BigDecimal.valueOf(100000));
        departement.setId(departementId);

        Employe emp1 = new Employe("A", "B", "a@b.com", departement, BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF");
        emp1.setId(10L);
        Employe emp2 = new Employe("C", "D", "c@d.com", departement, BigDecimal.valueOf(60000), LocalDate.now(), "ACTIF");
        emp2.setId(11L);
        List<Employe> employesEntities = Arrays.asList(emp1, emp2);
        Page<Employe> employePage = new PageImpl<>(employesEntities); 

        
        when(departementRepository.findById(departementId)).thenReturn(Optional.of(departement));
        when(employeRepository.findByDepartementId(eq(departementId), any(Pageable.class))).thenReturn(employePage);
        
        when(employeMapper.toDto(emp1)).thenReturn(new EmployeDto(emp1.getId(), emp1.getPrenom(), emp1.getNom(), emp1.getEmail(), emp1.getDepartement().getId(), emp1.getSalaire(), emp1.getDateEmbauche(), emp1.getStatut()));
        when(employeMapper.toDto(emp2)).thenReturn(new EmployeDto(emp2.getId(), emp2.getPrenom(), emp2.getNom(), emp2.getEmail(), emp2.getDepartement().getId(), emp2.getSalaire(), emp2.getDateEmbauche(), emp2.getStatut()));


        
        List<EmployeDto> result = departementService.getEmployesByDepartementId(departementId); 

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getPrenom()); 
        assertEquals("a@b.com", result.get(0).getEmail());
        verify(departementRepository, times(1)).findById(departementId);
        verify(employeRepository, times(1)).findByDepartementId(eq(departementId), any(Pageable.class));
        verify(employeMapper, times(1)).toDto(emp1); 
        verify(employeMapper, times(1)).toDto(emp2);
    }

    @Test
    void getEmployesByDepartementId_shouldThrowExceptionIfDepartementNotFound() {
        
        Long departementId = 99L;

        
        when(departementRepository.findById(departementId)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> departementService.getEmployesByDepartementId(departementId));
        verify(departementRepository, times(1)).findById(departementId);
        verify(employeRepository, never()).findByDepartementId(anyLong(), any(Pageable.class));
        verify(employeMapper, never()).toDto(any(Employe.class)); 
    }

    @Test
    void getDepartementBudgetAnalysis_shouldReturnBudget() {
        
        Long departementId = 1L;
        Departement departement = new Departement("Dev", null, BigDecimal.valueOf(120000));
        departement.setId(departementId);

        
        when(departementRepository.findById(departementId)).thenReturn(Optional.of(departement));

        
        BigDecimal budget = departementService.getDepartementBudgetAnalysis(departementId);

        assertNotNull(budget);
        assertEquals(BigDecimal.valueOf(120000), budget);
        verify(departementRepository, times(1)).findById(departementId);
    }
}
