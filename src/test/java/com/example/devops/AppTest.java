package com.example.devops;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    void printsExpectedMessage() {
        // Capture System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        try {
            App.main(new String[]{});
            String output = outContent.toString().trim();
            assertEquals(App.MESSAGE, output);
        } finally {
            System.setOut(originalOut);
        }
    }
}
