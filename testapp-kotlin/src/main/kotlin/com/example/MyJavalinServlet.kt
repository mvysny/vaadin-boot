package com.example

import io.javalin.Javalin
import io.javalin.http.Context
import jakarta.servlet.Servlet
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@WebServlet(name = "MyJavalinServlet", urlPatterns = ["/rest/*"])
class MyJavalinServlet : HttpServlet() {
    private val javalin: Servlet = Javalin.create { config ->
        config.routes.get("/rest") { ctx: Context -> ctx.result("Hello!") }
    }.javalinServlet()

    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        javalin.service(req, resp)
    }
}
