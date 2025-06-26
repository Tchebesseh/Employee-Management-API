package com.gestionemployes.employee_management_api.repository;

import com.gestionemployes.employee_management_api.model.Employe;

import java.util.List;

import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {
 
 Employe findByEmail(String email);

 boolean existsByEmail(String email);
 
 Page<Employe> findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(String nom, String email, Pageable pageable);

 
 Page<Employe> findByDepartementId(Long departementId, Pageable pageable);

boolean existsByDepartementId(Long id);

List<Employe> findByDepartementId(Long departementId);


}
