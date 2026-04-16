package com.jobsearch.security;

import com.jobsearch.model.User;
import com.jobsearch.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserDetailsServiceImpl service;

    @Test
    void loadUserByUsername_ReturnsSecurityUser() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword("secret");
        user.setRole(User.Role.USER);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("user@test.com");

        assertEquals("user@test.com", details.getUsername());
        assertEquals("secret", details.getPassword());
        assertEquals("ROLE_USER", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_ThrowsWhenMissing() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing@test.com"));
    }
}
