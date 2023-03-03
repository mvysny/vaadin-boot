package com.example

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.servlet.JavalinServlet
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@WebServlet(name = "MyJavalinServlet", urlPatterns = ["/rest/*"])
class MyJavalinServlet : HttpServlet() {
    private val javalin: JavalinServlet = Javalin.createStandalone()
        .get("/rest") { ctx: Context -> ctx.result("Hello!") }
        .javalinServlet()

    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        javalin.service(req, resp)
    }
}
