package com.example

import com.github.mvysny.kaributesting.v10.LocatorJ
import com.github.mvysny.kaributesting.v10.MockVaadin.setup
import com.github.mvysny.kaributesting.v10.MockVaadin.tearDown
import com.github.mvysny.kaributesting.v10.Routes
import com.vaadin.flow.component.UI
import org.junit.jupiter.api.*

/**
 * Karibu tests. Quick tests without you having to actually start Jetty. PREFERRED WAY OF TESTING YOUR APPS.
 */
class MainViewTest {
    @BeforeEach
    fun setupVaadin() {
        setup(routes)
    }

    @AfterEach
    fun tearDownVaadin() {
        tearDown()
    }

    @Test
    fun smoke() {
        UI.getCurrent().navigate(
            MainView::class.java
        )
        LocatorJ._assertOne(MainView::class.java)
        Assertions.assertTrue(Bootstrap.initialized)
    }

    companion object {
        private val routes = Routes().autoDiscoverViews("com.example")
        @BeforeAll
        @JvmStatic
        fun setupApp() {
            Assertions.assertFalse(Bootstrap.initialized)
            Bootstrap().contextInitialized(null)
        }

        @AfterAll
        @JvmStatic
        fun tearDownApp() {
            Bootstrap().contextDestroyed(null)
            Assertions.assertFalse(Bootstrap.initialized)
        }
    }
}
