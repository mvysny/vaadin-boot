package com.example

import com.github.mvysny.vaadinboot.VaadinBoot
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.server.PWA
import com.vaadin.flow.server.VaadinServlet
import jakarta.servlet.Servlet
import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.annotation.WebInitParam
import jakarta.servlet.annotation.WebListener
import jakarta.servlet.annotation.WebServlet
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@WebListener
class Bootstrap : ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent?) {
        executor = Executors.newSingleThreadScheduledExecutor()
        fooInitParamValue = sce?.let {
            val reg = it.servletContext.getServletRegistration("myservlet")
            requireNotNull(reg) { "No servlet named 'myservlet': ${it.servletContext.servletRegistrations}" }
            val param = reg.getInitParameter("foo")
            requireNotNull(param) { "No 'foo' parameter: ${reg.initParameters} "}
            param
        } ?: ""
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

        @JvmField
        @Volatile
        var fooInitParamValue = ""

        @Volatile
        lateinit var executor: ScheduledExecutorService
    }
}

@PWA(name = "Project Base for Vaadin", shortName = "Project Base")
@Push
class AppShell : AppShellConfigurator

/**
 * Tests a custom servlet with an init parameter. Tests for https://github.com/mvysny/vaadin-boot/issues/22
 */
@WebServlet(name = "myservlet", urlPatterns = ["/*"], initParams = [WebInitParam(name = "foo", value = "bar")])
class MyServlet : VaadinServlet()

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        VaadinBoot().run()
    }
}
