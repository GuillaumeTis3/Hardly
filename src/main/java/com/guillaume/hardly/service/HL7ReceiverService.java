package com.guillaume.hardly.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.guillaume.hardly.model.Patient;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;

@Service
public class HL7ReceiverService {
   
    private final HL7MessageProcessorService hl7MessageProcessorService;
    private final HL7ParserService hl7ParserService;

    public HL7ReceiverService(HL7MessageProcessorService hl7MessageProcessorService, 
                              HL7ParserService hl7ParserService) {
        this.hl7MessageProcessorService = hl7MessageProcessorService;
        this.hl7ParserService = hl7ParserService;
    }
    
    HapiContext context = new DefaultHapiContext();
    HL7Service server;
    boolean enEcoute = false;
    int port;

    ReceivingApplication<?> application = new ReceivingApplication<>() {

        @Override
        public boolean canProcess(Message theIn) {
            return true;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Message processMessage(Message theMessage, Map theMetadata)
                throws HL7Exception {
                
            // Vérifier si l'événement est connu
            if (!hl7MessageProcessorService.estEvenementConnu(theMessage)) {
                // Sauvegarder le message inconnu
                hl7MessageProcessorService.saveMessageInconnu(theMessage);
                // Renvoyer un NACK
                return hl7MessageProcessorService.buildNack(theMessage,
                    "Événement non géré : "
                    + hl7ParserService.getTypeMessage(theMessage)
                    + "^"
                    + hl7ParserService.getCodeEvenement(theMessage));
            }
        
            // Événement connu → traitement normal
            Message acquittement = hl7MessageProcessorService.buildAck(theMessage);
            Patient patient = hl7MessageProcessorService.handlePatient(theMessage);
            hl7MessageProcessorService.saveMessage(theMessage, patient);
        
            return acquittement;
        }
    };

    public synchronized void initiateServer(int port, boolean useTLS)
            throws InterruptedException, HL7Exception, ReceivingApplicationException {

        this.port = port;
        server = context.newServer(port, useTLS);
        server.registerApplication("*", "*", application);
        server.start();
        enEcoute = true;
    }

    public String lancerEcouteFonction() {
        if (enEcoute) {
            return "Le serveur est déjà en écoute sur le port " + port + ".";
        }
        try {
            initiateServer(4242, false);
            return "Ecoute sur le port " + port + ".";
        } catch (HL7Exception | InterruptedException | ReceivingApplicationException e) {
            throw new RuntimeException("Erreur d'écoute", e);
        }
    }

    public String arreterEcouteFonction() {
        if (!enEcoute) {
            return "Petit malin, le serveur n'est pas en écoute.";
        }
        try {
            closeServer();
            return "Ecoute terminée.";
        } catch (IOException e) {
            throw new RuntimeException("Erreur de fermeture d'écoute", e);
        }
    }

    public void closeServer() throws IOException {
        server.stop();
        enEcoute = false;
    }

    public boolean getEnEcoute() {
        return enEcoute;
    }

    public int getPort() {
        return port;
    }
}