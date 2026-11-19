# GeckoView migration — implementation notes

## Current branch state

This branch contains the GeckoView dependency, Mozilla Maven repository,
process-scoped runtime/session controller, Gecko navigation/progress/content
callbacks, an HTTPS-only runtime policy, third-party cookie isolation, cookie
purging, and query-parameter stripping.

**The production browser has not yet been switched to GeckoView.** The layout
still declares a WebView and MainActivity still contains WebView clients,
WebChromeClient, CookieManager, WebStorage, JavaScript injection, permission
callbacks, and the legacy download listener. The controller is not wired into
MainActivity. Do not merge this branch or ship an APK as a GeckoView browser yet.

## Nine integration workstreams still required

1. **Browser surface:** replace the WebView element in `main.xml` with
   `org.mozilla.geckoview.GeckoView`, preserve sizing, accessibility, and
   existing Snav UI.
2. **Activity ownership:** instantiate one controller per browser surface,
   connect its listener, and close/recover sessions according to Activity and
   content-process lifecycle.
3. **Navigation:** migrate URL loading, back/forward, reload, stop, redirect,
   external intents, and HTTPS-only handling to GeckoSession delegates.
4. **Progress and metadata:** migrate page progress, URL/title updates, error
   reporting, connection information, and back-state updates.
5. **Tracking and cookies:** remove WebView request interception and use
   Gecko content blocking and cookie behavior. Keep the existing preference
   controls truthful; do not show a toggle that does not affect the engine.
6. **Permissions and prompts:** migrate location, camera, microphone, JavaScript
   prompts, file chooser, and fullscreen handling. Deny by default and require
   explicit, origin-aware consent plus Android runtime permission.
7. **Downloads and storage:** migrate external responses to a safe download
   pipeline, preserve user confirmation and HTTPS checks, and implement clear
   history/cache/site-data actions with Gecko storage APIs.
8. **Browser tools:** migrate reader mode and JavaScript controls to supported
   Gecko APIs or a narrowly scoped WebExtension/content script. Do not retain
   WebView-only JavaScript injection or silently remove these features.
9. **Validation and release:** run debug and release builds, R8/resource
   shrinking, tests, emulator/device smoke tests, process-death recovery, and
   inspect APK/AAB size and supported ABIs.

## Security decisions

- Gecko runtime is configured for HTTPS-only connections; HTTP navigation is
  rejected by the controller.
- Third-party cookies use dynamic first-party isolation; tracking cookies are
  purged when eligible and query parameters are stripped.
- Web-origin permissions are not granted implicitly. The Activity must add an
  explicit permission UX before enabling camera, microphone, or location.
- Never bypass certificate validation or weaken TLS to make a site load.
- Do not reintroduce a WebView fallback: the product decision is GeckoView-only.

## Validation status

The GitHub edits are source changes only. No Gradle build, R8 run, emulator
test, or physical-device test has been executed in this environment. A CI
workflow is added on this branch to compile the debug variant on pushes and
pull requests. A green CI build is necessary but not sufficient for release.
