package com.hishabpati.hishabpati.service;

import com.hishabpati.hishabpati.dto.UserSaveDTO;
import com.hishabpati.hishabpati.model.User;
import com.hishabpati.hishabpati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(UserSaveDTO dto) {
        User user = User.builder()
                .name(dto.name())
                .email(dto.email().toLowerCase())
                .password(passwordEncoder.encode(dto.password()))
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.toLowerCase());
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase()).orElse(null);
    }

}