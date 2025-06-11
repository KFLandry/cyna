package com.cyna.auth_users.users.service;

import com.cyna.auth_users.users.dto.AddressDto;
import com.cyna.auth_users.users.models.Address;
import com.cyna.auth_users.users.models.User;
import com.cyna.auth_users.users.repositories.AddressRepository;
import com.cyna.auth_users.users.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<Address> getAll() {
        return addressRepository.findAll();
    }

    public Address get(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Adresse introuvable"));
    }

    public List<Address> getByUserId(Long userId) {
        // vérifie que l'utilisateur existe
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable id=" + userId));
        // retourne ses adresses
        return addressRepository.findByUserId(userId);
    }

    public String create(@Valid AddressDto addressDto) {
        // 1) on exige un userId
        Long uid = addressDto.getUserId();
        if (uid == null) {
            throw new IllegalArgumentException("Le champ userId est obligatoire");
        }

        // 2) on récupère l’utilisateur UNIQUEMENT par son ID
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable pour id=" + uid));

        // 3) on construit et on sauve l’adresse
        Address address = Address.builder()
                .user(user)
                .name(addressDto.getName())
                .postcode(Integer.valueOf(addressDto.getPostcode()))
                .city(addressDto.getCity())
                .country(addressDto.getCountry())
                .url(addressDto.getUrl())
                .build();
        addressRepository.save(address);

        return "Operation successful";
    }

    public String update(Long id, @Valid AddressDto addressDto) {
        // on récupère l’existant
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Adresse introuvable pour id=" + id));

        // on met à jour uniquement les champs modifiés, en gardant le même user
        Address updated = Address.builder()
                .id(id)
                .user(existing.getUser())
                .name(Optional.ofNullable(addressDto.getName()).orElse(existing.getName()))
                .postcode(Optional.ofNullable(addressDto.getPostcode())
                        .map(Integer::valueOf)
                        .orElse(existing.getPostcode()))
                .city(Optional.ofNullable(addressDto.getCity()).orElse(existing.getCity()))
                .country(Optional.ofNullable(addressDto.getCountry()).orElse(existing.getCountry()))
                .url(Optional.ofNullable(addressDto.getUrl()).orElse(existing.getUrl()))
                .build();

        addressRepository.save(updated);
        return "Operation successful";
    }

    public String delete(Long id) {
        addressRepository.deleteById(id);
        return "Operation successful";
    }
}
