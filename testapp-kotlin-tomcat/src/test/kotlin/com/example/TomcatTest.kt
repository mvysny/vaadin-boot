package com.example

import com.github.mvysny.vaadinboot.VaadinBoot
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Actually starts up Jetty. DON'T USE FOR TESTING OF YOUR APPS: see [MainViewTest] instead.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TomcatTest {
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
        assertTrue(body.contains("window.Vaadin"), body);
    }

    @Test
    fun testStaticResourcesServed() {
        val body = wget("http://localhost:44312/ROOT")
        assertEquals("Don't delete this file; see Main.java for details.", body.trim());
    }

    @Test
    fun testRest() {
        assertEquals("Hello!", wget("http://localhost:44312/rest"))
    }
}
