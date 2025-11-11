@echo off
REM Hackathon AI 评审工具部署脚本 (Windows版)
REM 用于快速部署和配置黑客松AI评审环境

setlocal enabledelayedexpansion

REM 颜色定义 (Windows CMD)
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "RESET=[0m"

REM 配置变量
set "JAR_NAME=hackathon-reviewer-2.0.jar"
set "CONFIG_FILE=hackathon-config.yaml"
set "DOWNLOAD_URL=https://github.com/jinhua10/ai-reviewer/releases/download/v2.0/%JAR_NAME%"

REM 日志函数
:log_info
echo [%BLUE%INFO%RESET%] %~1
goto :eof

:log_success
echo [%GREEN%SUCCESS%RESET%] %~1
goto :eof

:log_warning
echo [%YELLOW%WARNING%RESET%] %~1
goto :eof

:log_error
echo [%RED%ERROR%RESET%] %~1
goto :eof

REM 检查Java环境
:check_java
call :log_info "检查Java环境..."
java -version >nul 2>&1
if errorlevel 1 (
    call :log_error "Java未安装，请先安装JDK 17+"
    exit /b 1
)

for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER=%%i
set JAVA_VER=%JAVA_VER:"=%
for /f "delims=." %%i in ("%JAVA_VER%") do set JAVA_MAJOR=%%i

if %JAVA_MAJOR% lss 17 (
    call :log_error "需要JDK 17+，当前版本: %JAVA_VER%"
    exit /b 1
)

call :log_success "Java环境检查通过 (JDK %JAVA_VER%)"
goto :eof

REM 下载工具
:download_tool
call :log_info "下载Hackathon AI评审工具..."

if exist "%JAR_NAME%" (
    call :log_warning "文件已存在，跳过下载"
    goto :eof
)

if exist "%WINDIR%\System32\curl.exe" (
    curl -L -o "%JAR_NAME%" "%DOWNLOAD_URL%"
) else (
    call :log_error "需要curl来下载文件，请安装curl或手动下载"
    call :log_info "下载地址: %DOWNLOAD_URL%"
    exit /b 1
)

if not exist "%JAR_NAME%" (
    call :log_error "下载失败"
    exit /b 1
)

call :log_success "工具下载完成: %JAR_NAME%"
goto :eof

REM 配置API密钥
:configure_api_key
call :log_info "配置API密钥..."

if "%DEEPSEEK_API_KEY%"=="" (
    set /p API_KEY="请输入DeepSeek API密钥: "
    if "!API_KEY!"=="" (
        call :log_error "API密钥不能为空"
        exit /b 1
    )

    REM 设置环境变量
    setx DEEPSEEK_API_KEY "!API_KEY!" /M >nul 2>&1
    if errorlevel 1 (
        call :log_warning "无法设置系统环境变量，请手动设置"
        echo 请运行: setx DEEPSEEK_API_KEY "your-api-key" /M
    )

    call :log_success "API密钥已配置"
) else (
    call :log_success "API密钥已配置"
)
goto :eof

REM 创建配置文件
:create_config
call :log_info "创建配置文件..."

if exist "%CONFIG_FILE%" (
    call :log_warning "配置文件已存在，跳过创建"
    goto :eof
)

(
echo # Hackathon AI 评审工具配置文件
echo # 专为黑客松比赛优化的配置
echo.
echo # AI服务配置
echo aiService:
echo   provider: "deepseek"
echo   apiKey: "${DEEPSEEK_API_KEY}"
echo   baseUrl: "https://api.deepseek.com/v1"
echo   model: "deepseek-chat"
echo   maxTokens: 2000
echo   temperature: 0.3
echo   timeout: 15000
echo   maxRetries: 2
echo   maxConcurrency: 5
echo.
echo # 缓存配置
echo cache:
echo   enabled: true
echo   type: "file"
echo   ttlHours: 2
echo   maxSize: 500
echo.
echo # 文件扫描配置
echo fileScan:
echo   includePatterns:
echo     - "*.java"
echo     - "*.py"
echo     - "*.js"
echo     - "*.ts"
echo     - "*.html"
echo     - "*.css"
echo   excludePatterns:
echo     - "*.log"
echo     - "node_modules/"
echo     - "*.git*"
echo     - ".DS_Store"
echo   maxFileSize: 512
echo   maxFilesCount: 100
echo.
echo # 分析配置
echo analysis:
echo   analysisDimensions:
echo     - "architecture"
echo     - "code_quality"
echo     - "technical_debt"
echo     - "functionality"
echo     - "business_value"
echo     - "test_coverage"
echo   dimensionWeights:
echo     architecture: 0.15
echo     code_quality: 0.20
echo     technical_debt: 0.10
echo     functionality: 0.25
echo     business_value: 0.20
echo     test_coverage: 0.10
echo   batchSize: 5
echo   maxConcurrentBatches: 2
echo   batchTimeout: 200000
echo.
echo # 评分规则配置
echo scoring:
echo   rules:
echo     - name: "hackathon-architecture-rule"
echo       type: "ARCHITECTURE"
echo       weight: 0.15
echo       config:
echo         keywords:
echo           positive:
echo             "分层": 10
echo             "模块化": 10
echo           negative:
echo             "硬编码": -10
echo.
echo     - name: "hackathon-quality-rule"
echo       type: "CODE_QUALITY"
echo       weight: 0.20
echo       config:
echo         keywords:
echo           positive:
echo             "单元测试": 20
echo             "注释": 10
echo           negative:
echo             "代码重复": -15
echo.
echo     - name: "hackathon-functionality-rule"
echo       type: "FUNCTIONALITY"
echo       weight: 0.25
echo       config:
echo         keywords:
echo           positive:
echo             "功能实现": 25
echo             "用户界面": 20
echo           negative:
echo             "功能缺失": -20
echo.
echo     - name: "hackathon-business-rule"
echo       type: "BUSINESS_VALUE"
echo       weight: 0.20
echo       config:
echo         keywords:
echo           positive:
echo             "用户价值": 20
echo             "创新性": 15
echo           negative:
echo             "概念不清": -15
echo.
echo     - name: "hackathon-test-rule"
echo       type: "TEST_COVERAGE"
echo       weight: 0.10
echo       config:
echo         keywords:
echo           positive:
echo             "测试用例": 20
echo           negative:
echo             "无测试": -25
echo.
echo # 报告配置
echo report:
echo   defaultFormat: "markdown"
echo   includeCharts: false
echo   includeMetrics: false
echo.
echo # 日志配置
echo logging:
echo   level: "WARN"
) > "%CONFIG_FILE%"

call :log_success "配置文件创建完成: %CONFIG_FILE%"
goto :eof

REM 运行测试
:run_tests
call :log_info "运行功能测试..."

java -jar "%JAR_NAME%" --help >nul 2>&1
if errorlevel 1 (
    call :log_error "工具运行测试失败"
    exit /b 1
)

call :log_success "工具运行测试通过"
goto :eof

REM 显示使用指南
:show_usage_guide
call :log_info "显示使用指南..."

echo.
echo ==================== 使用指南 ====================
echo.
echo 基本命令:
echo   # 查看帮助
echo   java -jar hackathon-reviewer-2.0.jar help
echo.
echo   # 评审单个项目
echo   java -jar hackathon-reviewer-2.0.jar review C:\path\to\project QUICK
echo.
echo   # 批量评审项目
echo   java -jar hackathon-reviewer-2.0.jar batch project1 project2 DETAILED
echo.
echo   # 查看排行榜
echo   java -jar hackathon-reviewer-2.0.jar leaderboard
echo.
echo 评审模式:
echo   • QUICK: 快速评审 (10秒) - 大规模初筛
echo   • DETAILED: 详细评审 (30秒) - 复赛评审
echo   • EXPERT: 专家评审 (60秒) - 决赛评审
echo.
echo 使用建议:
echo   1. 初赛阶段: 使用QUICK模式批量评审
echo   2. 复赛阶段: 使用DETAILED模式深度分析
echo   3. 决赛阶段: 使用EXPERT模式专业评审
echo.
echo 更多信息请查看: HACKATHON-REVIEW-GUIDE.md
echo.
goto :eof

REM 主函数
:main
echo  ========================================
echo  🏆 Hackathon AI 评审工具部署脚本
echo  ========================================

call :check_java
call :download_tool
call :configure_api_key
call :create_config
call :run_tests
call :show_usage_guide

call :log_success "部署完成！开始使用Hackathon AI评审工具吧！"
echo.
echo 🚀 快速开始:
echo java -jar %JAR_NAME% demo
goto :eof

REM 参数处理
if "%1"=="--help" goto show_script_help
if "%1"=="-h" goto show_script_help
if "%1"=="--version" goto show_script_version
if "%1"=="-v" goto show_script_version
goto main

:show_script_help
echo Hackathon AI 评审工具部署脚本 (Windows版)
echo.
echo 用法: %0 [选项]
echo.
echo 选项:
echo   --help, -h     显示此帮助信息
echo   --version, -v  显示版本信息
echo.
echo 无参数运行将执行完整部署流程
goto :eof

:show_script_version
echo Hackathon AI 评审工具部署脚本 v2.0.0 (Windows版)
goto :eof
