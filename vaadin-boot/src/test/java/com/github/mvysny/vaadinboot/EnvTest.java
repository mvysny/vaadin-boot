package com.github.mvysny.vaadinboot;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class EnvTest {

    @Test
    public void testFixClasspath() {
        final String cp = System.getProperty("java.class.path");
        try {
            // test crazy classpaths
            System.setProperty("java.class.path", File.pathSeparator);
            new VaadinBoot().fixClasspath();
            assertEquals("", System.getProperty("java.class.path"));
            System.setProperty("java.class.path", "a" + File.pathSeparator + "b" + File.pathSeparator + "c");
            new VaadinBoot().fixClasspath();
            assertEquals("", System.getProperty("java.class.path"));
            System.setProperty("java.class.path", File.pathSeparator + "a" + File.pathSeparator + "b" + File.pathSeparator + "c" + File.pathSeparator);
            new VaadinBoot().fixClasspath();
            assertEquals("", System.getProperty("java.class.path"));

            // classpath with existing entry
            System.setProperty("java.class.path", File.pathSeparator + "src/main/java" + File.pathSeparator);
            new VaadinBoot().fixClasspath();
            assertEquals("src/main/java", System.getProperty("java.class.path"));

            // filters out non-existing entries
            System.setProperty("java.class.path", File.pathSeparator + "src/main/java" + File.pathSeparator + "nonexisting");
            new VaadinBoot().fixClasspath();
            assertEquals("src/main/java", System.getProperty("java.class.path"));
        } finally {
            System.setProperty("java.class.path", cp);
        }
    }
}