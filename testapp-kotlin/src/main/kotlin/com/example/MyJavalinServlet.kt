package com.example

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.JavalinServlet
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(name = "MyJavalinServlet", urlPatterns = ["/rest/*"])
class MyJavalinServlet : HttpServlet() {
    private val javalin: JavalinServlet = Javalin.createStandalone()
        .get("/rest") { ctx: Context -> ctx.result("Hello!") }
        .javalinServlet()

    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        javalin.service(req, resp)
    }
}
