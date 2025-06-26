package com.gestionemployes.employee_management_api.model;


import jakarta.persistence.*;
import java.time.LocalDate; 
import java.time.LocalTime; 
import java.time.Duration; 

@Entity 
@Table(name = "presences") 
public class Presence {

 @Id 
 @GeneratedValue(strategy = GenerationType.IDENTITY) 
 private Long id;

 
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "employe_id", nullable = false) 
 private Employe employe;

 @Column(nullable = false) 
 private LocalDate date;

 @Column(nullable = false) 
 private LocalTime arrivee;

 @Column 
 private LocalTime depart;

 @Column(name = "heures_travaillees") 
 private Long heuresTravaillees; 
                                 

 
 public Presence() {
 }

 public Presence(Employe employe, LocalDate date, LocalTime arrivee) {
     this.employe = employe;
     this.date = date;
     this.arrivee = arrivee;
     
 }

 
 public Long getId() {
     return id;
 }

 public void setId(Long id) {
     this.id = id;
 }

 public Employe getEmploye() {
     return employe;
 }

 public void setEmploye(Employe employe) {
     this.employe = employe;
 }

 public LocalDate getDate() {
     return date;
 }

 public void setDate(LocalDate date) {
     this.date = date;
 }

 public LocalTime getArrivee() {
     return arrivee;
 }

 public void setArrivee(LocalTime arrivee) {
     this.arrivee = arrivee;
 }

 public LocalTime getDepart() {
     return depart;
 }

 public void setDepart(LocalTime depart) {
     this.depart = depart;
     
     calculateHeuresTravaillees();
 }

 public Long getHeuresTravaillees() {
     return heuresTravaillees;
 }

 
 private void setHeuresTravaillees(Long heuresTravaillees) {
     this.heuresTravaillees = heuresTravaillees;
 }

 
 
//Méthode pour calculer les heures travaillées
 @PrePersist @PreUpdate // Assure que le calcul est fait avant la persistance ou la mise à jour
 public void calculateHeuresTravaillees() {
     if (this.arrivee != null && this.depart != null) {
         this.heuresTravaillees = Duration.between(this.arrivee, this.depart).toMinutes();
     } else {
         this.heuresTravaillees = null; // Ou 0, selon votre logique métier
     }
 }

 @Override
 public String toString() {
     return "Presence{" +
            "id=" + id +
            ", employeId=" + (employe != null ? employe.getId() : "null") +
            ", date=" + date +
            ", arrivee=" + arrivee +
            ", depart=" + depart +
            ", heuresTravaillees=" + heuresTravaillees +
            '}';
 }
}
