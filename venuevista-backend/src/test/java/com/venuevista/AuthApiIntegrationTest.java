package com.venuevista;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.venuevista.dto.LoginRequest;
import com.venuevista.dto.RegisterRequest;
import com.venuevista.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegistrationSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Alice Smith",
                "alice@example.com",
                "9876543210",
                "Password123",
                "Delhi",
                "Concerts",
                2,
                null
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("alice@example.com"));
    }

    @Test
    void testRegistrationDuplicateEmailReturns409() throws Exception {
        RegisterRequest request1 = new RegisterRequest("Alice", "alice@example.com", "9876543210", "Password123", null, null, null, null);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        RegisterRequest request2 = new RegisterRequest("Alice Dup", "Alice@Example.com", "9876543211", "Password123", null, null, null, null);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_EXISTS"));
    }

    @Test
    void testRegistrationWeakPasswordReturns400() throws Exception {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "9876543210", "weak", null, null, null, null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void testLoginSuccessAndInvalidCredentials() throws Exception {
        RegisterRequest reg = new RegisterRequest("Bob", "bob@example.com", "9876543210", "Password123", null, null, null, null);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        // Valid login
        LoginRequest validLogin = new LoginRequest("bob@example.com", "Password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        // Invalid password
        LoginRequest invalidLogin = new LoginRequest("bob@example.com", "WrongPassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }
}
