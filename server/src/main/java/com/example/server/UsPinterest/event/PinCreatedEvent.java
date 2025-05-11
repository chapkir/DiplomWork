package com.example.server.UsPinterest.event;

import com.example.server.UsPinterest.model.Pin;

public class PinCreatedEvent {
    private final Pin pin;

    public PinCreatedEvent(Pin pin) {
        this.pin = pin;
    }

    public Pin getPin() {
        return pin;
    }
}