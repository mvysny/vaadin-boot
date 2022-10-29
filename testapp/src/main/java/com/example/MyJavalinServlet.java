package com.example;

import io.javalin.Javalin;
import io.javalin.http.JavalinServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "MyJavalinServlet", urlPatterns = {"/rest/*"})
public class MyJavalinServlet extends HttpServlet {
    private final JavalinServlet javalin = Javalin.createStandalone()
            .get("/rest", ctx -> ctx.result("Hello!"))
            .javalinServlet();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        javalin.service(req, resp);
    }
}
