package com.elpais;

//Selenium imports
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
//Java imports
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

public class ElPaisTest {

    public static void main(String[] args) {

        // Combination of Desktop + Mobile (Task 5 requirement)
        String[] platforms = { "chrome", "firefox", "edge", "safari", "iphone" };

        ExecutorService executor = Executors.newFixedThreadPool(platforms.length);

        for (String platform : platforms) {

            executor.submit(() -> {

                WebDriver driver = null;
                WebDriverWait wait;

                try {
                    driver = createBrowserStackDriver(platform);
                    wait = new WebDriverWait(driver, Duration.ofSeconds(20));

                    openHomePage(driver, wait);
                    verifySpanishLanguage(driver);
                    navigateToOpinion(driver, wait);

                    List<String> spanishTitles = extractFirstFiveArticles(driver, wait);
                    List<String> englishTitles = translateTitlesToEnglish(spanishTitles);
                    analyzeRepeatedWords(englishTitles);

                    ((JavascriptExecutor) driver).executeScript(
                            "browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\":\"passed\", \"reason\": \"Execution successful\"}}");
                } catch (Exception e) {
                    if (driver != null) {
                        ((JavascriptExecutor) driver).executeScript(
                                "browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\":\"failed\", \"reason\": \"Exception occurred\"}}");
                    }
                    e.printStackTrace();
                } finally {
                    if (driver != null) {
                        driver.quit();
                    }
                }
            });
        }

        executor.shutdown();
    }

    private static WebDriver createBrowserStackDriver(String platform) throws Exception {

        String USERNAME = "ashishbelel_tDJ698";
        String ACCESS_KEY = "6kGk6cARURSgYZ29t1z8";

        String URL = "https://" + USERNAME + ":" + ACCESS_KEY +
                "@hub-cloud.browserstack.com/wd/hub";

        MutableCapabilities capabilities = new MutableCapabilities();
        HashMap<String, Object> bstackOptions = new HashMap<>();

        bstackOptions.put("buildName", "ElPais Assignment Build");
        bstackOptions.put("sessionName", platform + " Test");

        // Desktop configurations
        if (platform.equalsIgnoreCase("chrome") ||
                platform.equalsIgnoreCase("firefox") ||
                platform.equalsIgnoreCase("edge")) {

            capabilities.setCapability("browserName", platform.substring(0, 1).toUpperCase() + platform.substring(1));
            capabilities.setCapability("browserVersion", "latest");
            bstackOptions.put("os", "Windows");
            bstackOptions.put("osVersion", "11");
        }

        else if (platform.equalsIgnoreCase("safari")) {
            capabilities.setCapability("browserName", "Safari");
            capabilities.setCapability("browserVersion", "latest");
            bstackOptions.put("os", "OS X");
            bstackOptions.put("osVersion", "Ventura");
        }

        // Mobile configuration
        else if (platform.equalsIgnoreCase("iphone")) {
            capabilities.setCapability("browserName", "Safari");
            bstackOptions.put("deviceName", "iPhone 14");
            bstackOptions.put("realMobile", "true");
            bstackOptions.put("osVersion", "16");
        }

        capabilities.setCapability("bstack:options", bstackOptions);

        return new RemoteWebDriver(new URL(URL), capabilities);
    }

    private static void openHomePage(WebDriver driver, WebDriverWait wait) {
        driver.get("https://elpais.com/");
        handleCookies(wait);
    }

    private static void handleCookies(WebDriverWait wait) {
        try {
            WebElement acceptButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector("button[data-testid='didomi-notice-agree-button']")));
            acceptButton.click();
        } catch (Exception ignored) {
        }
    }

    private static void verifySpanishLanguage(WebDriver driver) {
        String language = driver.findElement(By.tagName("html")).getAttribute("lang");
        System.out.println("Language Attribute: " + language);
    }

    private static void navigateToOpinion(WebDriver driver, WebDriverWait wait) {
        driver.get("https://elpais.com/opinion/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("article")));
    }

    private static List<String> extractFirstFiveArticles(WebDriver driver, WebDriverWait wait) {

        List<WebElement> articleLinks = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector("article h2 a")));

        List<String> urls = new ArrayList<>();
        List<String> spanishTitles = new ArrayList<>();

        for (int i = 0; i < 5 && i < articleLinks.size(); i++) {
            urls.add(articleLinks.get(i).getAttribute("href"));
        }

        for (int i = 0; i < urls.size(); i++) {

            driver.get(urls.get(i));

            WebElement titleElement = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

            String title = titleElement.getText().trim();
            spanishTitles.add(title);

            System.out.println("\n===== ARTICLE " + (i + 1) + " =====");
            System.out.println("Title: " + title);
            System.out.println("Content (Spanish):");

            try {
                WebElement content = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector("div.a_c.clearfix")));

                List<WebElement> paragraphs = content.findElements(By.tagName("p"));

                for (WebElement p : paragraphs) {
                    String text = p.getText().trim();
                    if (!text.isEmpty()) {
                        System.out.println(text);
                    }
                }
            } catch (Exception e) {
                System.out.println("No content found.");
            }

            downloadCoverImageIfExists(driver, i + 1);
        }

        return spanishTitles;
    }

    private static void downloadCoverImageIfExists(WebDriver driver, int articleNumber) {
        try {
            WebElement image = driver.findElement(By.cssSelector("figure img"));
            String imageUrl = image.getAttribute("src");

            if (imageUrl != null && !imageUrl.isEmpty()) {
                try (java.io.InputStream in = new java.net.URL(imageUrl).openStream()) {
                    java.nio.file.Files.copy(
                            in,
                            java.nio.file.Paths.get("article_" + articleNumber + ".jpg"),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static List<String> translateTitlesToEnglish(List<String> spanishTitles) {

        List<String> translatedTitles = new ArrayList<>();

        System.out.println("\n===== TRANSLATED HEADERS =====");

        for (String title : spanishTitles) {

            try {
                String encodedTitle = java.net.URLEncoder.encode(title, "UTF-8");

                String urlStr = "https://translate.googleapis.com/translate_a/single"
                        + "?client=gtx&sl=es&tl=en&dt=t&q=" + encodedTitle;

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

                String translated = response.toString().split("\"")[1];

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

    private static void analyzeRepeatedWords(List<String> englishTitles) {

        System.out.println("\n===== REPEATED WORD ANALYSIS =====");

        Map<String, Integer> wordCount = new HashMap<>();

        for (String title : englishTitles) {

            String[] words = title
                    .replaceAll("[^a-zA-Z ]", "")
                    .toLowerCase()
                    .split("\\s+");

            for (String word : words) {
                if (word.length() > 2) {
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