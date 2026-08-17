# Android Insight

Android Insight 是一个面向 Android 构建产物的 Gradle 插件，目前提供 APK 分析、自动诊断、APK 对比，以及可选的 ReDex 自动优化。

## V0.4 能力

V0.4 在 V0.1~V0.3 的 Analyzer / Compare / Diagnosis 基础上新增 ReDex Optimizer：

- `analyze<Variant>Apk`：分析 APK，并输出中文诊断建议。
- `compare<Variant>Apk`：把当前 APK 与配置的候选 APK 做前后对比。
- `optimize<Variant>Apk`：调用外部 ReDex，生成独立优化 APK，再自动分析和对比。
- 原始 AGP APK 不会被替换；优化前后会通过 SHA-256 再确认原始 APK 是否保持不变。
- 支持内置 `strip` 和 `strip-interdex` 两种策略。
- 支持 `redexHome`，也支持分别配置 `redexScript` / `redexBinary`。
- 可选调用 ReDex 完成签名；优化完成后自动尝试 `zipalign -c` 与 `apksigner verify`。
- Console / HTML 报告以中文为主；Pass、DEX、Method ID、文件路径等技术标识保留英文。

## 模块

- `insight-analyzer-core`：APK / DEX 分析。
- `insight-diagnosis`：规则驱动的诊断引擎。
- `insight-report`：Console / JSON / HTML 报告。
- `insight-optimizer-redex`：ReDex 命令封装、内置 Pass 配置、Build Tools 验证。
- `insight-gradle-plugin`：Android Gradle Plugin 接入和 Variant Task 编排。

## 要求

- JDK 17
- 当前开发样例使用 Android Gradle Plugin 8.13.2 / Gradle 8.13
- 使用 Optimizer 时，需要本机已有可工作的 ReDex
- 如果通过 `redex.py` 运行，需要可用 Python 环境

## 本地 composite build 接入

宿主工程 `settings.gradle`：

    pluginManagement {
        includeBuild '../AndroidInsight'
        repositories {
            google()
            mavenCentral()
            gradlePluginPortal()
        }
    }

App 模块：

    plugins {
        id 'io.github.androidinsight'
    }

基础配置：

    androidInsight {
        enabled = true
        topFiles = 20
        jsonReport = true
        htmlReport = true
    }

## ReDex Optimizer 配置

推荐配置 `redexHome`，Android Insight 会在其中寻找 `redex.py` 和 `redex-all`：

    androidInsight {
        redex {
            enabled = true
            strategy = "strip-interdex"   // strip / strip-interdex
            redexHome = "/path/to/redex"
            pythonExecutable = "/path/to/python3.11"

            // 可选：Python 临时依赖目录，例如 packaging 所在目录
            pythonPath = "/path/to/extra/python/packages"

            // 可选：不设置时使用 Android Insight 内置 config
            // configFile = "/path/to/custom-redex.config"

            // 可选：传给 ReDex 的 ProGuard rules
            // proguardConfig = file("proguard-rules.pro").absolutePath

            // 默认 false。线上正式 keystore 不建议直接写入工程文件。
            sign = false
        }
    }

也可以不用 `redexHome`，分别配置：

    redexScript = "/path/to/redex.py"
    redexBinary = "/path/to/redex-all"

如果 Android Studio 的 Gradle 进程找不到 `python3.11`，请将 `pythonExecutable` 改为 `which python3.11` 返回的绝对路径。

## Task

分析：

    ./gradlew :app:analyzeDebugApk

比较：

    ./gradlew :app:compareDebugApk

自动优化：

    ./gradlew :app:optimizeDebugApk

Release variant 也会生成对应的：

    analyzeReleaseApk
    compareReleaseApk
    optimizeReleaseApk

## V0.4 优化产物

以 debug + `strip-interdex` 为例：

    app/build/outputs/android-insight/debug/
      app-debug-redex-strip-interdex.apk

报告：

    app/build/reports/android-insight/debug/optimize/
      optimization.html
      optimization.json
      redex.log
      redex-config/

报告会展示：

- 优化前 / 优化后 APK 大小
- 体积变化百分比
- DEX 数量变化
- Class Defs / DEX Method ID Entries 变化
- Health Score 变化
- 原始 APK 是否保持不变
- ZIP 对齐验证
- APK 签名验证
- 优化 APK 路径
- ReDex 日志路径

## 内置策略

### `strip`

只执行：

- `StripDebugInfoPass`

### `strip-interdex`

依次执行：

- `StripDebugInfoPass`
- `InterDexPass`

其中 InterDex 默认开启 `minimize_cross_dex_refs`。V0.4 暂时不开放大量激进 Pass，先保证优化链可解释、可比较、可回归。

## 重要安全边界

V0.4 不会把优化 APK 自动替换为 AGP 正常构建产物，也不会自动接入正式 release 发布流程。

正确流程仍然是：

    原始 APK
      -> Android Insight Analyze / Diagnose
      -> ReDex Optimize
      -> 自动 Compare / Validate
      -> 安装、启动、核心功能回归
      -> 再决定是否进入正式发布流程

正式签名建议后续使用 CI Secret / KMS / 企业签名服务，而不是把 keystore 密码提交到仓库。

## Diagnosis 默认阈值

当前规则仍保持透明、简单：

- DEX 占比 >= 60%：MEDIUM；>= 80%：HIGH
- DEX 数量 >= 5：MEDIUM；>= 10：HIGH
- Tiny DEX：<= 64 KiB 且 <= 最大 DEX 的 5%
- 大型依赖文件：Java 风格包路径且 >= 100 KiB
- Native 占比 >= 40%：MEDIUM；>= 60%：HIGH
- Resources 占比 >= 30%：MEDIUM；>= 50%：HIGH
- Assets 占比 >= 20%：MEDIUM；>= 40%：HIGH

这些只是诊断信号，不是通用性能真理。

## Method 指标说明

`DEX Method ID Entries` 是所有 DEX 的 `method_ids_size` 引用表项之和，不是 APK 的唯一方法数量。DEX 重新分包后，即使业务语义不变，该值也可能变化。
