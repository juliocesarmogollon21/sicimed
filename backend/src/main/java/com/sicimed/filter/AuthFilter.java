package com.sicimed.filter;

import com.sicimed.model.Usuario;
import com.sicimed.util.JsonUtil;
import com.sicimed.util.SchemaInitializer;
import com.sicimed.util.TokenStore;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/api/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        SchemaInitializer.ensureInitialized();
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.startsWith("/api/auth/") || "OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String header = req.getHeader("Authorization");
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }
        Usuario user = TokenStore.resolve(token);
        if (user == null) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "No autenticado");
            return;
        }
        req.setAttribute("usuario", user);
        chain.doFilter(request, response);
    }
}
