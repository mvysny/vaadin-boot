package com.example

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.resource.EmptyResource
import org.eclipse.jetty.webapp.WebAppContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertEquals

class MyJavalinServletTest {
    private var server: Server? = null

    @BeforeEach
    fun startJetty() {
        val ctx = WebAppContext()
        ctx.baseResource = EmptyResource.INSTANCE
        ctx.addServlet(MyJavalinServlet::class.java, "/rest/*")
        server = Server(30123)
        server!!.handler = ctx
        server!!.start()
    }

    @AfterEach
    fun stopJetty() {
        server?.stop()
    }

    @Test
    fun testRest() {
        assertEquals("Hello!", URL("http://localhost:30123/rest").readText())
    }
}
