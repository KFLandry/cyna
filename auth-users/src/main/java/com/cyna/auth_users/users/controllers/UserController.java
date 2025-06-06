package com.cyna.auth_users.users.controllers;

import com.cyna.auth_users.users.dto.UpdateUserDto;
import com.cyna.auth_users.users.dto.UserDto;
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
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> search(@RequestParam String name) {
        return ResponseEntity.ok(userService.getByName(name));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id ,@Valid @RequestBody UpdateUserDto userDto) {
        return ResponseEntity.ok(userService.update(id ,userDto));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<String> updatePassword(@PathVariable Long id, @Valid @RequestBody UpdateUserDto userDto) {
        return ResponseEntity.ok(userService.update(id, userDto));
    }

    @PatchMapping("/{id}/profiles")
    public ResponseEntity<String> uploadProfile(@PathVariable Long id , @Valid @ModelAttribute UpdateUserDto userDto) {
        return ResponseEntity.ok(userService.update(id ,userDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return ResponseEntity.ok(userService.delete(id));
    }
}