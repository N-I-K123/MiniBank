package org.kz.minibank.controller;


import jakarta.validation.Valid;
import org.kz.minibank.DTO.AccountResponseDTO;
import org.kz.minibank.DTO.CreateUserDTO;
import org.kz.minibank.DTO.UserResponseDTO;
import org.kz.minibank.model.Account;
import org.kz.minibank.model.User;
import org.kz.minibank.service.AccountService;
import org.kz.minibank.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AccountService accountService;

    public UserController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile(Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(new UserResponseDTO(user.getName(), user.getSurname(), user.getEmail()));
    }



    @GetMapping("/me/userAccounts")
    public ResponseEntity<List<AccountResponseDTO>> getMyAccounts(Principal principal){
        List<Account> accounts = accountService.getAccountsByUserId(principal.getName());
        List<AccountResponseDTO> response = accounts.stream().map(a -> new AccountResponseDTO(
                a.getId(),
                a.getAccountNumber(),
                a.getBalance(),
                a.getCurrency().getCurrencyCode(),
                a.getUser().getName(),
                a.getUser().getSurname(),
                a.getUser().getEmail()
        )).toList();
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody CreateUserDTO userDTO, Principal principal){
        User userToEdit = userService.getUserById(id);
        if (!userToEdit.getEmail().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this user!");
        }
        userToEdit = userService.updateUser(id, userDTO.name(), userDTO.surname(), userDTO.email());
        return ResponseEntity.ok(new UserResponseDTO(userToEdit.getName(), userToEdit.getSurname(), userToEdit.getEmail()));
    }

    //todo finish when roles will be added
    /*@GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }*/

    /*@DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }*/
}
