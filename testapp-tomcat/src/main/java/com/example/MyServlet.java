package com.example;

import com.vaadin.flow.server.VaadinServlet;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = "/*")
public class MyServlet extends VaadinServlet {
}
