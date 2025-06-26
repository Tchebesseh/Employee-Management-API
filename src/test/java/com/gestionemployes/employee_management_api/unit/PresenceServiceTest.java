package com.gestionemployes.employee_management_api.unit;

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
import com.gestionemployes.employee_management_api.service.PresenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl; 
import org.springframework.data.domain.Pageable; 

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PresenceServiceTest {

    @Mock
    private PresenceRepository presenceRepository;
    @Mock
    private EmployeRepository employeRepository;
    @Mock
    private DepartementRepository departementRepository;

    @InjectMocks
    private PresenceService presenceService;

    private Employe testEmploye;
    private Departement testDepartement;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testDepartement = new Departement("HR", null, BigDecimal.valueOf(50000));
        testDepartement.setId(1L);
        testEmploye = new Employe("Test", "Employe", "test@example.com", testDepartement, BigDecimal.valueOf(30000), LocalDate.now(), "ACTIF");
        testEmploye.setId(1L);
    }

    @Test
    void pointageArrivee_shouldCreateSuccessfully() {
        
        PresenceArriveeRequest request = new PresenceArriveeRequest(testEmploye.getId(), LocalDate.now(), LocalTime.of(9, 0));
        Presence newPresence = new Presence(testEmploye, request.getDate(), request.getArrivee());
        newPresence.setId(1L);

        when(employeRepository.findById(request.getEmployeId())).thenReturn(Optional.of(testEmploye));
        when(presenceRepository.findByEmployeIdAndDate(testEmploye.getId(), request.getDate())).thenReturn(Optional.empty());
        when(presenceRepository.save(any(Presence.class))).thenReturn(newPresence);

        
        Presence createdPresence = presenceService.pointageArrivee(request);

        
        assertNotNull(createdPresence);
        assertEquals(request.getArrivee(), createdPresence.getArrivee());
        verify(employeRepository, times(1)).findById(request.getEmployeId());
        verify(presenceRepository, times(1)).findByEmployeIdAndDate(testEmploye.getId(), request.getDate());
        verify(presenceRepository, times(1)).save(any(Presence.class));
    }

    @Test
    void pointageArrivee_shouldThrowExceptionIfEmployeNotFound() {
        
        PresenceArriveeRequest request = new PresenceArriveeRequest(99L, LocalDate.now(), LocalTime.of(9, 0));

        when(employeRepository.findById(request.getEmployeId())).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> presenceService.pointageArrivee(request));
        verify(employeRepository, times(1)).findById(request.getEmployeId());
        verify(presenceRepository, never()).findByEmployeIdAndDate(anyLong(), any(LocalDate.class));
        verify(presenceRepository, never()).save(any(Presence.class));
    }

    @Test
    void pointageArrivee_shouldThrowExceptionIfAlreadyClockedInAndNotClockedOut() {
        
        PresenceArriveeRequest request = new PresenceArriveeRequest(testEmploye.getId(), LocalDate.now(), LocalTime.of(9, 0));
        Presence existingPresence = new Presence(testEmploye, request.getDate(), request.getArrivee());
        existingPresence.setId(1L); 

        when(employeRepository.findById(request.getEmployeId())).thenReturn(Optional.of(testEmploye));
        when(presenceRepository.findByEmployeIdAndDate(testEmploye.getId(), request.getDate())).thenReturn(Optional.of(existingPresence));

        
        assertThrows(BadRequestException.class, () -> presenceService.pointageArrivee(request));
        verify(employeRepository, times(1)).findById(request.getEmployeId());
        verify(presenceRepository, times(1)).findByEmployeIdAndDate(testEmploye.getId(), request.getDate());
        verify(presenceRepository, never()).save(any(Presence.class));
    }

    @Test
    void pointageArrivee_shouldThrowExceptionIfAlreadyCompletedForToday() {
        
        PresenceArriveeRequest request = new PresenceArriveeRequest(testEmploye.getId(), LocalDate.now(), LocalTime.of(9, 0));
        Presence existingCompletedPresence = new Presence(testEmploye, request.getDate(), request.getArrivee());
        existingCompletedPresence.setDepart(LocalTime.of(17, 0)); 
        existingCompletedPresence.setId(1L);

        when(employeRepository.findById(request.getEmployeId())).thenReturn(Optional.of(testEmploye));
        when(presenceRepository.findByEmployeIdAndDate(testEmploye.getId(), request.getDate())).thenReturn(Optional.of(existingCompletedPresence));

        
        assertThrows(BadRequestException.class, () -> presenceService.pointageArrivee(request));
        verify(employeRepository, times(1)).findById(request.getEmployeId());
        verify(presenceRepository, times(1)).findByEmployeIdAndDate(testEmploye.getId(), request.getDate());
        verify(presenceRepository, never()).save(any(Presence.class));
    }


    @Test
    void pointageDepart_shouldUpdateSuccessfullyAndCalculateHours() {
        
        PresenceDepartRequest request = new PresenceDepartRequest(1L, LocalTime.of(17, 0));
        Presence existingPresence = new Presence(testEmploye, LocalDate.now(), LocalTime.of(9, 0));
        existingPresence.setId(1L); 

        when(presenceRepository.findById(request.getPresenceId())).thenReturn(Optional.of(existingPresence));
        when(presenceRepository.save(any(Presence.class))).thenReturn(existingPresence);

        
        Presence updatedPresence = presenceService.pointageDepart(request);

        
        assertNotNull(updatedPresence);
        assertEquals(request.getDepart(), updatedPresence.getDepart());
        assertEquals(Duration.between(LocalTime.of(9,0), LocalTime.of(17,0)).toMinutes(), updatedPresence.getHeuresTravaillees()); 
        verify(presenceRepository, times(1)).findById(request.getPresenceId());
        verify(presenceRepository, times(1)).save(existingPresence);
    }

    @Test
    void pointageDepart_shouldThrowExceptionIfPresenceNotFound() {
        
        PresenceDepartRequest request = new PresenceDepartRequest(99L, LocalTime.of(17, 0));

        when(presenceRepository.findById(request.getPresenceId())).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> presenceService.pointageDepart(request));
        verify(presenceRepository, times(1)).findById(request.getPresenceId());
        verify(presenceRepository, never()).save(any(Presence.class));
    }

    @Test
    void pointageDepart_shouldThrowExceptionIfAlreadyClockedOut() {
        
        PresenceDepartRequest request = new PresenceDepartRequest(1L, LocalTime.of(17, 0));
        Presence existingPresence = new Presence(testEmploye, LocalDate.now(), LocalTime.of(9, 0));
        existingPresence.setDepart(LocalTime.of(16, 0)); 
        existingPresence.setId(1L);

        when(presenceRepository.findById(request.getPresenceId())).thenReturn(Optional.of(existingPresence));

        
        assertThrows(BadRequestException.class, () -> presenceService.pointageDepart(request));
        verify(presenceRepository, times(1)).findById(request.getPresenceId());
        verify(presenceRepository, never()).save(any(Presence.class));
    }

    @Test
    void pointageDepart_shouldThrowExceptionIfDepartureBeforeArrival() {
        
        PresenceDepartRequest request = new PresenceDepartRequest(1L, LocalTime.of(8, 0)); 
        Presence existingPresence = new Presence(testEmploye, LocalDate.now(), LocalTime.of(9, 0));
        existingPresence.setId(1L);

        when(presenceRepository.findById(request.getPresenceId())).thenReturn(Optional.of(existingPresence));

        
        assertThrows(BadRequestException.class, () -> presenceService.pointageDepart(request));
        verify(presenceRepository, times(1)).findById(request.getPresenceId());
        verify(presenceRepository, never()).save(any(Presence.class));
    }

    @Test
    void getMonthlyPresenceReport_shouldReturnReportForGivenMonth() {
        
        LocalDate dateInMonth = LocalDate.of(2024, 6, 15);
        Presence p1 = new Presence(testEmploye, LocalDate.of(2024, 6, 10), LocalTime.of(9, 0)); p1.setDepart(LocalTime.of(17,0)); p1.calculateHeuresTravaillees();
        Presence p2 = new Presence(testEmploye, LocalDate.of(2024, 6, 15), LocalTime.of(9, 0)); p2.setDepart(LocalTime.of(17,0)); p2.calculateHeuresTravaillees();
        Presence p3_outsideMonth = new Presence(testEmploye, LocalDate.of(2024, 5, 20), LocalTime.of(9, 0)); p3_outsideMonth.setDepart(LocalTime.of(17,0)); p3_outsideMonth.calculateHeuresTravaillees();

        List<Presence> allPresences = Arrays.asList(p1, p2, p3_outsideMonth);

        when(employeRepository.existsById(testEmploye.getId())).thenReturn(true);
        when(presenceRepository.findByEmployeIdOrderByDateAsc(testEmploye.getId())).thenReturn(allPresences);

        
        List<Presence> report = presenceService.getMonthlyPresenceReport(testEmploye.getId(), 2024, 6);

        
        assertNotNull(report);
        assertEquals(2, report.size());
        assertTrue(report.contains(p1));
        assertTrue(report.contains(p2));
        assertFalse(report.contains(p3_outsideMonth));
        verify(employeRepository, times(1)).existsById(testEmploye.getId());
        verify(presenceRepository, times(1)).findByEmployeIdOrderByDateAsc(testEmploye.getId());
    }

    @Test
    void getMonthlyPresenceReport_shouldThrowExceptionIfEmployeNotFound() {
        
        Long employeId = 99L;
        when(employeRepository.existsById(employeId)).thenReturn(false);

        
        assertThrows(ResourceNotFoundException.class, () -> presenceService.getMonthlyPresenceReport(employeId, 2024, 6));
        verify(employeRepository, times(1)).existsById(employeId);
        verify(presenceRepository, never()).findByEmployeIdOrderByDateAsc(anyLong());
    }

    @Test
    void getDepartementPresenceSummary_shouldReturnSummaryForDepartment() {
        
        Departement dept = new Departement("Dev", null, BigDecimal.valueOf(100000));
        dept.setId(1L);
        Employe emp1 = new Employe("Emp1", "Dev", "emp1@dev.com", dept, BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF");
        emp1.setId(10L);
        Employe emp2 = new Employe("Emp2", "Dev", "emp2@dev.com", dept, BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF");
        emp2.setId(11L);

        Presence p1 = new Presence(emp1, LocalDate.of(2024, 6, 1), LocalTime.of(9,0)); p1.setDepart(LocalTime.of(17,0)); p1.calculateHeuresTravaillees();
        Presence p2 = new Presence(emp2, LocalDate.of(2024, 6, 1), LocalTime.of(9,0)); p2.setDepart(LocalTime.of(17,0)); p2.calculateHeuresTravaillees();
        Presence p3 = new Presence(emp1, LocalDate.of(2024, 6, 2), LocalTime.of(9,0)); p3.setDepart(LocalTime.of(17,0)); p3.calculateHeuresTravaillees();

        when(departementRepository.findById(dept.getId())).thenReturn(Optional.of(dept));
        when(employeRepository.findByDepartementId(eq(dept.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(emp1, emp2)));
        when(presenceRepository.findByEmployeIdOrderByDateAsc(emp1.getId())).thenReturn(Arrays.asList(p1, p3));
        when(presenceRepository.findByEmployeIdOrderByDateAsc(emp2.getId())).thenReturn(Collections.singletonList(p2));

        
        List<Presence> summary = presenceService.getDepartementPresenceSummary(dept.getId());

        
        assertNotNull(summary);
        assertEquals(3, summary.size());
        assertTrue(summary.contains(p1));
        assertTrue(summary.contains(p2));
        assertTrue(summary.contains(p3));
        verify(departementRepository, times(1)).findById(dept.getId());
        verify(employeRepository, times(1)).findByDepartementId(eq(dept.getId()), any(Pageable.class));
        verify(presenceRepository, times(1)).findByEmployeIdOrderByDateAsc(emp1.getId());
        verify(presenceRepository, times(1)).findByEmployeIdOrderByDateAsc(emp2.getId());
    }

    @Test
    void getDepartementPresenceSummary_shouldThrowExceptionIfDepartementNotFound() {
        
        Long departementId = 99L;
        when(departementRepository.findById(departementId)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> presenceService.getDepartementPresenceSummary(departementId));
        verify(departementRepository, times(1)).findById(departementId);
        verify(employeRepository, never()).findByDepartementId(anyLong(), any(Pageable.class));
        verify(presenceRepository, never()).findByEmployeIdOrderByDateAsc(anyLong());
    }
}
