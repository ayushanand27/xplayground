package com.example.devops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    void messageConstantIsCorrect() {
        // Test the constant exists and has expected value
        assertNotNull(App.MESSAGE);
        assertEquals("DevOps Pipeline Working", App.MESSAGE);
    }

    @Test
    void messageIsNotEmpty() {
        // Additional sanity check
        assertEquals(false, App.MESSAGE.isEmpty());
    }
}
