@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo 黑客松项目评审工具
echo ========================================
echo.

REM 检查 JAR 文件是否存在
if not exist "target\ai-reviewer-2.0.jar" (
    echo ❌ 错误: JAR 文件不存在！
    echo.
    echo 请先运行以下命令打包项目:
    echo   mvn clean package -DskipTests
    echo.
    pause
    exit /b 1
)

REM 检查 Java 是否安装
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 错误: 未安装 Java 或 Java 不在 PATH 中！
    echo.
    echo 请先安装 Java 17 或更高版本
    echo.
    pause
    exit /b 1
)

echo [1/3] 正在评审 Gitee 项目...
echo 项目: https://gitee.com/gnnu/yumbo-music-utils
echo 团队: Team Awesome
echo.

REM 评审 Gitee 项目
java -jar target\ai-reviewer-2.0.jar hackathon ^
  --gitee-url https://gitee.com/gnnu/yumbo-music-utils ^
  --team "Team Awesome" ^
  --output score.json ^
  --report report.md

echo.
if %ERRORLEVEL% EQU 0 (
  echo ========================================
  echo ✅ 评审完成！
  echo ========================================
  echo.
  echo 📊 评分结果: score.json
  echo 📄 详细报告: report.md
  echo.
  echo 使用以下命令查看结果:
  echo   type score.json
  echo   notepad report.md
  echo.
) else (
  echo ========================================
  echo ❌ 评审失败！
  echo ========================================
  echo.
  echo 错误码: %ERRORLEVEL%
  echo.
  echo 常见问题排查:
  echo 1. 检查网络连接
  echo 2. 确认 Gitee 项目 URL 是否正确
  echo 3. 检查 AI 服务配置 (src/main/resources/config.yaml)
  echo.
)

pause

