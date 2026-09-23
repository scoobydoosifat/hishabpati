package com.hishabpati.hishabpati.service;

import com.hishabpati.hishabpati.dto.UserSaveDTO;
import com.hishabpati.hishabpati.model.User;
import com.hishabpati.hishabpati.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerEncodesPasswordAndSavesUser() {
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        User saved = User.builder()
                .id("u1")
                .name("Sifat")
                .email("sifat@example.com")
                .password("encoded-secret")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.register(new UserSaveDTO("Sifat", "sifat@example.com", "secret"));

        assertNotNull(result);
        assertEquals("u1", result.getId());
        assertEquals("sifat@example.com", result.getEmail());
        verify(passwordEncoder).encode("secret");
        verify(userRepository).save(argThat(user -> "encoded-secret".equals(user.getPassword())));
    }

    @Test
    void registerLowercasesEmail() {
        when(passwordEncoder.encode(any())).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(new UserSaveDTO("Sifat", "Sifat@Example.com", "secret"));

        assertEquals("sifat@example.com", result.getEmail());
    }

    @Test
    void emailExistsDelegatesToRepository() {
        when(userRepository.existsByEmail("sifat@example.com")).thenReturn(true);

        assertTrue(userService.emailExists("sifat@example.com"));
    }

    @Test
    void findByEmailReturnsUserWhenFound() {
        User user = User.builder().id("u1").email("sifat@example.com").build();
        when(userRepository.findByEmail("sifat@example.com")).thenReturn(Optional.of(user));

        assertEquals(user, userService.findByEmail("sifat@example.com"));
    }

    @Test
    void findByEmailReturnsNullWhenNotFound() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertNull(userService.findByEmail("nobody@example.com"));
    }

}