package com.hishabpati.hishabpati.controller;

import com.hishabpati.hishabpati.dto.LoginDTO;
import com.hishabpati.hishabpati.dto.UserResponse;
import com.hishabpati.hishabpati.dto.UserSaveDTO;
import com.hishabpati.hishabpati.model.User;
import com.hishabpati.hishabpati.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserSaveDTO dto) {
        if (userService.emailExists(dto.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        User user = userService.register(dto);
        UserResponse response = new UserResponse(user.getId(), user.getName(), user.getEmail());
        return ResponseEntity.created(URI.create("/api/auth/register")).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));
            return ResponseEntity.ok(Map.of("message", "Login successful"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }

}