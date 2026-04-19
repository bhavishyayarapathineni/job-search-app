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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_ExistingUser_ReturnsUserDetails() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("password123");
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        var result = userDetailsService.loadUserByUsername("test@gmail.com");
        assertNotNull(result);
        assertEquals("test@gmail.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound@gmail.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () ->
            userDetailsService.loadUserByUsername("notfound@gmail.com"));
    }
}
