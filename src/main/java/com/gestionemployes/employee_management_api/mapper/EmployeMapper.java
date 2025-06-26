package com.gestionemployes.employee_management_api.mapper;

import com.gestionemployes.employee_management_api.dto.EmployeDto;
import com.gestionemployes.employee_management_api.dto.EmployeRequest;
import com.gestionemployes.employee_management_api.model.Departement;
import com.gestionemployes.employee_management_api.model.Employe;
import org.springframework.stereotype.Component;

@Component
public class EmployeMapper {

    public Employe toEntity(EmployeRequest employeRequest, Departement departement) {
        Employe employe = new Employe();
        employe.setPrenom(employeRequest.getPrenom());
        employe.setNom(employeRequest.getNom());
        employe.setEmail(employeRequest.getEmail());
        employe.setDepartement(departement);
        employe.setSalaire(employeRequest.getSalaire());
        employe.setDateEmbauche(employeRequest.getDateEmbauche());
        employe.setStatut(employeRequest.getStatut());
        return employe;
    }

    public EmployeDto toDto(Employe employe) {
        Long departementId = (employe.getDepartement() != null && employe.getDepartement().getId() != null)
                              ? employe.getDepartement().getId()
                              : null;

        return new EmployeDto(
                employe.getId(),
                employe.getPrenom(),
                employe.getNom(),
                employe.getEmail(),
                departementId,
                employe.getSalaire(),
                employe.getDateEmbauche(),
                employe.getStatut()
        );
    }
}
