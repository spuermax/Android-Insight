# Changelog

## V0.4

- Focused the plugin on APK analysis, diagnosis, comparison, and optimization suggestions.
- Kept Android Insight read-only: it analyzes build artifacts but never modifies APK files.
- Kept per-variant `analyze<Variant>Apk` and `compare<Variant>Apk` tasks.
- Kept Console, JSON, and HTML reports for analysis and comparison.
- Added Gradle Plugin Portal metadata and a shaded plugin artifact for internal modules.
- Kept technical identifiers such as DEX and Method ID terms in English.

## V0.3

- Added `insight-diagnosis` as a pure Java diagnosis module.
- Added rule-driven `DiagnosisEngine`.
- Added Health Score and Optimization Potential.
- Added diagnosis issues with severity, category, evidence, and suggestions.
- Added default rules for DEX dominance, MultiDex count, DEX distribution imbalance, large dependency files, native share, resource share, and asset share.
- Integrated diagnosis into existing `analyze<Variant>Apk` tasks; no extra Gradle task is required.
- Added diagnosis sections to console, JSON, and HTML reports.
- Renamed aggregate `Method IDs` reporting to `DEX Method ID Entries` to avoid implying a unique application method count.
- Regenerated sample analysis and comparison reports from the real Chapter22 APKs.

## V0.2

- Added APK comparison model.
- Added `compare<Variant>Apk` Gradle tasks.
- Added configurable `comparisonApks` map.
- Added console, JSON and HTML comparison reports.
- Added category-level compressed-size deltas.
- Moved Android Insight tasks into their own Gradle task group.
- Added a real APK comparison sample.

## V0.1

- Added build-time APK analysis using AGP APK artifacts.
- Added APK/DEX/category analysis.
- Added console, JSON and HTML reports.
