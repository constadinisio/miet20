package com.miet20.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainAppSmokeTest {

    @Test
    void mainClassNameIsStable() {
        assertEquals("com.miet20.app.MainApp", MainApp.class.getName());
    }
}
