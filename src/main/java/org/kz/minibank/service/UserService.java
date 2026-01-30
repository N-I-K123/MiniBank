package org.kz.minibank.service;


import jakarta.transaction.Transactional;
import org.kz.minibank.model.User;
import org.kz.minibank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User registerUser(String name, String surname, String email, String password) {
        if (name == null || surname == null || email == null || password == null) {
            throw new IllegalArgumentException("All fields are required!");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with this email already exists!");
        }

        return userRepository.save(new User(name, surname, email, password));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found!"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found!"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, String name, String surname, String email) {
        User user = getUserById(id);

        if (email != null && !email.isBlank() && !user.getEmail().equals(email)) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("User with this email already exists!");
            }
            user.setEmail(email);
        }

        if (name != null && !name.isBlank() && !user.getName().equals(name)) user.setName(name);

        if (surname != null && !surname.isBlank() && !user.getSurname().equals(surname)) user.setSurname(surname);

        return userRepository.save(user);

    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        if (!user.getAccounts().isEmpty()) { throw new IllegalArgumentException("User has accounts!");}

        userRepository.deleteById(id);
    }
}
