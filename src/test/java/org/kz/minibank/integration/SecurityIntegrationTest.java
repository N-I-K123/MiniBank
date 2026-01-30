package org.kz.minibank.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.kz.minibank.DTO.AuthResponse;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldFailWhenNotAuthenticated() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "SRC001", "TGT001", new BigDecimal("100.00"), "Test Transfer");

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailWhenAccessingOtherUsersAccount() throws Exception {
        String hackerEmail = "hacker@test.com";
        String hackerToken = registerAndLogin(hackerEmail, "pass123");

        String victimEmail = "victim@test.com";
        registerAndLogin(victimEmail, "pass123");
        User victim = userRepository.findByEmail(victimEmail).orElseThrow();
        createAccountViaRepo(victim, "VICTIM_ACC", "USD", new BigDecimal("1000.00"));

        User hacker = userRepository.findByEmail(hackerEmail).orElseThrow();
        createAccountViaRepo(hacker, "HACKER_ACC", "USD", new BigDecimal("0.00"));

        CreateTransactionRequest request = new CreateTransactionRequest(
                "VICTIM_ACC", "HACKER_ACC", new BigDecimal("100.00"), "Stealing Money");

        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + hackerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailWhenReadingOtherUsersBalance() throws Exception {
        String hackerToken = registerAndLogin("spy@test.com", "pass123");

        registerAndLogin("rich_guy@test.com", "pass123");
        User victim = userRepository.findByEmail("rich_guy@test.com").orElseThrow();

        Account victimAccount = new Account(Currency.getInstance("USD"), victim);
        victimAccount.setBalance(new BigDecimal("1000000.00"));
        accountRepository.save(victimAccount);
        Long victimAccountId = victimAccount.getId();

        mockMvc.perform(get("/api/accounts/" + victimAccountId + "/balance")
                .header("Authorization", "Bearer " + hackerToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
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
