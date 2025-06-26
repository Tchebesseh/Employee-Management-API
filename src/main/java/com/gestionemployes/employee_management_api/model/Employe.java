package com.gestionemployes.employee_management_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set; 

@Entity
@Table(name = "employes", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement; 

    @Column(nullable = false)
    private BigDecimal salaire;

    @Column(name = "date_embauche", nullable = false)
    private LocalDate dateEmbauche;

    @Column(nullable = false)
    private String statut;

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY) 
    private Set<Presence> presences;

    public Employe() {
    }

    public Employe(String prenom, String nom, String email, Departement departement, BigDecimal salaire, LocalDate dateEmbauche, String statut) {
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.departement = departement;
        this.salaire = salaire;
        this.dateEmbauche = dateEmbauche;
        this.statut = statut;
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }

    public BigDecimal getSalaire() { return salaire; }
    public void setSalaire(BigDecimal salaire) { this.salaire = salaire; }

    public LocalDate getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(LocalDate dateEmbauche) { this.dateEmbauche = dateEmbauche; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Set<Presence> getPresences() { return presences; }
    public void setPresences(Set<Presence> presences) { this.presences = presences; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employe employe = (Employe) o;
        return Objects.equals(id, employe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Employe{" +
                "id=" + id +
                ", prenom='" + prenom + '\'' +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", departementId=" + (departement != null ? departement.getId() : "null") +
                ", salaire=" + salaire +
                ", dateEmbauche=" + dateEmbauche +
                ", statut='" + statut + '\'' +
                '}';
    }
}
