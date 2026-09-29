package org.eclipse.classic.web.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // เตรียมข้อมูลที่ต้องส่งให้หน้า home ตรงนี้ได้
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}
