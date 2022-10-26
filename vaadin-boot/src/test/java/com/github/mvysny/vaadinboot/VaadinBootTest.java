package com.github.mvysny.vaadinboot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VaadinBootTest {
    @Test
    public void smoke() {
        new VaadinBoot().withArgs(new String[]{"9090"});
    }

    @Test
    public void testDefaultPort() {
        assertEquals(8080, new VaadinBoot().port);
    }

    @Test
    public void testPortParsedCorrectly() {
        assertEquals(9090, new VaadinBoot().withArgs(new String[]{"9090"}).port);
    }
}
