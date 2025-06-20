package com.cyna.auth_users.users.service;

import com.cyna.auth_users.users.dto.UpdateUserDto;
import com.cyna.auth_users.users.dto.UserDto;
import com.cyna.auth_users.users.dto.UserMapper;
import com.cyna.auth_users.users.models.ROLE;
import com.cyna.auth_users.users.models.User;
import com.cyna.auth_users.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    // Endpoint ouvrert par gateway pour servir les images
    @Value("${static.location}")
    private String staticLocation;

    @Value("${directory.images}")
    private String imagesPath;

    @Value("${endpoints.password_forgot}")
    private String passwordForgotEndpoint;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final MailerSendService mailerSendService;

    public List<UserDto> getAll() {
        return userRepository.findAll().stream().map(userMapper::UserToUserDto).toList();
    }

    public UserDto getById(Long id) {
        return userMapper.UserToUserDto(userRepository.findById(id).orElse(null));
    }

    public String update(Long id, UpdateUserDto userDto) {
        User user = userRepository.getReferenceById(id);

        User updateUser =  User.builder()
                .id(user.getId())
                .customerId(Optional.ofNullable(userDto.getCustomerId()).orElse(user.getCustomerId()))
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String url = user.getUrlProfile();
        if (url != null) {
            String filename = Path.of(url).getFileName().toString();
            Path profilePath = Path.of(imagesPath, filename);

            if (Files.exists(profilePath)) {
                try {
                    Files.delete(profilePath);
                } catch (IOException e) {
                    log.error("Erreur lors de la suppression de l'image {}", profilePath, e);
                    throw new RuntimeException("Erreur lors de la suppression de l'image", e);
                }
            }
        }

        userRepository.delete(user);
        return "User deleted";
    }

    public List<UserDto> getByName(String name) {
        return userRepository.findByFirstnameContainingIgnoreCaseOrLastnameContainingIgnoreCase(name, name).stream().map(userMapper::UserToUserDto).toList();
    }

    public String passwordForget(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            try {
                mailerSendService.sendEmail(user.getEmail(), passwordForgotEndpoint+"?userId="+user.getId() , "password.forgot");
            } catch (Exception e) {
                throw new RuntimeException("[UserService][PasswordForget]", e);
            }
        });
        return "Operation done!";
    }
}
