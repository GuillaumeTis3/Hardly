package com.guillaume.hardly.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "MESSAGE_HL7")
public class MessageHL7 {

    // Constantes de direction
    public static final String DIRECTION_RECU = "RECU";
    public static final String DIRECTION_ENVOYE = "ENVOYE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MSG_ID")
    private Long id;

    @Column(name = "MSG_CONTENU", length = 4000)
    private String contenu;

    @Column(name = "MSG_DATE_RECEPTION")
    private LocalDateTime dateReception;

    @Column(name = "MSG_DIRECTION")
    private String direction;

    @Column(name = "MSG_DESTINATION")
    private String destination;

    @Column(name = "MSG_RESUME", length = 500)
    private String resume;

    @Column(name = "MSG_TYPE")
    private String typeMessage; // ex: "ADT"

    @Column(name = "MSG_EVENEMENT")
    private String evenement; // ex: "A28"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAT_ID")
    private Patient patient;

    public Long getId() { return id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateReception() { return dateReception; }
    public void setDateReception(LocalDateTime dateReception) { this.dateReception = dateReception; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }

    public String getTypeMessage() { return typeMessage; }
    public void setTypeMessage(String typeMessage) { this.typeMessage = typeMessage; }

    public String getEvenement() { return evenement; }
    public void setEvenement(String evenement) { this.evenement = evenement; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
}