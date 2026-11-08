package com.guillaume.hardly.service;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.guillaume.hardly.model.HL7EvenementType;
import com.guillaume.hardly.model.MessageHL7;
import com.guillaume.hardly.model.Patient;
import com.guillaume.hardly.repository.MessageHL7Repository;

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

@Service
public class HL7MessageProcessorService {

    private final PatientService patientService;
    private final MessageHL7Repository messageHL7Repository;
    private final HL7ParserService hl7ParserService;

    public HL7MessageProcessorService(PatientService patientService,
                                      MessageHL7Repository messageHL7Repository,
                                      HL7ParserService hl7ParserService) {
        this.patientService = patientService;
        this.messageHL7Repository = messageHL7Repository;
        this.hl7ParserService = hl7ParserService;
    }

    // ================================
    // Générer ACK positif
    // ================================
    public Message buildAck(Message message) {
        try {
            return message.generateACK();
        } catch (IOException | HL7Exception e) {
            throw new RuntimeException("Erreur ACK HL7", e);
        }
    }

    // ================================
    // Générer NACK — événement non géré
    // ================================
    public Message buildNack(Message message, String raison) {
        try {
            return message.generateACK(
                AcknowledgmentCode.AE,
                new HL7Exception(raison)
            );
        } catch (IOException | HL7Exception e) {
            throw new RuntimeException("Erreur NACK HL7", e);
        }
    }

    // ================================
    // Vérifier si l'événement est connu
    // ================================
    public boolean estEvenementConnu(Message message) {
        String type = hl7ParserService.getTypeMessage(message);
        String code = hl7ParserService.getCodeEvenement(message);
        return HL7EvenementType.estConnu(type, code);
    }

    // ================================
    // Traiter le patient
    // ================================
    public Patient handlePatient(Message message) {
        return patientService.addPatient(message);
    }

    // ================================
    // Sauvegarder le message reçu
    // ================================
    public void saveMessage(Message messageRecu, Patient patient) {
        try {
            String type = hl7ParserService.getTypeMessage(messageRecu);
            String code = hl7ParserService.getCodeEvenement(messageRecu);
            String resume = hl7ParserService.genererResume(messageRecu);

            MessageHL7 msg = new MessageHL7();
            msg.setContenu(messageRecu.encode());
            msg.setDateReception(LocalDateTime.now());
            msg.setDirection(MessageHL7.DIRECTION_RECU);
            msg.setDestination(null);
            msg.setTypeMessage(type);
            msg.setEvenement(code);
            msg.setResume(resume);
            msg.setPatient(patient);
            messageHL7Repository.save(msg);
        } catch (HL7Exception e) {
            throw new RuntimeException("Erreur stockage message", e);
        }
    }

    // ================================
    // Sauvegarder un message non géré
    // ================================
    public void saveMessageInconnu(Message messageRecu) {
        try {
            String type = hl7ParserService.getTypeMessage(messageRecu);
            String code = hl7ParserService.getCodeEvenement(messageRecu);

            MessageHL7 msg = new MessageHL7();
            msg.setContenu(messageRecu.encode());
            msg.setDateReception(LocalDateTime.now());
            msg.setDirection(MessageHL7.DIRECTION_RECU);
            msg.setDestination(null);
            msg.setTypeMessage(type);
            msg.setEvenement(code);
            msg.setResume("[NON GÉRÉ] " + type + "^" + code);
            msg.setPatient(null);
            messageHL7Repository.save(msg);
        } catch (HL7Exception e) {
            throw new RuntimeException("Erreur stockage message inconnu", e);
        }
    }
}