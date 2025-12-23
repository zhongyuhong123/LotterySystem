# 自动化测试报告

## 概览
- 项目测试类型：单元/集成测试 + Selenium UI 自动化。
- 最近未实际执行测试（未记录运行结果）；以下为现有用例梳理与运行指引。

## 测试环境要求
- JDK 17，Maven。
- 本地 MySQL、Redis、RabbitMQ 等按 `application-dev.properties` 配置可用（用于后端集成测试）。
- Chrome 浏览器已安装；如 WebDriverManager 受限，可手动指定驱动路径：
  - 在运行参数添加 `-Dwebdriver.chrome.driver="C:/chromedriver-win64/chromedriver.exe"`。
  - 默认使用 `-Dselenium.base-url=http://localhost:8080 -Dselenium.headless=true`，可按需覆盖。

## 用例清单（按包）
### `org.example.lotterysystem`（后端）
- `LotterySystemApplicationTests`：应用上下文加载校验。
- 其他类（如 `UserTest`、`DrawPrizeTest`、`RedisTest`、`JWTTest` 等）：围绕用户、抽奖、Redis、JWT、加解密、日志、SQL、验证码服务等逻辑的单元/集成测试。需依赖真实或可用的外部服务与数据库数据。

### `org.example.lotterysystem.ui`（前端 UI）
- `AuthPageSeleniumTests`（基于 `BaseUiTest`）：
  - 注册页必填校验提示。
  - 管理员登录页 Tab 切换显示/隐藏对应表单。
  - 管理员密码登录缺省输入的校验提示。
- 基类 `BaseUiTest`：统一 Chrome 启动、headless 配置、显式等待与 URL 构造。已添加兼容启动参数 `--remote-allow-origins=*`；如需手动驱动路径请使用上文运行参数。

## 运行指引
> Maven `pom.xml` 默认 `maven.test.skip=true`，需手动关闭跳过。

示例命令（仅运行 UI 测试）：
```
mvn -Dmaven.test.skip=false ^
    -Dselenium.base-url=http://localhost:8080 ^
    -Dselenium.headless=true ^
    -Dtest=org.example.lotterysystem.ui.AuthPageSeleniumTests test
```

示例命令（运行全部测试，需准备依赖服务与测试数据）：
```
mvn -Dmaven.test.skip=false test
```

## 风险与注意
- 未实际执行测试，结果未知；首次运行可能暴露依赖环境或测试数据缺失问题。
- UI 用例依赖前端静态资源可访问；请确保应用已启动且端口、路径与 `selenium.base-url` 对齐。
- 若使用远程/云端浏览器，请相应调整 ChromeOptions 或改用远程 WebDriver。用户在 `BaseUiTest` 中已标注本地驱动路径方案。
