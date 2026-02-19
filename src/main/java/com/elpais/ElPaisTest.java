package com.elpais;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

public class ElPaisTest {

    public static void main(String[] args) {

        WebDriverManager.chromedriver().setup(); //going with chrome first

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        WebDriver driver = new ChromeDriver(options);

        driver.get("https://elpais.com/");

        System.out.println("Title of page: " + driver.getTitle());

        driver.quit();
    }
}