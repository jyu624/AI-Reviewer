#!/bin/bash

# Hackathon AI 评审工具部署脚本
# 用于快速部署和配置黑客松AI评审环境

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
JAR_NAME="hackathon-reviewer-2.0.jar"
CONFIG_FILE="hackathon-config.yaml"
DOWNLOAD_URL="https://github.com/jinhua10/ai-reviewer/releases/download/v2.0/${JAR_NAME}"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Java环境
check_java() {
    log_info "检查Java环境..."

    if ! command -v java &> /dev/null; then
        log_error "Java未安装，请先安装JDK 17+"
        exit 1
    fi

    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        log_error "需要JDK 17+，当前版本: $JAVA_VERSION"
        exit 1
    fi

    log_success "Java环境检查通过 (JDK $JAVA_VERSION)"
}

# 下载工具
download_tool() {
    log_info "下载Hackathon AI评审工具..."

    if [ -f "$JAR_NAME" ]; then
        log_warning "文件已存在，跳过下载"
        return
    fi

    if command -v wget &> /dev/null; then
        wget -O "$JAR_NAME" "$DOWNLOAD_URL"
    elif command -v curl &> /dev/null; then
        curl -L -o "$JAR_NAME" "$DOWNLOAD_URL"
    else
        log_error "需要wget或curl来下载文件"
        exit 1
    fi

    if [ ! -f "$JAR_NAME" ]; then
        log_error "下载失败"
        exit 1
    fi

    log_success "工具下载完成: $JAR_NAME"
}

# 配置API密钥
configure_api_key() {
    log_info "配置API密钥..."

    if [ -z "$DEEPSEEK_API_KEY" ]; then
        echo -n "请输入DeepSeek API密钥: "
        read -s API_KEY
        echo

        if [ -z "$API_KEY" ]; then
            log_error "API密钥不能为空"
            exit 1
        fi

        # 设置环境变量
        export DEEPSEEK_API_KEY="$API_KEY"
        echo "export DEEPSEEK_API_KEY=\"$API_KEY\"" >> ~/.bashrc
        log_success "API密钥已配置并保存到环境变量"
    else
        log_success "API密钥已配置"
    fi
}

# 创建配置文件
create_config() {
    log_info "创建配置文件..."

    if [ -f "$CONFIG_FILE" ]; then
        log_warning "配置文件已存在，跳过创建"
        return
    fi

    cat > "$CONFIG_FILE" << 'EOF'
# Hackathon AI 评审工具配置文件
# 专为黑客松比赛优化的配置

# AI服务配置
aiService:
  provider: "deepseek"
  apiKey: "${DEEPSEEK_API_KEY}"
  baseUrl: "https://api.deepseek.com/v1"
  model: "deepseek-chat"
  maxTokens: 2000
  temperature: 0.3
  timeout: 15000
  maxRetries: 2
  maxConcurrency: 5

# 缓存配置
cache:
  enabled: true
  type: "file"
  ttlHours: 2
  maxSize: 500

# 文件扫描配置
fileScan:
  includePatterns:
    - "*.java"
    - "*.py"
    - "*.js"
    - "*.ts"
    - "*.html"
    - "*.css"
  excludePatterns:
    - "*.log"
    - "node_modules/"
    - "*.git*"
    - ".DS_Store"
  maxFileSize: 512
  maxFilesCount: 100

# 分析配置
analysis:
  analysisDimensions:
    - "architecture"
    - "code_quality"
    - "technical_debt"
    - "functionality"
    - "business_value"
    - "test_coverage"
  dimensionWeights:
    architecture: 0.15
    code_quality: 0.20
    technical_debt: 0.10
    functionality: 0.25
    business_value: 0.20
    test_coverage: 0.10
  batchSize: 5
  maxConcurrentBatches: 2
  batchTimeout: 200000

# 评分规则配置
scoring:
  rules:
    - name: "hackathon-architecture-rule"
      type: "ARCHITECTURE"
      weight: 0.15
      config:
        keywords:
          positive:
            "分层": 10
            "模块化": 10
          negative:
            "硬编码": -10

    - name: "hackathon-quality-rule"
      type: "CODE_QUALITY"
      weight: 0.20
      config:
        keywords:
          positive:
            "单元测试": 20
            "注释": 10
          negative:
            "代码重复": -15

    - name: "hackathon-functionality-rule"
      type: "FUNCTIONALITY"
      weight: 0.25
      config:
        keywords:
          positive:
            "功能实现": 25
            "用户界面": 20
          negative:
            "功能缺失": -20

    - name: "hackathon-business-rule"
      type: "BUSINESS_VALUE"
      weight: 0.20
      config:
        keywords:
          positive:
            "用户价值": 20
            "创新性": 15
          negative:
            "概念不清": -15

    - name: "hackathon-test-rule"
      type: "TEST_COVERAGE"
      weight: 0.10
      config:
        keywords:
          positive:
            "测试用例": 20
          negative:
            "无测试": -25

# 报告配置
report:
  defaultFormat: "markdown"
  includeCharts: false
  includeMetrics: false

# 日志配置
logging:
  level: "WARN"
EOF

    log_success "配置文件创建完成: $CONFIG_FILE"
}

# 运行测试
run_tests() {
    log_info "运行功能测试..."

    if java -jar "$JAR_NAME" --help &> /dev/null; then
        log_success "工具运行测试通过"
    else
        log_error "工具运行测试失败"
        exit 1
    fi
}

# 显示使用指南
show_usage_guide() {
    log_info "显示使用指南..."

    cat << 'EOF'

🎯 Hackathon AI 评审工具使用指南

📋 基本命令:
  # 查看帮助
  java -jar hackathon-reviewer-2.0.jar help

  # 评审单个项目
  java -jar hackathon-reviewer-2.0.jar review /path/to/project QUICK

  # 批量评审项目
  java -jar hackathon-reviewer-2.0.jar batch project1 project2 DETAILED

  # 查看排行榜
  java -jar hackathon-reviewer-2.0.jar leaderboard

📊 评审模式:
  • QUICK: 快速评审 (10秒) - 大规模初筛
  • DETAILED: 详细评审 (30秒) - 复赛评审
  • EXPERT: 专家评审 (60秒) - 决赛评审

💡 使用建议:
  1. 初赛阶段: 使用QUICK模式批量评审
  2. 复赛阶段: 使用DETAILED模式深度分析
  3. 决赛阶段: 使用EXPERT模式专业评审

📚 更多信息请查看: HACKATHON-REVIEW-GUIDE.md

EOF
}

# 主函数
main() {
    echo "🏆 Hackathon AI 评审工具部署脚本"
    echo "=================================="

    check_java
    download_tool
    configure_api_key
    create_config
    run_tests
    show_usage_guide

    log_success "部署完成！开始使用Hackathon AI评审工具吧！"
    echo
    echo "🚀 快速开始:"
    echo "java -jar $JAR_NAME demo"
}

# 参数处理
case "${1:-}" in
    "--help"|"-h")
        echo "Hackathon AI 评审工具部署脚本"
        echo
        echo "用法: $0 [选项]"
        echo
        echo "选项:"
        echo "  --help, -h    显示此帮助信息"
        echo "  --version, -v 显示版本信息"
        echo
        echo "无参数运行将执行完整部署流程"
        exit 0
        ;;
    "--version"|"-v")
        echo "Hackathon AI 评审工具部署脚本 v2.0.0"
        exit 0
        ;;
    *)
        main "$@"
        ;;
esac
