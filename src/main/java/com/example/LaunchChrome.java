package com.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

public class LaunchChrome {

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();

        WebDriver driver = new ChromeDriver();
        driver.get("https://www.google.com");

        try {
            Thread.sleep(3000); // just to see the browser
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        driver.quit();
    }
}
