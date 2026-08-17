# Changelog

## V0.4

- Added `insight-optimizer-redex` module.
- Added `optimize<Variant>Apk` tasks.
- Added ReDex `strip` and `strip-interdex` built-in strategies.
- Added `redexHome` convenience configuration plus explicit `redexScript` / `redexBinary` overrides.
- Added ReDex runner with safe command logging and password redaction.
- Added optional `PYTHONPATH` and TRACE environment configuration.
- Added SHA-256 check to confirm the original AGP APK is not overwritten.
- Added optional APK signing through ReDex and post-run `zipalign` / `apksigner` verification.
- Added optimization Console / JSON / HTML reports with Chinese user-facing text where appropriate.
- Kept technical identifiers such as Pass names, DEX and Method ID terms in English.
- Optimization output is written to a separate Android Insight directory and never replaces the normal AGP artifact in V0.4.

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
- Added a real comparison sample using Chapter22 original / StripDebugInfo / InterDex APKs.

## V0.1

- Added build-time APK analysis using AGP APK artifacts.
- Added APK/DEX/category analysis.
- Added console, JSON and HTML reports.
