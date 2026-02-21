# El País Automation Assignment

## Overview

This project automates the following tasks using Java, Selenium WebDriver, and BrowserStack:

1. Visit the Spanish news website El País.
2. Verify that the website is displayed in Spanish.
3. Navigate to the Opinion section.
4. Extract the first five articles.
5. Print the title and content of each article (in Spanish).
6. Download the cover image (if available).
7. Translate the article titles into English.
8. Analyze translated titles to find words repeated more than twice.
9. Execute the solution across 5 parallel browsers using BrowserStack.

All functionality is implemented inside a single file: ElPaisTest.java
-------------------
🛠 Tech Stack

Java 17

Selenium WebDriver

Maven

BrowserStack (Cloud Execution)

Google Translate API (HTTP integration)
-------------------
📂 Project Structure
```text
src/
 └── main/
     └── java/
         └── com/elpais/
             └── ElPaisTest.java
```
-------------------
☁️ BrowserStack Execution (Task 5)

The script runs in parallel across:

Chrome (Windows 11)

Firefox (Windows 11)

Edge (Windows 11)

Safari (macOS Ventura)

iPhone 14 (Real Mobile Device)

Parallel execution is implemented using:
ExecutorService
Each thread creates a separate RemoteWebDriver session.
All the assignment tasks were implemented.