package com.example;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginTestProcess {

    static WebDriver driver;
    static WebDriverWait wait;
    static String baseUrl = "https://opensource-demo.orangehrmlive.com/web/index.php/auth/login";

    @BeforeAll
    static void setup() {
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

    @BeforeEach
    void openLoginPage() {
        driver.manage().deleteAllCookies();
        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

  @AfterAll
static void tearDown() {
    if (driver != null) {
        driver.quit();
        System.out.println("🧹 WebDriver closed after all tests.");
    }
}


    // ✅ STEP 1: Login with CSV data
    @Order(1)
    @ParameterizedTest
    @CsvFileSource(resources = "/loginData.csv", numLinesToSkip = 1)
    void loginAndAddEmployee(String username, String password, String expected) {
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/dashboard"),
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//p[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'invalid credentials')]"))
            ));

            boolean success = driver.getCurrentUrl().contains("/dashboard");

            if (expected.equalsIgnoreCase("success")) {
                assertTrue(success, "❌ Expected success but failed!");
                System.out.println("✅ Login success for user: " + username);

                // ✅ STEP 2: Only add employees if login success
                addEmployeesFromCsv();

                // ✅ STEP 3: Logout after done
                logoutIfLoggedIn();
            } else {
                assertFalse(success, "❌ Expected failure but succeeded!");
                System.out.println("⚠️ Login failed as expected for: " + username);
            }

        } catch (TimeoutException e) {
            fail("❌ Timeout – cannot determine login result.");
        }
    }

    // ✅ STEP 2: Add Employee (called internally)
    void addEmployeesFromCsv() {


        
pimMoveToAddEmployee();

        try (java.io.InputStream is = getClass().getResourceAsStream("/employeeData.csv");
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {

            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 4) continue;///

                String firstName = data[0].trim();
                String middleName = data[1].trim();
                String lastName = data[2].trim();
                String expected = data[3].trim();

               pimMoveToAddEmployee();
addEmployee(firstName, middleName, lastName, expected);

            }

        } catch (Exception e) {
            fail("❌ Failed to read employee CSV: " + e.getMessage());
        }
    }
void addEmployee(String firstName, String middleName, String lastName, String expected) {
    try {
        // ✅ Wait until the "Add Employee" form is visible
        WebElement firstNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.name("firstName")
        ));

        // Fill input fields
        firstNameInput.sendKeys(firstName);
        driver.findElement(By.name("middleName")).sendKeys(middleName);
        driver.findElement(By.name("lastName")).sendKeys(lastName);
        // Wait for loader to disappear
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
            By.cssSelector(".oxd-form-loader")
        ));

        // Click Save button
        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[normalize-space()='Save']")
        ));
        saveButton.click();

        // Wait for redirect (success)
        wait.until(ExpectedConditions.urlContains("/empNumber"));

        // Verify employee added by checking presence of name header
      boolean exists = driver.findElements(
    By.xpath("//h6[normalize-space()='Personal Details']")
).size() > 0;

        if (exists) {

            System.out.println("✅ Employee added: " + firstName + " " + middleName + " " + lastName);
            logoutIfLoggedIn();

        } else {
            // 🕒 Try waiting for alert for 3 seconds only
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                WebElement alertMessage = shortWait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[contains(text(),'No Records Found')]")
                    )
                );
                System.out.println("⚠️ Employee addition failed: " + alertMessage.getText());
            } catch (TimeoutException te) {
                System.out.println("ℹ️ No 'No Records Found' alert detected. Maybe another issue.");
            }

            pimMoveToAddEmployee();
        }

    } catch (TimeoutException e) {
        fail("❌ Timeout waiting for form submission or loader to finish.");
    } catch (Exception e) {
        e.printStackTrace();
        fail("❌ Error adding employee: " + e.getMessage());
    }
}


 
void pimMoveToAddEmployee() {
    try {
        WebElement pimMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//span[text()='PIM']")));
        pimMenu.click();

        WebElement addEmployeeButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[text()='Add Employee']")));
        addEmployeeButton.click();

        // ✅ Wait until the Add Employee form appears
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstName")));

        System.out.println("✅ Navigated to Add Employee page.");
    } catch (Exception e) {
        fail("❌ Could not navigate to Add Employee page: " + e.getMessage());
    }
}


    // ✅ STEP 3: Logout helper
  public  void logoutIfLoggedIn() {
        try {
            WebElement userDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".oxd-userdropdown-tab")));
            userDropdown.click();

            WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[text()='Logout']")));
            logoutButton.click();

            // Wait until login screen appears again
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            System.out.println("✅ Logout successful.");

        } catch (TimeoutException | NoSuchElementException e) {
            System.out.println("ℹ️ Not logged in, no logout needed.");
        }
    }
}
