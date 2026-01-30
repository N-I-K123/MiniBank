package org.kz.minibank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kz.minibank.model.Account;
import org.kz.minibank.model.User;
import org.kz.minibank.repository.UserRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("John", "Doe", "john@example.com", "password");
        user.setId(1L);
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User created = userService.registerUser("John", "Doe", "john@example.com", "password");

        assertNotNull(created);
        assertEquals("john@example.com", created.getEmail());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("John", "Doe", "john@example.com", "password"));
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updated = userService.updateUser(1L, "Johnny", "Doe", "john@example.com");

        assertEquals("Johnny", updated.getName());
    }

    @Test
    void updateUser_EmailChanged_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updated = userService.updateUser(1L, "John", "Doe", "other@example.com");

        assertEquals("other@example.com", updated.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_EmailTaken_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(1L, "John", "Doe", "other@example.com"));
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(999L, "John", "Doe", "john@example.com"));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_HasAccounts_ThrowsException() {
        user.addAccount(new Account());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }
}
