package com.example;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginTest extends BaseTest {

    @Test
    @Order(1)
    void login() {
        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");

        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        username.sendKeys("Admin");
        password.sendKeys("admin123");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        System.out.println("✅ Logged in successfully.");
    }

    @Test
    @Order(2)
    void addEmployeesFromCsv() throws Exception {
        List<String> lines = Files.readAllLines(Paths.get("src/test/resources/employeeData.csv"));
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length < 3) continue;
            addEmployee(parts[0], parts[1], parts[2]);
        }
    }

    void addEmployee(String firstName, String middleName, String lastName) {
        try {
            // Navigate to Add Employee
            WebElement pimMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[text()='PIM']")));
            pimMenu.click();

            WebElement addEmployee = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[text()='Add Employee']")));
            addEmployee.click();

            // Wait for form
            WebElement firstNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstName")));
            firstNameInput.sendKeys(firstName);
            driver.findElement(By.name("middleName")).sendKeys(middleName);
            driver.findElement(By.name("lastName")).sendKeys(lastName);

            // Wait for loader gone
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".oxd-form-loader")));

            WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Save']")));
            saveButton.click();

            // Confirm redirect
            wait.until(ExpectedConditions.urlContains("/empNumber"));

            // Verify name on profile
            WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h6[contains(text(),'Personal Details')]")));
            Assertions.assertTrue(title.isDisplayed(), "Employee saved successfully");

            System.out.println("✅ Added: " + firstName + " " + middleName + " " + lastName);

        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("❌ Error adding employee: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void logout() {
        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".oxd-userdropdown-tab")));
        userMenu.click();

        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[text()='Logout']")));
        logout.click();

        wait.until(ExpectedConditions.urlContains("/auth/login"));
        System.out.println("✅ Logged out successfully.");
    }
}
