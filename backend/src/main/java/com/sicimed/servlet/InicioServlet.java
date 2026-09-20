package com.sicimed.servlet;

import com.sicimed.util.DbConnection;
import com.sicimed.util.SchemaInitializer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/inicio")
public class InicioServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SchemaInitializer.ensureInitialized();
        request.setAttribute("perfil", DbConnection.getProfile());
        request.getRequestDispatcher("/WEB-INF/views/inicio.jsp").forward(request, response);
    }
}
