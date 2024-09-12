package com.github.mvysny.vaadinboot.common;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class DummyWebServer implements WebServer {
    public VaadinBootBase<?> configured;
    public boolean startCalled = false;
    public boolean running = false;
    @Override
    public synchronized void configure(@NotNull VaadinBootBase<?> configuration) throws Exception {
        this.configured = configuration;
    }

    @Override
    public synchronized void start() throws Exception {
        assertNotNull(configured, "configure() not called");
        assertFalse(running, "start() called repeatedly");
        assertFalse(startCalled, "start() called repeatedly");
        running = true;
        startCalled = true;
    }

    @Override
    public synchronized void stop() throws Exception {
        assertNotNull(configured, "configure() not called");
        assertTrue(running, "start() not called");
        running = false;
    }

    @Override
    public synchronized void await() throws InterruptedException {
        assertNotNull(configured, "configure() not called");
        assertTrue(running, "start() not called");
    }

    @Override
    public @NotNull String getName() {
        return "Dummy";
    }

    public static class FailsToStart extends DummyWebServer {
        @Override
        public synchronized void start() throws Exception {
            super.start();
            running = false;
            throw new IOException("Port 8080 is occupied, cannot bind");
        }

        @Override
        public synchronized void stop() throws Exception {
            super.stop();
            fail("Shouldn't be called");
        }

        @Override
        public synchronized void await() throws InterruptedException {
            fail("Shouldn't be called");
        }
    }

    public static class FailsToStop extends DummyWebServer {
        @Override
        public synchronized void stop() throws Exception {
            super.stop();
            throw new IOException("Failed to stop");
        }
    }
}
