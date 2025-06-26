package com.gestionemployes.employee_management_api.repository;

import com.gestionemployes.employee_management_api.model.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; 

@Repository
public interface DepartementRepository extends JpaRepository<Departement, Long> {
    
    Optional<Departement> findByNom(String nom); 

    boolean existsByNom(String nom); 

}
