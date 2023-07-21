package com.example

import com.github.mvysny.vaadinboot.VaadinBoot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
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
        Assertions.assertFalse(Bootstrap.initialized)
        vaadinBoot = VaadinBoot().setPort(44312).localhostOnly()
        vaadinBoot!!.start()
    }

    @AfterEach
    fun stopJetty() {
        vaadinBoot!!.stop("tests")
        Assertions.assertFalse(Bootstrap.initialized)
    }

    @Test
    fun testAppIsUp() {
        // make sure Bootstrap was called
        Assertions.assertTrue(Bootstrap.initialized)

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