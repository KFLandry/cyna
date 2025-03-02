package com.cyna.auth_users.users.service;

import com.cyna.auth_users.users.dto.UserDto;
import com.cyna.auth_users.users.models.ROLE;
import com.cyna.auth_users.users.models.User;
import com.cyna.auth_users.users.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    // Endpoint ouvrert par gateway pour servir les images
    @Value("${static.location}")
    private String staticLocation;

    @Value("${directory.images}")
    private String imagesPath;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public String update(Long id, UserDto userDto) {
        User user = userRepository.getReferenceById(id);

        User updateUser =  User.builder()
                .id(user.getId())
                .firstname(Optional.ofNullable(userDto.getFirstname()).orElse(user.getFirstname()))
                .lastname(Optional.ofNullable(userDto.getLastname()).orElse(user.getLastname()))
                .email(Optional.ofNullable(userDto.getEmail()).orElse(user.getEmail()))
                .phone(Optional.ofNullable(userDto.getPhone()).orElse(user.getPhone()))
                .enabled(Optional.ofNullable(userDto.getEnabled()).orElse(user.getEnabled()))
                .password(Optional.ofNullable( ( userDto.getPassword()!=null ) ? passwordEncoder.encode(userDto.getPassword()) : null).orElse(user.getPassword()))
                .urlProfile(Optional.ofNullable(userDto.getProfile()!=null ? this.uploadProfile(userDto.getProfile()) : null ).orElse(user.getUrlProfile()))
                .emailVerified(user.getEmailVerified())
                .roles(Optional.ofNullable( userDto.getRole()!=null ? ROLE.valueOf(userDto.getRole()) : null).orElse(user.getRoles()))
                .build();

        userRepository.save(updateUser);
        return "User updated";
    }

    public String uploadProfile(MultipartFile file) {
        Path directory = Path.of(imagesPath);

        // Créer le répertoire s'il n'existe pas
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                log.error("Erreur lors de la création du répertoire: {}", e.getMessage(), e);
                throw new RuntimeException("Impossible de créer le répertoire " + imagesPath, e);
            }
        }

        try {
            // On resoud le chemin du nouveau fichier
            Path destinationFile = directory.resolve(file.getOriginalFilename());

            // Transférer le fichier de tmp vers directory
            file.transferTo(destinationFile);

            return staticLocation+"/"+file.getOriginalFilename();
        } catch (IOException e) {
            log.error("Erreur lors du transfert du fichier: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du transfert du fichier " + file.getOriginalFilename(), e);
        }
    }

    public String delete(Long id) {
        User user = userRepository.getReferenceById(id);
        Path profilePath = Path.of(user.getUrlProfile());
        if (Files.exists(profilePath)) {
            try {
                Files.delete(profilePath);
            } catch (IOException e) {
                log.error("Erreur lors de la suppression de l'image {}", profilePath, e);
                throw new RuntimeException(e);
            }
        }
        userRepository.delete(user);
        return "User deleted";
    }

    public List<User> getByName(String name) {
//        return userRepository.findByfirstnameOrlastname(name, name);*
        return null;
    }
}
