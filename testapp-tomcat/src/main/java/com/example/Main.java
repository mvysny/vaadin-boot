package com.example;

import com.github.mvysny.vaadinboot.VaadinBoot;

public class Main {
    public static void main(String[] args) throws Exception {
        new VaadinBoot("testapp-tomcat-.*\\.jar").run();
    }
}
