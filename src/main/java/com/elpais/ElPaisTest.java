package com.elpais;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.By;
import io.github.bonigarcia.wdm.WebDriverManager;

public class ElPaisTest {

    public static void main(String[] args) {

        WebDriverManager.chromedriver().setup(); //going with chrome first

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        WebDriver driver = new ChromeDriver(options);

        driver.get("https://elpais.com/");

        String pageTitle = driver.getTitle();
        System.out.println("Page Title: " + pageTitle);

        String language = driver.findElement(By.tagName("html"))
                                .getAttribute("lang");

        System.out.println("Language Attribute: " + language);

        if (language.contains("es")) {
            System.out.println("Website is in Spanish");
        } else {
            System.out.println("Website is NOT confirmed Spanish");
        }

        driver.quit();
    }
}