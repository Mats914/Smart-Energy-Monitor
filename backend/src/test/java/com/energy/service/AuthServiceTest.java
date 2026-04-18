package com.energy.service;

import com.energy.dto.Dtos.*;
import com.energy.exception.ConflictException;
import com.energy.model.User;
import com.energy.repository.UserRepository;
import com.energy.security.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService unit tests")
class AuthServiceTest {

    @Mock private UserRepository        userRepo;
    @Mock private PasswordEncoder       encoder;
    @Mock private AuthenticationManager authManager;
    @Mock private JwtUtil               jwtUtil;

    @InjectMocks private AuthService service;

    // ── register ──────────────────────────────────────────────

    @Test
    @DisplayName("register — creates user and returns token")
    void register_success() {
        when(userRepo.existsByUsername("alice")).thenReturn(false);
        when(userRepo.existsByEmail("alice@test.com")).thenReturn(false);
        when(encoder.encode("secret")).thenReturn("hashed");
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("alice")).thenReturn("jwt-token");

        var req = new RegisterRequest();
        req.setUsername("alice");
        req.setEmail("alice@test.com");
        req.setPassword("secret");

        AuthResponse res = service.register(req);

        assertThat(res.getToken()).isEqualTo("jwt-token");
        assertThat(res.getUsername()).isEqualTo("alice");
        assertThat(res.getRole()).isEqualTo("USER");
        verify(userRepo).save(argThat(u -> u.getPassword().equals("hashed")));
    }

    @Test
    @DisplayName("register — throws ConflictException for duplicate username")
    void register_duplicateUsername_throws() {
        when(userRepo.existsByUsername("alice")).thenReturn(true);

        var req = new RegisterRequest();
        req.setUsername("alice");
        req.setEmail("alice@test.com");
        req.setPassword("secret");

        assertThatThrownBy(() -> service.register(req))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Username");
    }

    @Test
    @DisplayName("register — throws ConflictException for duplicate email")
    void register_duplicateEmail_throws() {
        when(userRepo.existsByUsername("alice")).thenReturn(false);
        when(userRepo.existsByEmail("alice@test.com")).thenReturn(true);

        var req = new RegisterRequest();
        req.setUsername("alice");
        req.setEmail("alice@test.com");
        req.setPassword("secret");

        assertThatThrownBy(() -> service.register(req))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("Email");
    }

    // ── login ─────────────────────────────────────────────────

    @Test
    @DisplayName("login — bad credentials throw BadCredentialsException")
    void login_badCredentials_throws() {
        doThrow(new BadCredentialsException("bad"))
            .when(authManager).authenticate(any());

        var req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("wrong");

        assertThatThrownBy(() -> service.login(req))
            .isInstanceOf(BadCredentialsException.class);
    }
}
