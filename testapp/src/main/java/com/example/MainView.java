package com.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends VerticalLayout {
    public MainView() {
        add(new H3("Yay, your Vaadin app is running!"));
        add(new Button("Click me", e -> Notification.show("Clicked!")));
    }
}
