# AST解析器重构指南

## 📋 重构目标
将所有AST解析器适配器中的重复代码提取到 `AbstractASTParser` 基类中，消除代码重复。

## ✅ 已完成
1. ✅ 在 `AbstractASTParser` 中添加了公共方法：
   - `buildProjectStructure()` - 构建项目结构
   - `buildDependencyGraph()` - 构建依赖图
   - `calculateStatistics()` - 计算统计信息
   - `calculateComplexityMetrics()` - 计算复杂度指标
   - `detectCodeSmells()` - 检测代码坏味道
   - `detectDesignPatterns()` - 检测设计模式
   - `detectSingletonPattern()` - 检测单例模式（可覆盖）
   - `detectFactoryPattern()` - 检测工厂模式
   - `detectObserverPattern()` - 检测观察者模式
   - `detectDecoratorPattern()` - 检测装饰器模式
   - `findRootPackage()` - 查找根包

2. ✅ 已重构 `PythonParserAdapter.java`

## 🔧 需要重构的文件
以下文件需要删除重复方法并使用基类方法：

1. **JavaParserAdapter.java** - 需要删除的private方法：
   - `buildProjectStructure()`
   - `findRootPackage()`
   - `buildDependencyGraph()`
   - `calculateStatistics()`
   - `detectDesignPatterns()`
   - `detectSingletonPattern()`
   - `detectFactoryPattern()`
   - `calculateComplexityMetrics()`
   - `detectCodeSmells()`

2. **JavaScriptParserAdapter.java** - 需要删除的private方法：
   - `buildProjectStructure()`
   - `buildDependencyGraph()`
   - `detectDesignPatterns()`
   - `detectSingletonPattern()`
   - `detectFactoryPattern()`
   - `calculateComplexityMetrics()`
   - `detectCodeSmells()`

3. **CppParserAdapter.java** - 需要删除的private方法：
   - `buildProjectStructure()`
   - `buildDependencyGraph()`
   - `calculateStatistics()`
   - `detectDesignPatterns()`
   - `detectSingletonPattern()`
   - `detectFactoryPattern()`
   - `calculateComplexityMetrics()`
   - `detectCodeSmells()`

4. **GoParserAdapter.java** - 需要删除的private方法：
   - `buildProjectStructure()`
   - `buildDependencyGraph()`
   - `detectDesignPatterns()`
   - `detectSingletonPattern()`
   - `calculateComplexityMetrics()`
   - `detectCodeSmells()`

## 📝 重构步骤（针对每个解析器）

### 步骤1：删除重复方法
找到并删除所有与基类同名的 private 方法实现。这些方法现在已在基类中提供。

### 步骤2：覆盖语言特定的方法（可选）
如果某个语言有特殊的设计模式检测逻辑，可以使用 `@Override` 覆盖基类方法：

```java
/**
 * Python特定的单例模式检测
 */
@Override
protected void detectSingletonPattern(List<ClassStructure> classes, DesignPatterns patterns) {
    // Python使用 __new__ 方法实现单例
    DesignPattern singletonPattern = DesignPattern.builder()
        .type(DesignPattern.PatternType.SINGLETON)
        .name("单例模式")
        .build();

    for (ClassStructure cls : classes) {
        boolean hasNewMethod = cls.getMethods().stream()
            .anyMatch(m -> m.getMethodName().equals("__new__"));

        if (hasNewMethod) {
            singletonPattern.addInstance(cls.getClassName());
        }
    }

    if (singletonPattern.getInstanceCount() > 0) {
        singletonPattern.setConfidence(0.7);
        patterns.addPattern(singletonPattern);
    }
}
```

### 步骤3：验证编译
```bash
mvn -DskipTests=true clean compile
```

## 📊 重构效果

### 代码减少量估算
- **AbstractASTParser**: +350行（新增公共代码）
- **PythonParserAdapter**: -280行
- **JavaParserAdapter**: -350行（预计）
- **JavaScriptParserAdapter**: -300行（预计）
- **CppParserAdapter**: -320行（预计）
- **GoParserAdapter**: -280行（预计）

**总计**: 净减少约 **1,180行重复代码**

### 维护性提升
1. ✅ 单一职责：公共逻辑集中在基类
2. ✅ 易于维护：修复bug只需改一处
3. ✅ 易于扩展：新增语言解析器只需实现核心解析逻辑
4. ✅ 一致性：所有解析器使用相同的算法

## 🎯 下一步行动
建议按以下顺序重构：
1. ✅ PythonParserAdapter - 已完成
2. GoParserAdapter - 最简单（方法最少）
3. CppParserAdapter
4. JavaScriptParserAdapter
5. JavaParserAdapter - 最复杂（方法最多）

## ⚠️ 注意事项
1. 删除 private 方法时，确保该方法与基类方法逻辑完全一致
2. 如果子类方法有特殊逻辑，应保留并添加 `@Override` 注解
3. 删除方法后立即编译验证，确保没有破坏功能
4. 特别关注方法的访问修饰符（基类使用 protected，子类不能用 private）

## 🐛 已知问题修复
- ✅ 移除了 `DATA_CLASS` 坏味道检测（该枚举值不存在）
- ✅ 所有基类方法使用 protected 访问修饰符以允许子类覆盖

