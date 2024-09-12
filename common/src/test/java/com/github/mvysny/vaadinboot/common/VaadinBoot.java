package com.github.mvysny.vaadinboot.common;

import org.jetbrains.annotations.NotNull;

public class VaadinBoot extends VaadinBootBase<VaadinBoot> {
    public VaadinBoot() {
        this(new DummyWebServer());
    }
    public VaadinBoot(@NotNull DummyWebServer webServer) {
        super(webServer);
    }
}
