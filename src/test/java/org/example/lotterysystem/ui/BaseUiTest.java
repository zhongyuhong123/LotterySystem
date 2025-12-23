package org.example.lotterysystem.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Selenium UI 测试基类，负责驱动初始化和通用等待工具。
 */
public abstract class BaseUiTest {

    protected static String baseUrl;
    protected static boolean headless;

    protected WebDriver driver;

    @BeforeAll
    static void setupClass() {
        baseUrl = System.getProperty("selenium.base-url", "http://localhost:8080");
        headless = Boolean.parseBoolean(System.getProperty("selenium.headless", "true"));
        WebDriverManager.chromedriver().setup();

        //无法启动 WebDriverManager，替换手动指定本地 ChromeDriver 路径
        // 注意：路径中的 \ 需要转义为 \\，或直接用 /
//        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
    }

    @BeforeEach
    void setupDriver() {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        // 新增：解决新版 Chrome 跨域/启动兼容问题（必加）
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1366,768");
        options.setAcceptInsecureCerts(true);
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void openPage(String path) {
        driver.get(buildUrl(path));
    }

    private String buildUrl(String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return baseUrl + path;
    }

    protected WebElement waitForVisible(By locator) {
        return waitWithTimeout().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForPresent(By locator) {
        return waitWithTimeout().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected WebDriverWait waitWithTimeout() {
        return new WebDriverWait(driver, Duration.ofSeconds(10));
    }
}
