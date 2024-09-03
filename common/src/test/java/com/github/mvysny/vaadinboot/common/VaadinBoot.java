package com.github.mvysny.vaadinboot.common;

public class VaadinBoot extends VaadinBootBase<VaadinBoot> {
    protected VaadinBoot() {
        super(new DummyWebServer());
    }
}
