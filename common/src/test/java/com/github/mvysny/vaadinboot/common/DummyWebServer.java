package com.github.mvysny.vaadinboot.common;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class DummyWebServer implements WebServer {
    public VaadinBootBase<?> configured;
    public boolean started;
    @Override
    public void configure(@NotNull VaadinBootBase<?> configuration) throws Exception {
        this.configured = configuration;
    }

    @Override
    public void start() throws Exception {
        assertNotNull(configured, "configure() not called");
        assertFalse(started, "start() called repeatedly");
        started = true;
    }

    @Override
    public void stop() throws Exception {
        assertNotNull(configured, "configure() not called");
        assertTrue(started, "start() not called");
        started = false;
    }

    @Override
    public void await() throws InterruptedException {
        assertNotNull(configured, "configure() not called");
        assertTrue(started, "start() not called");
    }

    @Override
    public @NotNull String getName() {
        return "Dummy";
    }

    public static class FailsToStart extends DummyWebServer {
        @Override
        public void start() throws Exception {
            super.start();
            started = false;
            throw new IOException("Port 8080 is occupied, cannot bind");
        }

        @Override
        public void stop() throws Exception {
            super.stop();
            fail("Shouldn't be called");
        }

        @Override
        public void await() throws InterruptedException {
            fail("Shouldn't be called");
        }
    }

    public static class FailsToStop extends DummyWebServer {
        @Override
        public void stop() throws Exception {
            super.stop();
            throw new IOException("Failed to stop");
        }
    }
}
