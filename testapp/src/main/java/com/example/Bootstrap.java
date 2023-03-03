package com.example;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class Bootstrap implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);
    public static volatile boolean initialized = false;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        initialized = true;
        log.info("Testapp Initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        initialized = false;
        log.info("Testapp shut down");
    }
}
