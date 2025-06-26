package com.gestionemployes.employee_management_api.mapper;

import com.gestionemployes.employee_management_api.dto.PresenceDto;
import com.gestionemployes.employee_management_api.model.Presence;
import org.springframework.stereotype.Component;

@Component
public class PresenceMapper {

    public PresenceDto toDto(Presence presence) {
        if (presence == null) {
            return null;
        }

        // Conversion des minutes en format "Xh Ym"
        String formattedHeuresTravaillees = null;
        if (presence.getHeuresTravaillees() != null) {
            long totalMinutes = presence.getHeuresTravaillees();
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            formattedHeuresTravaillees = String.format("%dh %02dm", hours, minutes);
        }

        return new PresenceDto(
            presence.getId(),
            presence.getEmploye() != null ? presence.getEmploye().getId() : null, // ID de l'employé
            presence.getDate(),
            presence.getArrivee(),
            presence.getDepart(),
            formattedHeuresTravaillees // Utilise la valeur formatée
        );
    }
}
