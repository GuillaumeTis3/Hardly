package com.guillaume.hardly.controller;

import com.guillaume.hardly.service.HL7ReceiverService;
import com.guillaume.hardly.service.HL7SenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hl7")
public class HL7Controller {

    @Autowired
    private HL7ReceiverService hl7ReceiverService;

    @Autowired
    private HL7SenderService hl7SenderService;

    // ================================
    // Lancer l'écoute MLLP
    // GET /hl7/lancer
    // ================================
    @GetMapping("/lancer")
    public String lancerEcoute(Model model) {
        String result = hl7ReceiverService.lancerEcouteFonction();
        model.addAttribute("message", result);
        return "index";
    }

    // ================================
    // Arrêter l'écoute MLLP
    // GET /hl7/arreter
    // ================================
    @GetMapping("/arreter")
    public String arreterEcoute(Model model) {
        String result = hl7ReceiverService.arreterEcouteFonction();
        model.addAttribute("message", result);
        return "index";
    }

    // ================================
    // Afficher le formulaire d'envoi
    // GET /hl7/envoyer
    // ================================
    @GetMapping("/envoyer")
    public String afficherFormulaireEnvoi() {
        return "envoyer-hl7";
    }

    // ================================
    // Traiter l'envoi du message HL7
    // POST /hl7/envoyer
    // ================================
    @PostMapping("/envoyer")
    public String envoyerMessage(
            @RequestParam("message") String message,
            @RequestParam("serveur") String serveur,
            @RequestParam("port") int port,
            Model model) {

        String result = hl7SenderService.sendMessage(message, serveur, port, false);
        model.addAttribute("message", result);
        return "index";
    }
}