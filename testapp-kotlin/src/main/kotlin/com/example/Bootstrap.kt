package com.example

import com.github.mvysny.vaadinboot.VaadinBoot
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.server.PWA
import org.slf4j.LoggerFactory
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

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
        VaadinBoot().withArgs(args).run()
    }
}
