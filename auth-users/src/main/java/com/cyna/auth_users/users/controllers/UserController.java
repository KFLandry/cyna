package com.cyna.auth_users.users.controllers;

import com.cyna.auth_users.users.dto.UserDto;
import com.cyna.auth_users.users.models.User;
import com.cyna.auth_users.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> search(@RequestParam String name) {
        return ResponseEntity.ok(userService.getByName(name));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id ,@Valid @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.update(id ,userDto));
    }

    @PatchMapping("/{id}/profiles")
    public ResponseEntity<String> uploadProfile(@PathVariable Long id ,@Valid @ModelAttribute UserDto userDto) {
        return ResponseEntity.ok(userService.update(id ,userDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return ResponseEntity.ok(userService.delete(id));
    }
}
