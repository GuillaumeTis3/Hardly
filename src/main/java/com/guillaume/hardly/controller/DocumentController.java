package com.guillaume.hardly.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.guillaume.hardly.model.Document;
import com.guillaume.hardly.model.Role;
import com.guillaume.hardly.model.Utilisateur;
import com.guillaume.hardly.service.DocumentService;
import com.guillaume.hardly.service.PatientService;
import com.guillaume.hardly.service.UtilisateurService;

@Controller
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final PatientService patientService;
    private final UtilisateurService utilisateurService;

    public DocumentController(DocumentService documentService,
                              PatientService patientService,
                              UtilisateurService utilisateurService) {
        this.documentService = documentService;
        this.patientService = patientService;
        this.utilisateurService = utilisateurService;
    }

    // ================================
    // Liste des documents d'un patient
    // GET /documents/{ipp}
    // ================================
    @GetMapping("/{ipp}")
    public String afficherDocuments(@PathVariable String ipp, Model model) {
        patientService.findByIpp(ipp).ifPresentOrElse(
            patient -> {
                model.addAttribute("patient", patient);
                model.addAttribute("documents", documentService.findByPatientIpp(ipp));
            },
            () -> model.addAttribute("erreur", "Patient introuvable pour l'IPP : " + ipp)
        );
        model.addAttribute("ipp", ipp);
        model.addAttribute("medecins", getMedecinsActifs());
        return "liste-documents";
    }

    // ================================
    // Formulaire d'upload
    // GET /documents/upload
    // ================================
    @GetMapping("/upload")
    public String afficherFormulaireUpload(
            @RequestParam(value = "ipp", required = false) String ipp,
            @RequestParam(value = "iep", required = false) String iep,
            Model model) {
        model.addAttribute("ipp", ipp != null ? ipp : "");
        model.addAttribute("iep", iep != null ? iep : "");
        model.addAttribute("medecins", getMedecinsActifs());
        model.addAttribute("utilisateurs", utilisateurService.findAll());
        return "upload-document";
    }

    // ================================
    // Traitement de l'upload
    // POST /documents/upload
    // ================================
    @PostMapping("/upload")
    public String uploadDocument(
            @RequestParam("ipp") String ipp,
            @RequestParam(value = "iep", required = false) String iep,
            @RequestParam("auteurId") Long auteurId,
            @RequestParam("responsableId") Long responsableId,
            @RequestParam("fichier") MultipartFile fichier,
            Model model) {

        // Validation
        if (ipp.isEmpty()) {
            model.addAttribute("erreur", "L'IPP du patient est obligatoire.");
            model.addAttribute("ipp", ipp);
            model.addAttribute("iep", iep);
            model.addAttribute("medecins", getMedecinsActifs());
            model.addAttribute("utilisateurs", utilisateurService.findAll());
            return "upload-document";
        }

        if (fichier.isEmpty()) {
            model.addAttribute("erreur", "Veuillez sélectionner un fichier.");
            model.addAttribute("ipp", ipp);
            model.addAttribute("iep", iep);
            model.addAttribute("medecins", getMedecinsActifs());
            model.addAttribute("utilisateurs", utilisateurService.findAll());
            return "upload-document";
        }

        if (!fichier.getContentType().equals("application/pdf")) {
            model.addAttribute("erreur", "Seuls les fichiers PDF sont acceptés.");
            model.addAttribute("ipp", ipp);
            model.addAttribute("iep", iep);
            model.addAttribute("medecins", getMedecinsActifs());
            model.addAttribute("utilisateurs", utilisateurService.findAll());
            return "upload-document";
        }

        if (fichier.getSize() > 2 * 1024 * 1024) {
            model.addAttribute("erreur", "Le fichier ne doit pas dépasser 2 Mo.");
            model.addAttribute("ipp", ipp);
            model.addAttribute("iep", iep);
            model.addAttribute("medecins", getMedecinsActifs());
            model.addAttribute("utilisateurs", utilisateurService.findAll());
            return "upload-document";
        }

        try {
            documentService.uploadDocument(ipp, iep, auteurId, responsableId, fichier);
            return "redirect:/documents/" + ipp;
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
            model.addAttribute("ipp", ipp);
            model.addAttribute("iep", iep);
            model.addAttribute("medecins", getMedecinsActifs());
            model.addAttribute("utilisateurs", utilisateurService.findAll());
            return "upload-document";
        } catch (IOException e) {
            model.addAttribute("erreur", "Erreur lors de l'upload du fichier.");
            model.addAttribute("ipp", ipp);
            model.addAttribute("iep", iep);
            model.addAttribute("medecins", getMedecinsActifs());
            model.addAttribute("utilisateurs", utilisateurService.findAll());
            return "upload-document";
        }
    }

    // ================================
    // Téléchargement d'un document
    // GET /documents/download/{id}
    // ================================
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        Optional<Document> document = documentService.findById(id);

        if (document.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Document doc = document.get();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + doc.getNom() + "\"")
                .body(doc.getContenu());
    }

    // ================================
    // Modifier le responsable
    // POST /documents/{id}/responsable
    // ================================
    @PostMapping("/{id}/responsable")
    public String modifierResponsable(
            @PathVariable Long id,
            @RequestParam("responsableId") Long responsableId,
            @RequestParam("ipp") String ipp,
            Model model) {

        try {
            documentService.modifierResponsable(id, responsableId);
        } catch (IllegalArgumentException e) {
            model.addAttribute("erreur", e.getMessage());
        }

        return "redirect:/documents/" + ipp;
    }

    // ================================
    // Méthode privée — médecins actifs
    // ================================
    private List<Utilisateur> getMedecinsActifs() {
        return utilisateurService.findByRole(Role.ROLE_MEDECIN)
            .stream()
            .filter(Utilisateur::isActif)
            .toList();
    }
}