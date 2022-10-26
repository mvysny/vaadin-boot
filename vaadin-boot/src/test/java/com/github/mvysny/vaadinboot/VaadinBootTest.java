package com.github.mvysny.vaadinboot;

import org.junit.jupiter.api.Test;

public class VaadinBootTest {
    @Test
    public void smoke() {
        new VaadinBoot().withArgs(new String[] { "9090" });
    }
}
