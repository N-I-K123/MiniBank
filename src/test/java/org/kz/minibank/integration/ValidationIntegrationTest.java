package org.kz.minibank.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.kz.minibank.DTO.CreateTransactionRequest;
import org.kz.minibank.DTO.CreateUserDTO;
import org.kz.minibank.DTO.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldFailWhenAmountIsNegative() throws Exception {
        String token = registerAndLogin("validator@test.com", "pass123");

        CreateTransactionRequest request = new CreateTransactionRequest(
                "SRC1", "TGT1", new BigDecimal("-100.00"), "Bad Transfer");

        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenTitleIsMissing() throws Exception {
        String token = registerAndLogin("validator2@test.com", "pass123");

        CreateTransactionRequest request = new CreateTransactionRequest(
                "SRC1", "TGT1", new BigDecimal("100.00"), null);

        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenAmountHasReducedPrecisionRequirementsButIsInvalidFormat() throws Exception {
        // DTO says @Digits(integer = 10, fraction = 2)
        String token = registerAndLogin("validator3@test.com", "pass123");

        CreateTransactionRequest request = new CreateTransactionRequest(
                "SRC1", "TGT1", new BigDecimal("100.123"), "Precision Fail");

        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private String registerAndLogin(String email, String password) throws Exception {
        CreateUserDTO registerRequest = new CreateUserDTO("Test", "User", email, password);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Map responseMap = objectMapper.readValue(responseContent, Map.class);
        return (String) responseMap.get("token");
    }
}
