package com.cyna.auth_users.users.controllers;

import com.cyna.auth_users.users.dto.CardDto;
import com.cyna.auth_users.users.models.BankCard;
import com.cyna.auth_users.users.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/card")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @GetMapping
    public ResponseEntity<List<BankCard>> getAll() {
        return ResponseEntity.ok(cardService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankCard> get(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.get(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CardDto CardDto) {
        return ResponseEntity.ok(cardService.create(CardDto));
    }

    @PatchMapping("{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CardDto CardDto) {
        return ResponseEntity.ok(cardService.update(id, CardDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.delete(id));
    }
}
