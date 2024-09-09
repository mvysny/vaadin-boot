package com.example

import com.github.mvysny.vaadinboot.VaadinBoot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.expect

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
        // make sure something is running on port 44312
        val vaadinPage = URL("http://localhost:44312").readText()
        expect(true, vaadinPage) { vaadinPage.contains("window.Vaadin = {Flow: {devServerIsNotLoaded: true}};") }
        // make sure Bootstrap was called
        Assertions.assertTrue(Bootstrap.initialized)
        // check that static resources are served
        expect("Don't delete this file; see Main.java for details.") {
            URL("http://localhost:44312/ROOT").readText().trim()
        }
    }
}
