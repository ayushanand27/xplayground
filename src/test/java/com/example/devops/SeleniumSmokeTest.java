package com.example.devops;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Dummy Selenium smoke test. Uses Chrome in headless mode.
 * It is DISABLED by default and will run only when -Dselenium.enabled=true is provided.
 * This keeps CI green even on agents without a browser.
 */
public class SeleniumSmokeTest {

    @Test
    @EnabledIfSystemProperty(named = "selenium.enabled", matches = "true")
    void openAndCloseBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options); // Selenium Manager resolves driver
        try {
            driver.get("about:blank");
            assertNotNull(driver.getTitle());
        } finally {
            driver.quit();
        }
    }
}
