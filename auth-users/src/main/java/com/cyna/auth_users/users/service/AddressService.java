package com.cyna.auth_users.users.service;

import com.cyna.auth_users.users.dto.AddressDto;
import com.cyna.auth_users.users.models.Address;
import com.cyna.auth_users.users.models.User;
import com.cyna.auth_users.users.repositories.AddressRepository;
import com.cyna.auth_users.users.repositories.UserRepository;
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
        return addressRepository.getReferenceById(id);
    }

    public String create(@Valid AddressDto addressDto) {
        User user = userRepository
                .findByIdOrCustomerId(addressDto.getUserId(), addressDto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Integer postcode = validateAndParsePostcode(addressDto.getPostcode());

        Address address = Address.builder()
                .user(user)
                .name(addressDto.getName())
                .postcode(postcode)
                .city(addressDto.getCity())
                .country(addressDto.getCountry())
                .url(addressDto.getUrl())
                .build();

        addressRepository.save(address);
        return "Operation successful";
    }

    public String update(Long id, @Valid AddressDto addressDto) {
        Address address = addressRepository.getReferenceById(id);

        Integer postcode = address.getPostcode();
        if (addressDto.getPostcode() != null) {
            postcode = validateAndParsePostcode(addressDto.getPostcode());
        }

        Address updatedAddress = Address.builder()
                .id(id)
                .user(address.getUser()) // ← essentiel
                .name(Optional.ofNullable(addressDto.getName()).orElse(address.getName()))
                .postcode(postcode)
                .city(Optional.ofNullable(addressDto.getCity()).orElse(address.getCity()))
                .country(Optional.ofNullable(addressDto.getCountry()).orElse(address.getCountry()))
                .url(Optional.ofNullable(addressDto.getUrl()).orElse(address.getUrl()))
                .build();

        addressRepository.save(updatedAddress);
        return "Operation successful";
    }

    public String delete(Long id) {
        addressRepository.deleteById(id);
        return "Operation successful";
    }

    private Integer validateAndParsePostcode(String postcodeStr) {
        try {
            Integer cp = Integer.parseInt(postcodeStr);
            if (cp < 1000 || cp > 99999) throw new NumberFormatException();
            return cp;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Code postal invalide (format numérique 5 chiffres requis)");
        }
    }
}
