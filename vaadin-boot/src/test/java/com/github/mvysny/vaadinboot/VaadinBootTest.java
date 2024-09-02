package com.github.mvysny.vaadinboot;

import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class VaadinBootTest {
    @Test
    public void smoke() {
        new VaadinBoot();
    }
}
