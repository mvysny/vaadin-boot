package com.example;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.vaadin.flow.component.UI;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import static com.github.mvysny.kaributesting.v10.LocatorJ._assertOne;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Karibu tests. Quick tests without you having to actually start Jetty. PREFERRED WAY OF TESTING YOUR APPS.
 */
public class MainViewTest {
    @NotNull
    private static final Routes routes = new Routes().autoDiscoverViews("com.example");

    @BeforeAll
    public static void setupApp() {
        assertFalse(Bootstrap.initialized);
        new Bootstrap().contextInitialized(null);
    }

    @AfterAll
    public static void tearDownApp() {
        new Bootstrap().contextDestroyed(null);
        assertFalse(Bootstrap.initialized);
    }

    @BeforeEach
    public void setupVaadin() {
        MockVaadin.setup(routes);
    }

    @AfterEach
    public void tearDownVaadin() {
        MockVaadin.tearDown();
    }

    @Test
    public void smoke() {
        UI.getCurrent().navigate(MainView.class);
        _assertOne(MainView.class);
        assertTrue(Bootstrap.initialized);
    }
}
