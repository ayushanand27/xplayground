package com.example.devops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Selenium smoke test that validates the deployed application at http://localhost:8800
 * It is DISABLED by default and will run only when -Dselenium.enabled=true is provided.
 * This keeps CI green even on agents without a browser.
 */
public class SeleniumSmokeTest {

    @Test
    @EnabledIfSystemProperty(named = "selenium.enabled", matches = "true")
    void validateDeployedApp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options); // Selenium Manager resolves driver
        try {
            // Open the locally deployed application
            driver.get("http://localhost:8800");
            
            // Verify page title
            assertTrue(driver.getTitle().contains("DevOps"));
            
            // Verify heading contains our message
            String heading = driver.findElement(By.tagName("h1")).getText();
            assertEquals("DevOps Pipeline Working", heading);
            
            System.out.println("✅ Selenium validated app at http://localhost:8800");
        } finally {
            driver.quit();
        }
    }
}
