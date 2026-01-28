package com.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

public class SeleniumApp {
    public static void main(String[] args) {
        // No environment variables needed; WebDriverManager handles driver binaries
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        // Uncomment to run without opening the browser UI:
        // options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get("https://www.google.com");
            System.out.println("Launched Chrome. Title: " + driver.getTitle());
        } finally {
            driver.quit();
            System.out.println("Browser closed. Exiting application.");
        }
    }
}
