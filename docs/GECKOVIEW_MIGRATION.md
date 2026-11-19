# GeckoView migration — implementation and validation

## Current implementation

The browser surface in `app/src/main/res/layout/main.xml` is now
`org.mozilla.geckoview.GeckoView`. `MainActivity` owns a
`GeckoEngineController`; the production navigation path uses a
process-scoped `GeckoRuntime` and an Activity-owned `GeckoSession`.
The old WebView clients, WebChromeClient, CookieManager, WebStorage,
WebView JavaScript injection, and WebView download listener have been
removed from the browser Activity and layout.

Implemented integrations:

- Navigation, HTTPS-only enforcement, back/forward state, reload, stop,
  title/location callbacks, progress, scroll callbacks, fullscreen, and
  content-process crash reporting.
- GeckoView tracking protection, dynamic first-party isolation (dFPI),
  tracking-cookie purging, and query-parameter stripping.
- Origin-aware geolocation, camera, and microphone prompts, paired with
  Android runtime permission checks. Unsupported permission classes are
  denied rather than implicitly granted.
- GeckoView's built-in prompt delegate remains enabled for browser prompts,
  including JavaScript dialogs and file selection.
- Native Gecko Reader Mode via `about:reader`, JavaScript enable/disable,
  optional DNT header on navigations initiated by the app, and translation
  navigation.
- Download responses are streamed from GeckoView's original response body
  into Android's Downloads collection (MediaStore on Android 10+, public
  Downloads on older Android versions), with HTTPS validation and explicit
  user confirmation.
- Navigation history and back/forward state are managed by GeckoSession and included in its saved session state; no unused duplicate URL log is maintained.
- Site-specific and global browsing-data clearing use GeckoView's
  `StorageController`. Sessions are detached and closed before clearing,
  then reopened to avoid immediate data recreation by an open session.
- Mozilla Maven repository configured at the root build level; Java 17, GeckoView 160.0.20261116130455, compileSdk 37, Build Tools 36.0.0, and minSdk 26 (Android 8.0) are configured in the app module. Native packaging is restricted to ARM64 and ARMv7 phone ABIs; x86/x86_64 emulator targets are intentionally excluded. CI produces both APKs and an AAB for store-side ABI delivery. The project uses AGP 9.1.1 / Gradle 9.3.1 because GeckoView's AndroidX Core 1.19.1 dependency requires AGP 9.1 and API 37. Jetifier is disabled because the app's dependencies are AndroidX-native; Gradle concurrency is capped and the heap is configured for the large GeckoView AAR.

## Security behavior

- The Gecko runtime uses HTTPS-only mode. Top-level HTTP requests are denied;
  the user can explicitly retry the HTTPS form.
- Certificate/TLS validation is not bypassed.
- The app does not grant site permissions without a user action and Android
  permission approval where required.
- Third-party cookies use Gecko's dynamic first-party isolation. The previous
  WebView-specific third-party-cookie toggle was removed because it cannot
  truthfully control Gecko's runtime cookie policy.
- Site-data clearing is host-scoped. GeckoView documents that
  `clearDataFromHost` does not clear data owned by third-party origins; this
  behavior is not represented as a complete cross-origin purge.
- The DNT preference applies to navigations initiated by the app; it is not
  represented as a guarantee for every internal subresource or page-driven
  navigation.

## Known limitations requiring validation

- The app remains a single-session browser; this migration does not add tabs.
- The existing Snav UI has no dedicated history-management screen; this migration keeps GeckoSession back/forward history without inventing a second history UI.
- Session state is not persisted across process death. A process restart
  starts a new browser session.
- GeckoView's built-in prompt UI is used for JavaScript dialogs and file
  selection; it needs runtime verification on supported Android versions.
- The broad GeckoView R8 keep rule is retained until a release/R8 build can
  prove which JNI/reflective entry points are safe to narrow. This may reduce
  code shrinking and should be revisited with measured release outputs. The
  GeckoView AAR is approximately 233 MB before packaging; final APK/AAB size
  and runtime memory must be measured rather than inferred from the AAR size.

## Validation status

The current branch head at the time of validation is
`cfeee7ef78a48f2cd5e77d63aed3b8f67b720a35` (`feature/geckoview-engine`).

GitHub Actions run
[44564995458](https://github.com/souhelin/Snav-Browser/actions/runs/44564995458)
completed successfully on 2026-11-19. The job executed the following
operations successfully:

- `:app:testDebugUnitTest` — Gradle reported `NO-SOURCE`; no unit tests
  currently exist, so this is not evidence of test coverage.
- `:app:assembleDebug` — successful.
- `:app:assembleRelease` — successful with R8 and resource shrinking.
- `:app:bundleRelease` — successful.
- APK/AAB artifact upload — successful; three output files were uploaded.

The build log reports deprecated Gradle features that will be incompatible
with Gradle 10. The exact deprecation sources have not yet been isolated.
This does not fail the current Gradle 9.3.1 build, but should be resolved
before upgrading to Gradle 10.

No emulator or physical-device smoke test has been performed. Runtime behavior
for navigation, prompts, Android permissions, downloads, site-data clearing,
fullscreen, process crashes, and Activity recreation therefore remains
unverified. The artifact upload proves packaging completed, not that these
flows work on a device. Final APK/AAB sizes and runtime memory use should be
measured on the delivered artifacts and representative devices.

Pull request #6 is closed and was not merged. The GeckoView implementation
remains on `feature/geckoview-engine`; do not treat the closed PR as a merge
or as proof of runtime validation. A successful CI build is a necessary
milestone, but the migration is not release-validated until device smoke
tests and real test coverage are added.
