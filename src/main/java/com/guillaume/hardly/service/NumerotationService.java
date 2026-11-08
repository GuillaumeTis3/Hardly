package com.guillaume.hardly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guillaume.hardly.model.Numerotation;
import com.guillaume.hardly.repository.NumerotationRepository;

@Service
public class NumerotationService {

    @Autowired
    private NumerotationRepository numerotationRepository;

    @Transactional
    public String genererNumero(String type) {

        // Récupérer la configuration
        Numerotation numerotation = numerotationRepository.findByType(type)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Aucune configuration de numérotation trouvée pour le type : " + type));

        // Vérifier le dépassement de capacité
        String prefixe  = numerotation.getPrefixe() != null ? numerotation.getPrefixe() : "";
        String suffixe  = numerotation.getSuffixe() != null ? numerotation.getSuffixe() : "";
        int longueurConstantes = prefixe.length() + suffixe.length();
        int longueurNumerique  = numerotation.getLongueur() - longueurConstantes;

        long maxValeur = (long) Math.pow(10, longueurNumerique) - 1;

        if (numerotation.getValeur() > maxValeur) {
            throw new IllegalStateException(
                "La numérotation " + type + " a atteint sa valeur maximale (" + maxValeur + ").");
        }

        // Générer le numéro
        // ex: prefixe="VEN", longueurNumerique=5, valeur=1 → "VEN00001"
        String partieNumerique = String.format("%0" + longueurNumerique + "d", numerotation.getValeur());
        String numero = prefixe + partieNumerique + suffixe;

        // Incrémenter et sauvegarder
        numerotation.setValeur(numerotation.getValeur() + 1);
        numerotationRepository.save(numerotation);

        return numero;
    }

    public String consulterProchainNumero(String type) {
        Numerotation numerotation = numerotationRepository.findByType(type)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Aucune configuration trouvée pour le type : " + type));

        String prefixe = numerotation.getPrefixe() != null ? numerotation.getPrefixe() : "";
        String suffixe = numerotation.getSuffixe() != null ? numerotation.getSuffixe() : "";
        int longueurConstantes = prefixe.length() + suffixe.length();
        int longueurNumerique  = numerotation.getLongueur() - longueurConstantes;

        String partieNumerique = String.format("%0" + longueurNumerique + "d", numerotation.getValeur());
        return prefixe + partieNumerique + suffixe;
    }

    // Nouveau — décrémente si l'utilisateur abandonne le formulaire
    @Transactional
    public void annulerReservation(String type) {
        Numerotation numerotation = numerotationRepository.findByType(type)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Aucune configuration trouvée pour le type : " + type));
                
        if (numerotation.getValeur() > 1) {
            numerotation.setValeur(numerotation.getValeur() - 1);
            numerotationRepository.save(numerotation);
        }
    }
}