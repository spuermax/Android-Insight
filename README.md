# Android Insight

**Android APK Analysis & Diagnosis Gradle Plugin**

Android Insight is a Gradle plugin for analyzing Android APK build artifacts, diagnosing packaging issues, comparing APKs, and generating optimization suggestions.

Android Insight 是一个面向 Android APK 构建产物的 Gradle 插件，提供 APK 分析、自动诊断、APK 对比以及优化建议。

Android Insight 只读取和分析 APK，不会修改、替换或重新签名 APK。

## V0.4 能力

- APK 文件结构与体积分析
- DEX 数量、体积、Class Defs 和 Method ID Entries 分析
- Resources、Assets 和 Native Library 分析
- 大文件排名与分类统计
- 规则驱动的 Diagnosis Engine
- Health Score 和 Optimization Potential
- APK 基线/候选产物对比
- Console、JSON 和 HTML 报告
- 针对 R8、依赖、DEX 布局、资源、Assets 和 Native Library 的优化建议

## 模块

- `insight-analyzer-core`：APK / DEX 分析模型与解析器。
- `insight-diagnosis`：规则驱动的诊断引擎与优化建议。
- `insight-report`：分析和对比的 Console / JSON / HTML 报告。
- `insight-gradle-plugin`：Android Gradle Plugin 接入和 Variant Task 编排。

## 要求

- JDK 17
- Gradle 8.13
- Android Gradle Plugin 8.13.2

## 使用

### Gradle Plugin Portal

在 Android App 模块中应用插件：

```groovy
plugins {
    id 'io.github.spuermax.androidinsight' version '0.4.0'
}
```

### 本地 composite build

宿主工程 `settings.gradle`：

```groovy
pluginManagement {
    includeBuild '../AndroidInsight'
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

App 模块：

```groovy
plugins {
    id 'io.github.spuermax.androidinsight'
}
```

## 配置

```groovy
androidInsight {
    enabled = true
    topFiles = 20
    jsonReport = true
    htmlReport = true

    comparisonApks = [
        releaseCandidate: '/path/to/candidate.apk'
    ]
}
```

- `enabled`：是否执行 Android Insight Task，默认 `true`。
- `topFiles`：报告中展示的最大文件数量，默认 `20`。
- `jsonReport`：是否生成 JSON 报告，默认 `true`。
- `htmlReport`：是否生成 HTML 报告，默认 `true`。
- `comparisonApks`：候选 APK 标签与文件路径的映射。

## Task

分析 Debug APK：

```bash
./gradlew :app:analyzeDebugApk
```

对比 Debug APK：

```bash
./gradlew :app:compareDebugApk
```

Release Variant 对应生成：

```text
analyzeReleaseApk
compareReleaseApk
```

## 报告输出

分析报告：

```text
app/build/reports/android-insight/<variant>/
  <apk-base-name>.json
  <apk-base-name>.html
```

对比报告：

```text
app/build/reports/android-insight/<variant>/compare/
  comparison.json
  comparison.html
```

Console 报告会直接输出在 Gradle 日志中。

## Diagnosis 默认阈值

- DEX 占比 `>= 60%`：MEDIUM；`>= 80%`：HIGH
- DEX 数量 `>= 5`：MEDIUM；`>= 10`：HIGH
- Tiny DEX：`<= 64 KiB` 且 `<= 最大 DEX 的 5%`
- 大型依赖文件：Java 风格包路径且 `>= 100 KiB`
- Native 占比 `>= 40%`：MEDIUM；`>= 60%`：HIGH
- Resources 占比 `>= 30%`：MEDIUM；`>= 50%`：HIGH
- Assets 占比 `>= 20%`：MEDIUM；`>= 40%`：HIGH

这些阈值是用于定位问题的诊断信号，不是适用于所有项目的性能真理。

## Method 指标说明

`DEX Method ID Entries` 是所有 DEX 的 `method_ids_size` 引用表项之和，不是 APK 的唯一方法数量。DEX 重新分包后，即使业务语义不变，该值也可能变化。

## 建议工作流

```text
APK Build
  -> Analyze
  -> Diagnose
  -> Report
  -> Developer Optimize
  -> Compare
```

Android Insight V0.4 负责分析、诊断和对比，实际优化由开发者在宿主工程中完成。
