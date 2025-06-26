package com.gestionemployes.employee_management_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet; 
import java.util.Objects;
import java.util.Set; 

@Entity
@Table(name = "departements")
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(name = "manager_id") 
    private Long managerId;

    @Column(nullable = false) 
    private BigDecimal budget;

    
    
    
    @OneToMany(mappedBy = "departement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) 
    private Set<Employe> employes = new HashSet<>(); 

    public Departement() {
    }

    public Departement(String nom, Long managerId, BigDecimal budget) {
        this.nom = nom;
        this.managerId = managerId;
        this.budget = budget;
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public Set<Employe> getEmployes() { return employes; }
    public void setEmployes(Set<Employe> employes) { this.employes = employes; }

    
    public void addEmploye(Employe employe) {
        employes.add(employe);
        employe.setDepartement(this);
    }

    public void removeEmploye(Employe employe) {
        employes.remove(employe);
        employe.setDepartement(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Departement that = (Departement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

	@Override
	public String toString() {
		return "Departement [id=" + id + ", nom=" + nom + ", managerId=" + managerId + ", budget=" + budget
				+ ", employes=" + employes + "]";
	}
    
}
