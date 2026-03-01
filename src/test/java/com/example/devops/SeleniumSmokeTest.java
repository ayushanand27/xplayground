package com.example.devops;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selenium smoke test that validates the deployed application at http://localhost:8800
 * It is DISABLED by default and will run only when -Dselenium.enabled=true is provided.
 * This keeps CI green even on agents without a browser.
 */
public class SeleniumSmokeTest {

    @Test
    @EnabledIfSystemProperty(named = "selenium.enabled", matches = "true")
    void validateDeployedApp() {
        WebDriver driver = null;
        try {
            System.out.println("🔧 Configuring Chrome options for headless mode...");
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-software-rasterizer");
            
            System.out.println("🚀 Starting Chrome WebDriver...");
            driver = new ChromeDriver(options); // Selenium Manager resolves driver
            
            // Set timeouts
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            System.out.println("🌐 Navigating to http://localhost:8800...");
            driver.get("http://localhost:8800");
            
            // Wait for page to load
            wait.until(ExpectedConditions.titleContains("DevOps"));
            
            System.out.println("✅ Page loaded successfully!");
            System.out.println("📄 Page title: " + driver.getTitle());
            
            // Verify page title
            assertTrue(driver.getTitle().contains("DevOps"), 
                "Page title should contain 'DevOps' but was: " + driver.getTitle());
            
            // Wait for and verify heading
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
            String heading = driver.findElement(By.tagName("h1")).getText();
            
            System.out.println("📝 Page heading: " + heading);
            assertEquals("DevOps Pipeline Working", heading,
                "Heading should be 'DevOps Pipeline Working' but was: " + heading);
            
            System.out.println("✅ Selenium validated app at http://localhost:8800");
            System.out.println("🎉 All Selenium tests passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Selenium test failed with error: " + e.getMessage());
            e.printStackTrace();
            
            if (driver != null) {
                System.err.println("📸 Current URL: " + driver.getCurrentUrl());
                System.err.println("📄 Page source length: " + driver.getPageSource().length());
            }
            
            fail("Selenium test failed: " + e.getMessage());
        } finally {
            if (driver != null) {
                System.out.println("🔒 Closing WebDriver...");
                driver.quit();
            }
        }
    }
}
