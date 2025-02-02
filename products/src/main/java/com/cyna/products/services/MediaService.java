package com.cyna.products.services;

import com.cyna.products.models.Media;
import com.cyna.products.repositories.MediaRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
@Slf4j
public class MediaService {
    @Value("${directory.images}")
    private String pathRep;

    @Autowired
    private MediaRepo mediaRepo;

    public Set<Media> uploadFiles(Set<MultipartFile> files) {
        Set<Media> images = new HashSet<>();
        Path directory = Path.of(pathRep);

        // Créer le répertoire s'il n'existe pas
        if (!Files.exists(directory)) {
            try {
                directory = Files.createDirectories(directory);
            } catch (IOException e) {
                log.error("Erreur lors de la création du répertoire: {}", e.getMessage(), e);
                throw new RuntimeException("Impossible de créer le répertoire " + pathRep, e);
            }
        }

        // Traiter chaque fichier
        for (MultipartFile file : files) {
            try {
                // On resoud le chemin du nouveau fichier
                Path destinationFile = directory.resolve(file.getOriginalFilename());

                // Transférer le fichier
                file.transferTo(destinationFile);

                // Créer l'objet Media
                Media media = Media.builder()
                        .name(file.getOriginalFilename())
                        .path(destinationFile.toString().replace("\\","/"))
                        .build();

                mediaRepo.save(media);
                images.add(media);


            } catch (IOException e) {
                log.error("Erreur lors du transfert du fichier: {}", e.getMessage(), e);
                throw new RuntimeException("Erreur lors du transfert du fichier " + file.getOriginalFilename(), e);
            }
        }

        return images;
    }

    public void deleteImages(Set<Media> images) {

        for (Media media : images) {
            File file = new File(media.getPath());
            if (file.delete()) {
                log.info("Deleted file {}", media.getPath());
            }else{
                log.error("Error while deleting file {}", media.getPath());
            }
        }
    }

    public Set<Media> getMediaByIds(Set<Long> imagesId) {
        return mediaRepo.findAllByIdIn(imagesId);
    }
}