package com.gestionemployes.employee_management_api.mapper;

import com.gestionemployes.employee_management_api.dto.DepartementDto;
import com.gestionemployes.employee_management_api.dto.DepartementRequest;
import com.gestionemployes.employee_management_api.model.Departement;
import org.springframework.stereotype.Component;

@Component
public class DepartementMapper {

    public Departement toEntity(DepartementRequest request) {
        Departement departement = new Departement();
        departement.setNom(request.getNom());
        departement.setManagerId(request.getManagerId());
        departement.setBudget(request.getBudget());
        return departement;
    }

    public DepartementDto toDto(Departement departement) {
        return new DepartementDto(
            departement.getId(),
            departement.getNom(),
            departement.getManagerId(),
            departement.getBudget()
        );
    }
}
