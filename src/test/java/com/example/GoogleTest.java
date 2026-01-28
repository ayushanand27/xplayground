package com.example;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

public class GoogleTest {

    @Test
    void openGoogleAndPrintTitle() {
        // Setup ChromeDriver using WebDriverManager (no manual downloads required)
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        // Comment out headless if you want to see the browser UI
        // options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get("https://www.google.com");
            String title = driver.getTitle();
            System.out.println("Page Title: " + title);
        } finally {
            // Always quit the browser
            driver.quit();
        }
    }
}
