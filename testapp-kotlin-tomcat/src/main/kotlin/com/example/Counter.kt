package com.example

import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Span
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Demoes Push over websockets.
 */
class Counter : Span() {
    // guarded-by: Vaadin session lock
    private var counter = 0
    @Transient
    private var future: ScheduledFuture<*>? = null

    override fun onAttach(attachEvent: AttachEvent?) {
        super.onAttach(attachEvent)
        counter = 0
        val ui = UI.getCurrent()
        future = Bootstrap.executor.scheduleWithFixedDelay({
            incrementCounter(ui)
        }, 1L, 1L, TimeUnit.SECONDS)
    }

    private fun incrementCounter(ui: UI) {
        ui.access { text = "Counter: ${++counter}" }
    }

    override fun onDetach(detachEvent: DetachEvent) {
        future?.cancel(false)
        future = null
        super.onDetach(detachEvent)
    }
}
