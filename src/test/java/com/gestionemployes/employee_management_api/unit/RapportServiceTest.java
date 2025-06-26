package com.gestionemployes.employee_management_api.unit;

import com.gestionemployes.employee_management_api.model.Departement;
import com.gestionemployes.employee_management_api.model.Employe;
import com.gestionemployes.employee_management_api.model.Presence;
import com.gestionemployes.employee_management_api.repository.DepartementRepository;
import com.gestionemployes.employee_management_api.repository.EmployeRepository;
import com.gestionemployes.employee_management_api.repository.PresenceRepository;
import com.gestionemployes.employee_management_api.service.RapportService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RapportServiceTest {

    @Mock
    private EmployeRepository employeRepository;
    @Mock
    private DepartementRepository departementRepository;
    @Mock
    private PresenceRepository presenceRepository;

    @InjectMocks
    private RapportService rapportService;

    private Departement devDepartement;
    private Departement hrDepartement;
    private Employe devLead;
    private Employe hrAssociate;
    private Employe devJunior;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        devDepartement = new Departement("Development", null, BigDecimal.valueOf(100000));
        devDepartement.setId(1L);

        hrDepartement = new Departement("Human Resources", null, BigDecimal.valueOf(80000));
        hrDepartement.setId(2L);

        devLead = new Employe("John", "Doe", "john@example.com", devDepartement,
                BigDecimal.valueOf(70000), LocalDate.now(), "ACTIF");
        devLead.setId(10L);

        devJunior = new Employe("Jane", "Smith", "jane@example.com", devDepartement,
                BigDecimal.valueOf(50000), LocalDate.now(), "ACTIF");
        devJunior.setId(11L);

        hrAssociate = new Employe("Paul", "Brown", "paul@example.com", hrDepartement,
                BigDecimal.valueOf(45000), LocalDate.now(), "ACTIF");
        hrAssociate.setId(20L);
    }

    @Test
    void getResumeSalairesParDepartement_shouldReturnCorrectStatistics() {
        
        when(departementRepository.findAll()).thenReturn(Arrays.asList(devDepartement, hrDepartement));

        when(employeRepository.findByDepartementId(devDepartement.getId()))
                .thenReturn(Arrays.asList(devLead, devJunior));

        when(employeRepository.findByDepartementId(hrDepartement.getId()))
                .thenReturn(Collections.singletonList(hrAssociate));

        
        Map<String, Map<String, BigDecimal>> result = rapportService.getResumeSalairesParDepartement();

        
        assertNotNull(result);
        assertEquals(2, result.size());

        Map<String, BigDecimal> devStats = result.get("Development");
        assertNotNull(devStats);
        assertEquals(BigDecimal.valueOf(120000), devStats.get("totalSalary"));
        assertEquals(BigDecimal.valueOf(60000.00).setScale(2), devStats.get("averageSalary"));
        assertEquals(BigDecimal.valueOf(70000), devStats.get("maxSalary"));
        assertEquals(BigDecimal.valueOf(50000), devStats.get("minSalary"));

        Map<String, BigDecimal> hrStats = result.get("Human Resources");
        assertNotNull(hrStats);
        assertEquals(BigDecimal.valueOf(45000), hrStats.get("totalSalary"));
        assertEquals(BigDecimal.valueOf(45000.00).setScale(2), hrStats.get("averageSalary"));
        assertEquals(BigDecimal.valueOf(45000), hrStats.get("maxSalary"));
        assertEquals(BigDecimal.valueOf(45000), hrStats.get("minSalary"));

        
        verify(departementRepository, times(1)).findAll();
        verify(employeRepository, times(1)).findByDepartementId(devDepartement.getId());
        verify(employeRepository, times(1)).findByDepartementId(hrDepartement.getId());
    }

    @Test
    void getResumeSalairesParDepartement_shouldHandleEmptyDepartment() {
        
        Departement emptyDepartement = new Departement("Empty", null, BigDecimal.valueOf(10000));
        emptyDepartement.setId(3L);
        when(departementRepository.findAll()).thenReturn(Collections.singletonList(emptyDepartement));
        when(employeRepository.findByDepartementId(eq(emptyDepartement.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList())); 

        
        Map<String, Map<String, BigDecimal>> result = rapportService.getResumeSalairesParDepartement();

        
        assertNotNull(result);
        assertEquals(1, result.size());

        Map<String, BigDecimal> emptyDeptStats = result.get("Empty");
        assertNotNull(emptyDeptStats);
        assertEquals(BigDecimal.ZERO, emptyDeptStats.get("totalSalary"));
        assertEquals(BigDecimal.ZERO, emptyDeptStats.get("averageSalary"));
        assertEquals(BigDecimal.ZERO, emptyDeptStats.get("maxSalary"));
        assertEquals(BigDecimal.ZERO, emptyDeptStats.get("minSalary"));
    }

    @Test
    void getTendancesPresences_shouldReturnDailyHoursTrends() {
        
        Presence p1 = new Presence(devLead, LocalDate.of(2024, 6, 20), LocalTime.of(9, 0));
        p1.setDepart(LocalTime.of(17, 0)); 
        p1.calculateHeuresTravaillees();

        Presence p2 = new Presence(hrAssociate, LocalDate.of(2024, 6, 20), LocalTime.of(9, 0));
        p2.setDepart(LocalTime.of(17, 0)); 
        p2.calculateHeuresTravaillees();

        Presence p3 = new Presence(devLead, LocalDate.of(2024, 6, 21), LocalTime.of(10, 0));
        p3.setDepart(LocalTime.of(18, 0)); 
        p3.calculateHeuresTravaillees();

        List<Presence> allPresences = Arrays.asList(p1, p2, p3);
        when(presenceRepository.findAll()).thenReturn(allPresences);

        
        Map<String, String> result = rapportService.getPresenceTrendsAndStats().getTotalHoursByDayOfWeek();

        
        assertNotNull(result);
        assertEquals(2, result.size()); 

        
        
        
        assertEquals("16h 00m", result.get(LocalDate.of(2024, 6, 20).getDayOfWeek().toString()));
        assertEquals("8h 00m", result.get(LocalDate.of(2024, 6, 21).getDayOfWeek().toString()));

        verify(presenceRepository, times(1)).findAll();
    }

    @Test
    void getTendancesPresences_shouldHandleNoPresenceData() {
        
        when(presenceRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, String> result = rapportService.getPresenceTrendsAndStats().getTotalHoursByDayOfWeek();


        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(presenceRepository, times(1)).findAll();
    }
}
