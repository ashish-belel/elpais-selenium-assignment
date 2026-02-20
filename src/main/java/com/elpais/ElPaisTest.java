package com.elpais;

// Selenium imports
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
// WebDriverManager import
import io.github.bonigarcia.wdm.WebDriverManager;
// Java imports
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
//import java.io.InputStream; //these are separately imported in the function
//import java.io.OutputStream; //don't need it but will if needed
import java.nio.charset.StandardCharsets;

public class ElPaisTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    public static void main(String[] args) {

        setupDriver();

        openHomePage();
        verifySpanishLanguage();

        navigateToOpinion();
        printFirstFiveArticleLinks();
        // extractTitlesFromFirstFiveArticles();
        // translateTitlesToEnglish(null);
        List<String> spanishTitles = extractTitlesFromFirstFiveArticles();
        List<String> englishTitles = translateTitlesToEnglish(spanishTitles);
        analyzeRepeatedWords(englishTitles);

        driver.quit();
        System.exit(0);
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
     * IGNORE BCOZ IT WAS FIRST ATTEMPT
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

                // CONTENT
                try {
                    // 1. Wait for the article container
                    WebElement contentDiv = wait.until(
                            ExpectedConditions.presenceOfElementLocated(
                                    By.cssSelector("article, div.a_c.clearfix"))); // Using a flexible container
                                                                                   // selector

                    // 2. Find BOTH <p> tags AND <figcaption> tags using a CSS selector with a comma
                    List<WebElement> textElements = contentDiv.findElements(By.cssSelector("p, figcaption"));

                    System.out.println("Content (Spanish):");

                    if (textElements.isEmpty()) {
                        System.out.println("[No standard text found. This might be a video or pure image gallery]");
                    }

                    // 3. Loop through everything we found
                    for (WebElement element : textElements) {
                        // Use textContent to bypass any "visibility" rules
                        String text = element.getAttribute("textContent").trim();

                        // Ensure it's not empty and ignore generic photo credits like "Foto: El País"
                        if (!text.isEmpty() && !text.startsWith("Foto:")) {
                            System.out.println(text);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Could not locate the content container for this article.");
                }

                downloadCoverImageIfExists(i + 1);

            } catch (Exception e) {
                System.err.println("Error processing article " + (i + 1) + ": " + e.getMessage());
            }
        }

        return spanishTitles;
    }

    // I CALLED THIS FUNCTION IN THE ABOVE FUNCTION TO DOWNLOAD COVER IMAGE WHEN
    // EXTRACTING TITLES AND CONTENT
    private static void downloadCoverImageIfExists(int articleNumber) {

        try {
            WebElement image = driver.findElement(By.cssSelector("figure img"));
            String imageUrl = image.getAttribute("src");

            if (imageUrl != null && !imageUrl.isEmpty()) {

                // java.io.InputStream in = new java.net.URL(imageUrl).openStream();
                try (java.io.InputStream in = new java.net.URL(imageUrl).openStream()) {
                    java.nio.file.Files.copy(
                            in,
                            java.nio.file.Paths.get("article_" + articleNumber + ".jpg"),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    System.out.println("Cover image downloaded.");
                }
            }

        } catch (Exception e) {
            System.out.println("No cover image found.");
        }
    }

    private static List<String> translateTitlesToEnglish(List<String> spanishTitles) {

        List<String> translatedTitles = new ArrayList<>();

        System.out.println("\n===== TRANSLATED HEADERS =====");

        for (String title : spanishTitles) {

            try {

                String encodedTitle = java.net.URLEncoder.encode(title, "UTF-8");

                String urlStr = "https://translate.googleapis.com/translate_a/single"
                        + "?client=gtx"
                        + "&sl=es"
                        + "&tl=en"
                        + "&dt=t"
                        + "&q=" + encodedTitle;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                // Extract translated text
                String result = response.toString();

                // First translated segment is inside [[[ "TRANSLATION"
                String translated = result.split("\"")[1]; // safe enough for this format

                translatedTitles.add(translated);

                System.out.println("Original:   " + title);
                System.out.println("Translated: " + translated);
                System.out.println();

            } catch (Exception e) {
                System.out.println("Translation failed for: " + title);
            }
        }

        return translatedTitles;
    }

    //on executing , no repeated words more than twice were found.
    //the given examples didn't have many repeated words
    private static void analyzeRepeatedWords(List<String> englishTitles) {

        System.out.println("\n===== REPEATED WORD ANALYSIS =====");

        java.util.Map<String, Integer> wordCount = new java.util.HashMap<>();

        for (String title : englishTitles) {

            String[] words = title
                    .replaceAll("[^a-zA-Z ]", "")
                    .toLowerCase()
                    .split("\\s+");

            for (String word : words) {

                if (word.length() > 2) { // ignores very small words like "of", "to"
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        }

        boolean found = false;

        for (String word : wordCount.keySet()) {

            if (wordCount.get(word) > 2) {
                System.out.println(word + " -> " + wordCount.get(word));
                found = true;
            }
        }
        
        if (!found) {
            System.out.println("No repeated words more than twice were found.");
        }
    }
}