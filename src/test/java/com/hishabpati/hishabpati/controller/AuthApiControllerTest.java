package com.hishabpati.hishabpati.controller;

import com.hishabpati.hishabpati.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        JsonMapper mapper = JsonMapper.builder().build();
        AuthApiController controller = new AuthApiController(userService, authenticationManager);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new JacksonJsonHttpMessageConverter(mapper))
                .build();
    }

    @Test
    void registerCreatesNewUser() throws Exception {
        when(userService.emailExists("sifat@example.com")).thenReturn(false);
        when(userService.register(any())).thenReturn(
                com.hishabpati.hishabpati.model.User.builder()
                        .id("u1").name("Sifat").email("sifat@example.com").build());

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"name\":\"Sifat\",\"email\":\"sifat@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("u1"))
                .andExpect(jsonPath("$.name").value("Sifat"))
                .andExpect(jsonPath("$.email").value("sifat@example.com"));
    }

    @Test
    void registerReturnsBadRequestWhenEmailIsTaken() throws Exception {
        when(userService.emailExists("sifat@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"name\":\"Sifat\",\"email\":\"sifat@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void loginReturnsSuccessForValidCredentials() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("sifat@example.com", "secret"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"sifat@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"sifat@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

}