package com.elpais;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.ArrayList;
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
        handleCookies();
    }

    private static void handleCookies() {
        try {
            WebElement acceptButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector("button[data-testid='didomi-notice-agree-button']")));
            acceptButton.click();
            System.out.println("Cookies accepted.");
        } catch (Exception e) {
            System.out.println("No cookie popup found.");
        }
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

    /*
     * private static void extractTitlesFromFirstFiveArticles() {
     * 
     * List<WebElement> articleLinks =
     * driver.findElements(By.cssSelector("article h2 a"));
     * 
     * System.out.println("Real article links found: " + articleLinks.size());
     * 
     * for (int i = 0; i < 5 && i < articleLinks.size(); i++) {
     * 
     * String articleUrl = articleLinks.get(i).getAttribute("href");
     * 
     * System.out.println("Article " + (i + 1) + " URL: " + articleUrl);
     * 
     * driver.navigate().to(articleUrl);
     * 
     * wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
     * 
     * String title = driver.findElement(By.cssSelector("main h1")).getText();
     * 
     * System.out.println("Article " + (i + 1) + " Title: " + title);
     * 
     * driver.navigate().back();
     * 
     * wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
     * "article")));
     * }
     * }
     */
    private static List<String> extractTitlesFromFirstFiveArticles() {
        // 1. Ensure we are on the main page before finding links
        List<WebElement> articleLinkElements = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector("article h2 a")));

        List<String> articleUrls = new ArrayList<>();
        List<String> spanishTitles = new ArrayList<>();

        // Step 1: Store first 5 URLs (Safe because we haven't navigated away yet)
        for (int i = 0; i < 5 && i < articleLinkElements.size(); i++) {
            String url = articleLinkElements.get(i).getAttribute("href");
            if (url != null && !url.isEmpty()) {
                articleUrls.add(url);
            }
        }

        // Step 2: Visit each article
        for (int i = 0; i < articleUrls.size(); i++) {
            try {
                String articleUrl = articleUrls.get(i);
                driver.get(articleUrl);
                // DEBUGGING LINES: Check if the URL is valid and what the title is
                System.out.println("DEBUG: Actually loaded URL -> " + driver.getCurrentUrl());
                System.out.println("DEBUG: Page Title -> " + driver.getTitle());

                // Use a more generic selector if h1.a_t is too specific/fragile
                WebElement titleElement = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector("h1")));

                String title = titleElement.getAttribute("textContent").trim();
                spanishTitles.add(title);

                System.out.println("\n===== ARTICLE " + (i + 1) + " =====");
                System.out.println("URL: " + articleUrl);
                System.out.println("Title: " + title);

                // CONTENT - Wrapping in try-catch in case an article has no text
                try {
                    WebElement contentDiv = wait.until(
                            ExpectedConditions.presenceOfElementLocated(
                                    By.cssSelector("article, div.a_c"))); // Tried a more flexible selector

                    List<WebElement> paragraphs = contentDiv.findElements(By.tagName("p"));
                    System.out.println("Content:");
                    for (WebElement p : paragraphs) {
                        if (!p.getText().isBlank()) {
                            System.out.println(p.getText().trim());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Could not extract content for this article.");
                }

                downloadCoverImageIfExists(i + 1);

            } catch (Exception e) {
                System.err.println("Error processing article " + (i + 1) + ": " + e.getMessage());
            }
        }

        return spanishTitles;
    }

    private static void downloadCoverImageIfExists(int articleNumber) {

        try {
            WebElement image = driver.findElement(By.cssSelector("figure img"));
            String imageUrl = image.getAttribute("src");

            if (imageUrl != null && !imageUrl.isEmpty()) {

                java.io.InputStream in = new java.net.URL(imageUrl).openStream();

                java.nio.file.Files.copy(
                        in,
                        java.nio.file.Paths.get("article_" + articleNumber + ".jpg"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Cover image downloaded.");
            }

        } catch (Exception e) {
            System.out.println("No cover image found.");
        }
    }
}