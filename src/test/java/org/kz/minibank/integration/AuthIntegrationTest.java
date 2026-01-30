package org.kz.minibank.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.kz.minibank.DTO.CreateUserDTO;
import org.kz.minibank.DTO.LoginRequest;
import org.kz.minibank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void shouldRegisterAndLoginUser() throws Exception {
                CreateUserDTO registerRequest = new CreateUserDTO("Adam", "Nowak", "adam@test.com", "securePass123");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists());

                boolean exists = userRepository.findByEmail("adam@test.com").isPresent();
                assert (exists);

                LoginRequest loginRequest = new LoginRequest("adam@test.com", "securePass123");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").isNotEmpty());
        }

        @Test
        void shouldFailLogin_WhenPasswordIsWrong() throws Exception {
                CreateUserDTO registerRequest = new CreateUserDTO("Ewa", "Kowalska", "ewa@test.com", "pass123");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)));

                LoginRequest badLogin = new LoginRequest("ewa@test.com", "WRONG_PASSWORD");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(badLogin)))
                                .andExpect(status().isForbidden());
        }
}