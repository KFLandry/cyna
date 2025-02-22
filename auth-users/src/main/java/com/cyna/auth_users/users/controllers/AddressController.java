package com.cyna.auth_users.users.controllers;

import com.cyna.auth_users.users.dto.AddressDto;
import com.cyna.auth_users.users.dto.CardDto;
import com.cyna.auth_users.users.models.Address;
import com.cyna.auth_users.users.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/address")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<Address>> getAll() {
        return ResponseEntity.ok(addressService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> get(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.get(id));
    }

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody AddressDto addressDto) {
        return ResponseEntity.ok(addressService.create(addressDto));
    }

    @PatchMapping("{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @Valid @RequestBody AddressDto addressDto) {
        return ResponseEntity.ok(addressService.update(id, addressDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.delete(id));
    }
}
