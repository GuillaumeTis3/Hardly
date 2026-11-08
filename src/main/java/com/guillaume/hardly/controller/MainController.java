package com.guillaume.hardly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Remplace quiSuisJeFonction()
    @GetMapping("/apropos")
    public String apropos(Model model) {
        model.addAttribute("message", """
                                      Bonjour, je m'appelle Guillaume Gaspard, dipl\u00f4mé TIS en 2017, je suis chef de projet Interopérabilité.
                                      Pour mon stage de fin d'études, je me suis lancé dans l'interopérabilité... Et quelle découverte ! Aujourd'hui j'en suis passionné.  
                                      Ps : N'hésitez pas \u00e0 me demander les sources ... mais je suis tr\u00e8s mauvais développeur.""");
        return "index";
    }

    // Remplace helpFonction()
    @GetMapping("/aide")
    public String aide(Model model) {
        model.addAttribute("message", """
                                    Voici quelques liens intéressants sur le HL7 :
                                        1) Caristix : https://hl7-definition.caristix.com/v2/HL7v2.5.1
                                        2) HAPI HL7 : https://hapifhir.github.io/hapi-hl7v2/""");
        return "index";
    }

    // Route vers la page de login
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}