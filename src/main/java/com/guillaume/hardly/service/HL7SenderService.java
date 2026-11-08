package com.guillaume.hardly.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.guillaume.hardly.model.MessageHL7;
import com.guillaume.hardly.repository.MessageHL7Repository;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;

@Service
public class HL7SenderService {

    private final HL7ParserService hl7ParserService;
    private final MessageHL7Repository messageHL7Repository;

    public HL7SenderService(HL7ParserService hl7ParserService,
                            MessageHL7Repository messageHL7Repository) {
        this.hl7ParserService = hl7ParserService;
        this.messageHL7Repository = messageHL7Repository;
    }

    HapiContext context = new DefaultHapiContext();
    List<String> listeMessage = new ArrayList<>();

    public List<String> getListeMessage() {
        return listeMessage;
    }

    public String sendMessage(String messageAEnvoyer, String server, int port, boolean useTLS) {

        listeMessage.clear();

        String messageUnitaire = messageAEnvoyer
                .replaceAll("\r\n", System.getProperty("line.separator"))
                .replaceAll("\r", System.getProperty("line.separator"))
                .replaceAll("\n", System.getProperty("line.separator"));

        boolean structCorrecte = hl7ParserService.verifierStructure(messageUnitaire);

        if (!structCorrecte) {
            return "Message HL7 invalide. \r\n Vérifiez qu'il contient :\r\n"
                    + "\t - un segment MSH (entête),\r\n"
                    + "\t - un segment PID (patient),\r\n"
                    + "\t - et éventuellement un segment PV1 (venue).";
        }

        listeMessage.add(messageUnitaire);

        try {
            String responseString = "";
            Connection connection = context.newClient(server, port, useTLS);
            Initiator initiator = connection.getInitiator();

            for (String message : listeMessage) {
                Parser parser = context.getGenericParser();
                Message parsedMessage = parser.parse(message);
                Message response = initiator.sendAndReceive(parsedMessage);
                responseString += "ACK reçu : \r\n" + response.toString() + "\r\n";

                // Persister le message envoyé
                sauvegarderMessageEnvoye(message, server, port);
            }

            connection.close();
            listeMessage.clear();
            return "Acquittement reçu : \r\n" + responseString;

        } catch (HL7Exception | LLPException | IOException e) {
            e.printStackTrace();
            listeMessage.clear();
            return "Erreur d'envoi.";
        }
    }

    // ================================
    // Persister le message envoyé
    // ================================
    private void sauvegarderMessageEnvoye(String contenu, String server, int port) {
        try {
            // Parser le message pour extraire type, événement et résumé
            Message parsedMessage = context.getGenericParser().parse(contenu);

            String type = hl7ParserService.getTypeMessage(parsedMessage);
            String code = hl7ParserService.getCodeEvenement(parsedMessage);
            String resume = hl7ParserService.genererResume(parsedMessage);

            MessageHL7 msg = new MessageHL7();
            msg.setContenu(contenu);
            msg.setDateReception(LocalDateTime.now());
            msg.setDirection(MessageHL7.DIRECTION_ENVOYE);
            msg.setDestination(server + ":" + port);
            msg.setTypeMessage(type);
            msg.setEvenement(code);
            msg.setResume(resume);
            msg.setPatient(null); // pas forcément lié à un patient connu
            messageHL7Repository.save(msg);

        } catch (Exception e) {
            // On ne bloque pas l'envoi si la sauvegarde échoue
            e.printStackTrace();
        }
    }
}