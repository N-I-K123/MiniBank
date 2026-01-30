package org.kz.minibank.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.kz.minibank.DTO.AuthResponse;
import org.kz.minibank.DTO.CreateTransactionRequest;
import org.kz.minibank.DTO.CreateUserDTO;
import org.kz.minibank.DTO.LoginRequest;
import org.kz.minibank.model.Account;
import org.kz.minibank.model.User;
import org.kz.minibank.repository.AccountRepository;
import org.kz.minibank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TransferIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldLoginAndPerformTransferSuccessfully() throws Exception {
        String senderEmail = "sender@test.com";
        String token = registerAndLogin(senderEmail, "pass123");

        User sender = userRepository.findByEmail(senderEmail).orElseThrow();
        createAccountViaRepo(sender, "SRC001", "USD", new BigDecimal("1000.00"));

        String receiverEmail = "receiver@test.com";
        registerAndLogin(receiverEmail, "pass123");
        User receiver = userRepository.findByEmail(receiverEmail).orElseThrow();
        createAccountViaRepo(receiver, "TGT001", "USD", new BigDecimal("500.00"));
        CreateTransactionRequest request = new CreateTransactionRequest(
                "SRC001", "TGT001", new BigDecimal("100.00"), "Test Transfer");

        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        Account source = accountRepository.findByAccountNumber("SRC001");
        Account target = accountRepository.findByAccountNumber("TGT001");

        assertEquals(0, new BigDecimal("900.00").compareTo(source.getBalance()), "Source balance should decrease");
        assertEquals(0, new BigDecimal("600.00").compareTo(target.getBalance()), "Target balance should increase");
    }

    private String registerAndLogin(String email, String password) throws Exception {
        CreateUserDTO registerRequest = new CreateUserDTO("Test", "User", email, password);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        AuthResponse response = objectMapper.readValue(responseContent, AuthResponse.class);
        return response.token();
    }

    private void createAccountViaRepo(User user, String accountNumber, String currencyCode, BigDecimal balance) {
        Account account = new Account(Currency.getInstance(currencyCode), user);
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        accountRepository.save(account);
    }
}
