package com.elpais;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.List;

public class ElPaisTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    public static void main(String[] args) {

        setupDriver();

        openHomePage();
        verifySpanishLanguage();

        navigateToOpinion();
        printFirstFiveArticleLinks();
        extractTitlesFromFirstFiveArticles();
        driver.quit();
    }

    private static void setupDriver() {

        WebDriverManager.chromedriver().setup();// going with chrome first

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    private static void openHomePage() {

        driver.get("https://elpais.com/");
        System.out.println("Page Title: " + driver.getTitle());
    }

    private static void verifySpanishLanguage() {

        String language = driver.findElement(By.tagName("html"))
                .getAttribute("lang");

        System.out.println("Language Attribute: " + language);
    }

    private static void navigateToOpinion() {

        driver.get("https://elpais.com/opinion/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("article")));

        System.out.println("Navigated to Opinion section.");
    }

    private static void printFirstFiveArticleLinks() {

        List<WebElement> articles = driver.findElements(By.cssSelector("article"));

        System.out.println("Total articles found: " + articles.size());

        for (int i = 0; i < 5 && i < articles.size(); i++) {

            WebElement linkElement = articles.get(i)
                    .findElement(By.cssSelector("a"));

            String articleUrl = linkElement.getAttribute("href");

            System.out.println("Article " + (i + 1) + " URL: " + articleUrl);
        }
    }

    private static void extractTitlesFromFirstFiveArticles() {

        List<WebElement> links = driver.findElements(By.cssSelector("article a[href]"));

        int count = 0;

        for (WebElement link : links) {

            String articleUrl = link.getAttribute("href");

            // Only select real articles containing a date pattern
            if (articleUrl.contains("/202")) {

                driver.navigate().to(articleUrl);

                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1")));

                String title = driver.findElement(By.cssSelector("h1")).getText();

                System.out.println("Article " + (count + 1) + " Title: " + title);

                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("article")));

                count++;

                if (count == 5) {
                    break;
                }
            }
        }
    }
}