package com.gestionemployes.employee_management_api.repository;

import com.gestionemployes.employee_management_api.model.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; 

@Repository
public interface PresenceRepository extends JpaRepository<Presence, Long> {
 
 Optional<Presence> findByEmployeIdAndDate(Long employeId, LocalDate date);
 
 List<Presence> findByEmployeIdOrderByDateAsc(Long employeId);

 boolean existsByEmployeId(Long employeId);
}
