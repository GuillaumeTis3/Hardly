package com.guillaume.hardly.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.guillaume.hardly.model.MessageHL7;
import com.guillaume.hardly.repository.MessageHL7Repository;
import com.guillaume.hardly.service.HL7SenderService;

@Controller
@RequestMapping("/historique")
public class HistoriqueController {

    private final MessageHL7Repository messageHL7Repository;
    private final HL7SenderService hl7SenderService;

    public HistoriqueController(MessageHL7Repository messageHL7Repository,
                                HL7SenderService hl7SenderService) {
        this.messageHL7Repository = messageHL7Repository;
        this.hl7SenderService = hl7SenderService;
    }

    // ================================
    // Liste de tous les messages
    // GET /historique
    // ================================
    @GetMapping
    public String afficherHistorique(
            @RequestParam(value = "direction", required = false) String direction,
            Model model) {

        if (direction != null && !direction.isEmpty()) {
            // Filtrer par direction
            model.addAttribute("messages",
                messageHL7Repository.findByDirectionOrderByDateReceptionDesc(direction));
            model.addAttribute("filtreActif", direction);
        } else {
            // Tous les messages
            model.addAttribute("messages",
                messageHL7Repository.findAllByOrderByDateReceptionDesc());
            model.addAttribute("filtreActif", "TOUS");
        }

        return "historique-messages";
    }

    // ================================
    // Détail d'un message
    // GET /historique/{id}
    // ================================
    @GetMapping("/{id}")
    public String afficherDetailMessage(@PathVariable Long id, Model model) {

        Optional<MessageHL7> message = messageHL7Repository.findById(id);

        if (message.isEmpty()) {
            model.addAttribute("erreur", "Message introuvable.");
            return "historique-messages";
        }

        model.addAttribute("message", message.get());
        return "detail-message";
    }

    // ================================
    // Renvoi d'un message
    // POST /historique/{id}/renvoyer
    // ================================
    @PostMapping("/{id}/renvoyer")
    public String renvoyerMessage(
            @PathVariable Long id,
            @RequestParam("serveur") String serveur,
            @RequestParam("port") int port,
            Model model) {

        Optional<MessageHL7> message = messageHL7Repository.findById(id);

        if (message.isEmpty()) {
            model.addAttribute("erreur", "Message introuvable.");
            return "detail-message";
        }

        String resultat = hl7SenderService.sendMessage(
            message.get().getContenu(), serveur, port, false
        );

        model.addAttribute("message", message.get());
        model.addAttribute("resultatRenvoi", resultat);
        return "detail-message";
    }
}