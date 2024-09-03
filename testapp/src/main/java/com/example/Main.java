package com.example;

import com.github.mvysny.vaadinboot.VaadinBoot;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println(Thread.currentThread().getContextClassLoader());
        final String classpath = System.getProperty("java.class.path");
        System.out.println(classpath);
        new VaadinBoot().run();
    }
}
