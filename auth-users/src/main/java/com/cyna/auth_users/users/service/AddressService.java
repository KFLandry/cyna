package com.cyna.auth_users.users.service;

import com.cyna.auth_users.users.dto.AddressDto;
import com.cyna.auth_users.users.models.Address;
import com.cyna.auth_users.users.models.User;
import com.cyna.auth_users.users.repositories.AddressRepository;
import com.cyna.auth_users.users.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        User user = userRepository.findById(addressDto.getUser_id()).orElseThrow();
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
        Address address = addressRepository.getReferenceById(id);

        // On met a jour uniquement les champs ayant été modifiés
        Address updatedAddress =  Address.builder()
                .id(id)
                .name(Optional.ofNullable(addressDto.getName()).orElse(address.getName()))
                .postcode(Optional.ofNullable(addressDto.getPostcode()!=null? Integer.valueOf(addressDto.getPostcode()) : null).orElse(address.getPostcode()))
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
}
