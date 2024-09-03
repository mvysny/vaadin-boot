package com.github.mvysny.vaadinboot.common;

import org.jetbrains.annotations.NotNull;

public class DummyWebServer implements WebServer {
    @Override
    public void configure(@NotNull VaadinBootBase<?> configuration) throws Exception {
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void await() throws InterruptedException {
    }

    @Override
    public @NotNull String getName() {
        return "Dummy";
    }
}
