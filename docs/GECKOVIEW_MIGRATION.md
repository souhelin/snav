# GeckoView migration — implementation notes

## Dependency and runtime

Snav now resolves GeckoView from Mozilla's Maven repository and pins the stable
artifact version declared in `app/build.gradle`. Java source/target compatibility
is set to Java 17 as required by current GeckoView consumer documentation.

`GeckoEngineController` provides a process-scoped `GeckoRuntime` and an
Activity/view-scoped `GeckoSession`. It exposes the core operations needed by
the browser UI: load, reload, stop, back, and close.

## Important migration boundary

This commit does **not** switch the production browser surface yet. The existing
`MainActivity` still contains Android WebView-specific callbacks and APIs for
navigation interception, TLS errors, downloads, JavaScript injection, file
chooser, geolocation, and runtime permissions. Replacing the XML WebView alone
would break these paths.

The next integration change must migrate those responsibilities to GeckoSession
delegates (NavigationDelegate, ProgressDelegate, ContentBlockingDelegate,
PermissionDelegate, PromptDelegate, and ContentDelegate), then replace the
WebView view in `main.xml` and remove the `android.webkit` dependency from
`MainActivity`. Do not ship a mixed or fallback engine if the product decision
is GeckoView-only.

## Security and release checks

- Keep TLS errors fail-closed; never add a certificate-error bypass.
- Reimplement third-party tracker blocking using Gecko content-blocking APIs
  rather than WebView request interception.
- Grant camera, microphone, location, and file access only after explicit
  origin-aware user consent.
- Keep downloads constrained to safe destination handling and user-initiated
  actions.
- Verify release shrinking against the GeckoView AAR and inspect final APK/AAB
  size and ABI contents.
- Update GeckoView regularly for Mozilla security fixes.

## Validation status

No Android build or device test was run by the GitHub editing operation. The
current MainActivity remains WebView-backed; this is preparatory integration,
not a completed engine replacement.
