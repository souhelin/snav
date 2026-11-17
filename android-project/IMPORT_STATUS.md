# Snav Browser Android project import

This directory contains the Gradle configuration extracted from the supplied `SnavBrowser.zip` project.

## Current status

- Gradle root and app-module configuration imported.
- Original package: `com.snav.browser`.
- Version declared by the source: `0.2.0` (versionCode `1`).
- The repository already contains the Sketchware `.swb` project in `beta/`.
- This is **not yet a complete, buildable Android source import**: the ZIP also contains Java sources, layouts, drawable assets, fonts and launcher resources that have not been transferred into this branch.

## Verification blockers found in the supplied manifest

The original `AndroidManifest.xml` refers to `.SketchApplication` and `.DebugActivity`, but neither class is present among the three Java files in the ZIP (`MainActivity`, `SketchwareUtil`, and `FileUtil`). Those references need to be resolved before treating the project as build-ready. No successful Gradle build or APK validation has been performed.

## Next import step

Transfer the remaining `app/src/main` source tree from the supplied ZIP, then reconcile the missing manifest classes and run a clean Android Gradle build. Do not merge this scaffold as a finished app.
