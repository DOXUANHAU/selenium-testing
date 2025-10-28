package com.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.WebDriverManager;

public class SeleniumTest {
    private static final Logger logger = LoggerFactory.getLogger(SeleniumTest.class);

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.setBinary("/usr/bin/brave-browser");

        WebDriver driver = new ChromeDriver(options);
        driver.get("https://www.brave.com");

        logger.info("Title: {}", driver.getTitle());

        driver.quit();
    }
}
