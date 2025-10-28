package com.example;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginTest {

    static WebDriver driver;
    static WebDriverWait wait;
    static String baseUrl = "https://opensource-demo.orangehrmlive.com/web/index.php/auth/login";

    @BeforeAll
    static void setup() {
        try {
            // 🔧 Setup ChromeDriver version compatible with Brave 140
            WebDriverManager.chromedriver().browserVersionDetection(true)
                .setup();

            // ⚙️ Cấu hình Brave thông qua ChromeOptions
            ChromeOptions options = new ChromeOptions();

            // 🔹 Set Brave browser binary path
            options.setBinary("/usr/bin/brave-browser");

            // 🔧 Disable password and notification prompts
            options.addArguments("--disable-save-password-bubble");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-features=PasswordLeakDetection");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            Map<String, Object> prefs = new HashMap<>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            options.setExperimentalOption("prefs", prefs);

            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            System.out.println("🚀 BraveDriver khởi động thành công!");
        } catch (Exception e) {
            System.err.println("❌ Error setting up WebDriver: " + e.getMessage());
            throw e;
        }
    }

    @BeforeEach
    void openLoginPage() {
        driver.manage().deleteAllCookies();
        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    @AfterEach
    void logoutIfLoggedIn() {
        try {
            WebElement userDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".oxd-userdropdown-tab")));
            userDropdown.click();

            WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[text()='Logout']")));
            logoutButton.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            System.out.println("✅ Đã logout sau test.");
        } catch (TimeoutException | NoSuchElementException e) {
            System.out.println("ℹ️ Không cần logout (chưa đăng nhập).");
        }
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ✅ Test Data-Driven từ file CSV
    @Order(1)
    @ParameterizedTest
    @CsvFileSource(resources = "/loginData.csv", numLinesToSkip = 1)
    void loginDDT(String username, String password, String expected) {

        System.out.printf("🔹 Testing with: username='%s', password='%s'%n", username, password);

        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // 🧩 Kiểm tra kết quả đăng nhập
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/dashboard"),
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//p[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'invalid credentials')]"))
            ));

            boolean success = driver.getCurrentUrl().contains("/dashboard");
            if (expected.equalsIgnoreCase("success")) {
                assertTrue(success, "❌ Expected success but failed!");
            } else {
                assertFalse(success, "❌ Expected failure but succeeded!");
            }

        } catch (TimeoutException e) {
            fail("❌ Test timeout – không xác định được trạng thái login.");
        }
    }
}
