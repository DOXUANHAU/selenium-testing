package com.example;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginBraveTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    void setUp() {
        // Path to Brave browser executable (Linux default)
        String bravePath = "/usr/bin/brave-browser";

        // Configure Brave with ChromeOptions
        ChromeOptions options = new ChromeOptions();
        options.setBinary(bravePath);
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-features=PrivacySandboxSettings4");
        // Optional: Uncomment for headless mode
        // options.addArguments("--headless=new");

        // Initialize ChromeDriver with Brave
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Method to read CSV file and provide test data
    private List<Object[]> readCsvData() throws IOException, CsvValidationException {
        String csvFilePath = "/home/xuanhau/CodeSpace/testing_folder/data_driven_test_v2/xuanhau/src/test/resources/login_data_test.csv"; // Adjusted path
        List<Object[]> testData = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(new FileReader(csvFilePath))) {
            csvReader.readNext(); // Skip header row
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                testData.add(new Object[]{row[0], row[1], Integer.parseInt(row[2])});
            }
        }
        return testData;
    }

    @ParameterizedTest
    @MethodSource("readCsvData")
    void testLoginPage(String username, String password, int expectedStatus) {
        // Navigate to the login page
        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");

        try {
            // Wait for and interact with username field
            WebElement usernameField = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("username"))
            );
            usernameField.clear();
            usernameField.sendKeys(username);

            // Wait for and interact with password field
            WebElement passwordField = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("password"))
            );
            passwordField.clear();
            passwordField.sendKeys(password);

            // Wait for and click the login button
            WebElement loginButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
            );
            loginButton.click();

            // Verify the result based on expectedStatus
            if (expectedStatus == 200) {
                // For successful login, wait for the dashboard page (title contains "OrangeHRM")
                wait.until(ExpectedConditions.titleContains("OrangeHRM"));
                String actualTitle = driver.getTitle();
                assert actualTitle.contains("OrangeHRM") :
                    String.format("Expected successful login (status 200) for user %s, but title was %s", username, actualTitle);
                System.out.printf("Successful login for user %s. Title: %s%n", username, actualTitle);

                // Perform logout
                try {
                    // Click the user dropdown (top-right corner)
                    WebElement userDropdown = wait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector(".oxd-userdropdown-tab"))
                    );
                    userDropdown.click();

                    // Click the "Logout" link
                    WebElement logoutLink = wait.until(
                        ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Logout')]"))
                    );
                    logoutLink.click();

                    // Verify return to login page
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
                    System.out.printf("Successfully logged out for user %s. Back on login page.%n", username);
                } catch (Exception e) {
                    System.err.printf("Logout failed for user %s: %s%n", username, e.getMessage());
                    throw e; // Re-throw to fail the test if logout fails
                }
            } else if (expectedStatus == 401) {
                // For failed login, check for the "Invalid credentials" error message
                WebElement errorMessage = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("p.oxd-text.oxd-alert-content-text")
                    )
                );
                String actualError = errorMessage.getText();
                assert actualError.contains("Invalid credentials") :
                    String.format("Expected login failure (status 401) for user %s, but error message was %s", username, actualError);
                System.out.printf("Failed login for user %s as expected. Error: %s%n", username, actualError);
            }

        } catch (Exception e) {
            System.err.printf("Test failed for user %s: %s%n", username, e.getMessage());
            // Save screenshot for debugging
           
            throw e; // Re-throw to mark test as failed
        }
    }

    @AfterAll
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}