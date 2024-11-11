package com.example

import com.github.mvysny.vaadinboot.VaadinBoot
import jakarta.servlet.Servlet
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Actually starts up Jetty. DON'T USE FOR TESTING OF YOUR APPS: see [MainViewTest] instead.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JettyTest {
    private var vaadinBoot: VaadinBoot? = null
    @BeforeAll
    fun startJetty() {
        assertFalse(Bootstrap.initialized)
        vaadinBoot = VaadinBoot().withPort(44312).localhostOnly()
        vaadinBoot!!.start()
    }

    @AfterAll
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

        val body = wget("http://localhost:44312")
        assertTrue(body.contains("This file is auto-generated by Vaadin."), body);
    }

    @Test
    fun testStaticResourcesServed() {
        val body = wget("http://localhost:44312/ROOT")
        assertEquals("Don't delete this file; see Main.java for details.", body.trim());
    }
}