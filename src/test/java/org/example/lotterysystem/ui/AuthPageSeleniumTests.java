package org.example.lotterysystem.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * 使用 Selenium 针对登录、注册页面的基础 UI 自动化用例。
 */
class AuthPageSeleniumTests extends BaseUiTest {

    @Test
    @DisplayName("注册页必填校验提示应出现")
    void registerPageShowsValidationMessages() {
        openPage("/register.html");
        driver.findElement(By.cssSelector("#registerForm button[type='submit']")).click();

        WebElement nameError = waitForVisible(By.id("name-error"));
        WebElement mailError = waitForVisible(By.id("mail-error"));
        WebElement phoneError = waitForVisible(By.id("phoneNumber-error"));
        WebElement passwordError = waitForVisible(By.id("password-error"));

        Assertions.assertEquals("请输入您的姓名", nameError.getText());
        Assertions.assertEquals("请输入有效的邮箱地址", mailError.getText());
        Assertions.assertEquals("请输入您的手机号", phoneError.getText());
        Assertions.assertTrue(passwordError.getText().startsWith("请输入密码"));
    }

    @Test
    @DisplayName("管理员登录页 Tab 切换应隐藏/展示对应表单")
    void loginTabsSwitchForms() {
        openPage("/blogin.html");

        WebElement codeTab = driver.findElement(By.cssSelector(".tab-box span[data-form='codeForm']"));
        codeTab.click();

        WebElement codeForm = waitForVisible(By.id("codeForm"));
        WebElement loginForm = driver.findElement(By.id("loginForm"));

        Assertions.assertTrue(codeForm.isDisplayed(), "验证码登录表单应可见");
        Assertions.assertFalse(loginForm.isDisplayed(), "密码登录表单应被隐藏");
    }

    @Test
    @DisplayName("管理员密码登录缺少输入时应给出校验提示")
    void passwordLoginRequiresInputValidation() {
        openPage("/blogin.html");
        driver.findElement(By.cssSelector("#loginForm button.login-btn")).click();

        WebElement phoneError = waitForVisible(By.id("phoneNumber-error"));
        WebElement passwordError = waitForVisible(By.id("password-error"));

        Assertions.assertEquals("请输入您的手机号", phoneError.getText());
        Assertions.assertTrue(passwordError.getText().contains("请输入密码"));
    }

    @Override
    protected void openPage(String path) {
        super.openPage(path);
        // 等待 jQuery validate 针对 DOM 完成绑定，避免首个点击被忽略
        waitWithTimeout().until(ExpectedConditions.jsReturnsValue("return !!window.jQuery;"));
    }
}
