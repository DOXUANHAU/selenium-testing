package com.example;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;


public class BaseTest {
    protected static WebDriver driver;
    protected static WebDriverWait wait;

    @BeforeAll
    static void setUp() {
        try {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();

            options.addPreference("dom.webnotifications.enabled", false);
            options.addPreference("signon.rememberSignons", false);
            options.addPreference("network.cookie.cookieBehavior", 0);
            options.addPreference("media.autoplay.default", 1);

            driver = new FirefoxDriver(options);
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        } catch (Exception e) {
            System.err.println("❌ Error setting up WebDriver: " + e.getMessage());
            throw e;
        }
    }

    // Optional: Method to open a specific URL before each test
    // To use this method, uncomment it and add these imports:
    // import org.openqa.selenium.By;
    // import org.openqa.selenium.support.ui.ExpectedConditions;
    // import org.junit.jupiter.api.BeforeEach;
    /*
    @BeforeEach
    void openLoginPage(String baseUrl) {
        driver.manage().deleteAllCookies();
        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }
    */

    @AfterAll
static void tearDown() {
    if (driver != null) {
        driver.quit();
        System.out.println("🧹 WebDriver closed after all tests.");
    }
}
}
