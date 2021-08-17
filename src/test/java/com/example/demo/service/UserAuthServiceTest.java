package com.example.demo.service;

import com.example.demo.dao.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

public class UserAuthServiceTest {
    private final UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
    private final UserAuthService userAuthService = new UserAuthService(userRepositoryMock);

    @Test
    void loadUserByUsernameTest() {
        final String username = "username";
        final String notExistingUsername = "user";
        final User expectedUserDetails = new User(username, "password", new ArrayList<>());
        Mockito.when(userRepositoryMock.findUserByUsername("username"))
                .thenReturn(Optional.of(new com.example.demo.domain.User(
                        1L, username, "password", null, new HashSet<>(), null)));
        Mockito.when(userRepositoryMock.findUserByUsername(Mockito.argThat(arg -> !arg.equals("username"))))
                .thenReturn(Optional.empty());

        Assertions.assertEquals(expectedUserDetails, userAuthService.loadUserByUsername(username));
        Assertions.assertThrows(UsernameNotFoundException.class, () -> userAuthService.loadUserByUsername(notExistingUsername));
    }
}
