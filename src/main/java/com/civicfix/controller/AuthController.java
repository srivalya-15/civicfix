package com.civicfix.controller;

import com.civicfix.model.User;
import com.civicfix.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record RegisterReq(String name, String email, String phone, String password) { }
    public record LoginReq(String email, String password) { }

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterReq r, HttpSession session) {
        if (r.name() == null || r.name().isBlank()) throw new IllegalArgumentException("Name is required");
        if (r.email() == null || !r.email().contains("@")) throw new IllegalArgumentException("Enter a valid email");
        if (r.password() == null || r.password().length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new IllegalArgumentException("Email is already registered");

        User u = new User();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPhone(r.phone());
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRole(User.Role.CITIZEN);
        users.save(u);
        return startSession(session, u);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginReq r, HttpSession session) {
        String email = r.email() == null ? "" : r.email().trim().toLowerCase();
        User u = users.findByEmail(email).orElse(null);
        if (u == null || r.password() == null || !encoder.matches(r.password(), u.getPasswordHash()))
            throw new IllegalArgumentException("Invalid email or password");
        return startSession(session, u);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("loggedIn", false);
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        Object uid = session.getAttribute("uid");
        Map<String, Object> m = new LinkedHashMap<>();
        if (uid == null) { m.put("loggedIn", false); return m; }
        m.put("loggedIn", true);
        m.put("name", session.getAttribute("name"));
        m.put("role", session.getAttribute("role"));
        return m;
    }

    private Map<String, Object> startSession(HttpSession session, User u) {
        session.setAttribute("uid", u.getId());
        session.setAttribute("name", u.getName());
        session.setAttribute("role", u.getRole().name());
        return me(session);
    }
}
