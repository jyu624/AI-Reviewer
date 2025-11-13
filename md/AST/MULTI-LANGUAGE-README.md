# 🌟 多语言AST支持 - 快速参考

## 📋 支持的语言

| 语言 | 状态 | 示例命令 |
|------|------|---------|
| **Java** | ✅ 完整支持 | `new JavaParserAdapter()` |
| **Python** | ✅ 基础支持 | `new PythonParserAdapter()` |
| **JavaScript/TypeScript** | ✅ 基础支持 | `new JavaScriptParserAdapter()` |
| **Go** | 🚧 计划中 | - |
| **C/C++** | 🚧 计划中 | - |

---

## 🚀 30秒快速开始

### 方式1：自动选择解析器（推荐）

```java
ASTParserFactory factory = new ASTParserFactory();
CodeInsight insight = factory.parseProject(project);
```

### 方式2：指定解析器

```java
// Python
PythonParserAdapter pythonParser = new PythonParserAdapter();
CodeInsight insight = pythonParser.parseProject(pythonProject);

// JavaScript/TypeScript
JavaScriptParserAdapter jsParser = new JavaScriptParserAdapter();
CodeInsight insight = jsParser.parseProject(jsProject);

// Java
JavaParserAdapter javaParser = new JavaParserAdapter();
CodeInsight insight = javaParser.parseProject(javaProject);
```

---

## 🧪 运行示例

```bash
# 多语言示例
mvn exec:java -Dexec.mainClass="top.yumbo.ai.reviewer.MultiLanguageASTExample"

# Java示例（原有）
mvn exec:java -Dexec.mainClass="top.yumbo.ai.reviewer.ASTAnalysisExample"
```

---

## 📚 详细文档

- 📖 [多语言支持完整指南](MULTI-LANGUAGE-SUPPORT.md)
- 🚀 [AST快速开始](AST-QUICKSTART.md)
- 📊 [实施完成报告](MULTI-LANGUAGE-COMPLETION.md)

---

## 💡 核心特性

✅ **自动语言检测** - 工厂模式自动选择  
✅ **统一接口** - 所有语言相同的API  
✅ **可扩展** - 3步添加新语言  
✅ **高性能** - 正则表达式解析快速  
✅ **向后兼容** - 不影响现有功能

---

## 📊 支持的代码特性

| 特性 | Java | Python | JavaScript |
|------|------|--------|-----------|
| 类解析 | ✅ | ✅ | ✅ |
| 方法/函数 | ✅ | ✅ | ✅ |
| 继承关系 | ✅ | ✅ | ✅ |
| 装饰器/注解 | ✅ | ✅ | ✅ (TS) |
| 复杂度计算 | ✅ | ✅ | ✅ |
| 设计模式识别 | ✅ | ✅ | ✅ |
| 代码坏味道 | ✅ | ✅ | ✅ |

---

**更新时间**: 2025-11-13  
**版本**: v2.0  
**状态**: ✅ 生产就绪

