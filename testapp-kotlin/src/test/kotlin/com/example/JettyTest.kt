package com.example

import com.github.mvysny.vaadinboot.VaadinBoot
import jakarta.servlet.Servlet
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Actually starts up Jetty. DON'T USE FOR TESTING OF YOUR APPS: see [MainViewTest] instead.
 */
class JettyTest {
    private var vaadinBoot: VaadinBoot? = null
    @BeforeEach
    fun startJetty() {
        assertFalse(Bootstrap.initialized)
        vaadinBoot = VaadinBoot().setPort(44312).localhostOnly().withServlet(MyServlet::class.java as Class<Servlet>)
        vaadinBoot!!.start()
    }

    @AfterEach
    fun stopJetty() {
        vaadinBoot!!.stop("tests")
        assertFalse(Bootstrap.initialized)
    }

    @Test
    fun testAppIsUp() {
        // make sure Bootstrap was called
        assertTrue(Bootstrap.initialized)
        // make sure the init parameter is parsed by Jetty correctly
        assertEquals("bar", Bootstrap.fooInitParamValue)

        // make sure something is running on port 44312
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder(URI.create("http://localhost:44312")).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        require(response.statusCode() == 200) {
            "$response failed: ${response.body()}"
        }
        println("Vaadin responded with: $response ${response.body()}")
    }
}