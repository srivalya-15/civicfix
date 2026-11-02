package com.civicfix.config;

import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
        String path = req.getRequestURI();
        boolean admin = path.startsWith("/api/admin");
        boolean needLogin = admin
                || path.startsWith("/api/my")
                || (path.equals("/api/complaints") && req.getMethod().equals("POST"));
        if (!needLogin) return true;

        HttpSession s = req.getSession(false);
        Object role = s == null ? null : s.getAttribute("role");
        if (role == null) { deny(res, 401, "Please login first"); return false; }
        if (admin && !"ADMIN".equals(role)) { deny(res, 403, "Admins only"); return false; }
        return true;
    }

    private void deny(HttpServletResponse res, int code, String msg) throws IOException {
        res.setStatus(code);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"" + msg + "\"}");
    }
}
