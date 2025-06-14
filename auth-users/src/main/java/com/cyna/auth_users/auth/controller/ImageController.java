package com.cyna.users.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ImageController {

    @Value("${directory.images}") // Utilise la même propriété que dans UserService pour le chemin physique
    private String imagesDirectory; // ex: "./uploads/" ou "./images/"

    @GetMapping("/api/v1/user/avatar/{filename}") // C'est cette URL que le frontend va appeler
    public ResponseEntity<Resource> serveAvatar(@PathVariable String filename) {
        try {
            // IMPORTANT : Nettoyer le filename pour éviter les attaques de traversée de répertoire (ex: ../../etc/passwd)
            // normalize() aide, mais une validation supplémentaire pourrait être ajoutée
            String safeFilename = filename.replaceAll("[^a-zA-Z0-9.\\-]", ""); // Garde seulement les caractères sûrs

            // Construisez le chemin complet du fichier sur le système de fichiers du serveur
            Path filePath = Paths.get(imagesDirectory).resolve(safeFilename).normalize();

            File imageFile = filePath.toFile();

            if (imageFile.exists() && imageFile.canRead() && imageFile.isFile()) {
                Resource resource = new FileSystemResource(imageFile);

                String contentType = determineContentType(safeFilename);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                System.err.println("Fichier non trouvé ou non lisible: " + filePath.toString());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du service de l'avatar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Méthode utilitaire pour déterminer le Content-Type basé sur l'extension
    private String determineContentType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        String lowerCaseFilename = filename.toLowerCase();
        if (lowerCaseFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerCaseFilename.endsWith(".jpg") || lowerCaseFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerCaseFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerCaseFilename.endsWith(".webp")) {
            return "image/webp";
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE; // Fallback
    }
}