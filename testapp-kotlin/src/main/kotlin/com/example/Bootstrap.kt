package com.example

import com.github.mvysny.vaadinboot.VaadinBoot
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.server.PWA
import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.annotation.WebListener
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@WebListener
class Bootstrap : ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent?) {
        executor = Executors.newSingleThreadScheduledExecutor()
        initialized = true
        log.info("Testapp Initialized")
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.SECONDS)
        executor.shutdownNow()
        executor.awaitTermination(1, TimeUnit.SECONDS)
        initialized = false
        log.info("Testapp shut down")
    }

    companion object {
        private val log = LoggerFactory.getLogger(Bootstrap::class.java)

        @JvmField
        @Volatile
        var initialized = false

        @Volatile
        lateinit var executor: ScheduledExecutorService
    }
}

@PWA(name = "Project Base for Vaadin", shortName = "Project Base")
@Push
class AppShell : AppShellConfigurator

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        VaadinBoot().run()
    }
}
