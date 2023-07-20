package com.example

import com.github.mvysny.vaadinboot.VaadinBoot
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.server.PWA
import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.annotation.WebListener
import org.slf4j.LoggerFactory

@WebListener
class Bootstrap : ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent?) {
        initialized = true
        log.info("Testapp Initialized")
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        initialized = false
        log.info("Testapp shut down")
    }

    companion object {
        private val log = LoggerFactory.getLogger(Bootstrap::class.java)

        @JvmField
        @Volatile
        var initialized = false
    }
}

@PWA(name = "Project Base for Vaadin", shortName = "Project Base")
class AppShell : AppShellConfigurator

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        VaadinBoot().run()
    }
}
