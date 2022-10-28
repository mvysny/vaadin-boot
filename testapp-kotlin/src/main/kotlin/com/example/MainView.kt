package com.example

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route

/**
 * The main view contains a button and a click listener.
 */
@Route("")
class MainView : VerticalLayout() {
    init {
        add(H3("Yay, your Vaadin app is running!"))
        add(Button("Click me") { Notification.show("Clicked!") })
    }
}
