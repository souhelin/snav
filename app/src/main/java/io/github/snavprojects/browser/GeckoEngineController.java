package io.github.snavprojects.browser;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import org.mozilla.geckoview.ContentBlocking;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.StorageController;
import org.mozilla.geckoview.WebResponse;
import org.mozilla.geckoview.WebRequestError;

import java.util.List;
import java.util.Map;

/** Owns the process-wide Gecko runtime and the browser's active Gecko session. */
public final class GeckoEngineController {
    public interface Listener {
        default void onPageStarted(String uri) {}
        default void onPageStopped(boolean success) {}
        default void onPageLoadError(String uri) {}
        default void onProgressChanged(int progress) {}
        default void onLocationChanged(String uri) {}
        default void onTitleChanged(String title) {}
        default void onCanGoBackChanged(boolean canGoBack) {}
        default void onCanGoForwardChanged(boolean canGoForward) {}
        default void onInsecureNavigationBlocked(String uri, String sourceUri) {}
        default void onExternalNavigationRequested(String uri) {}
        default void onNewTabRequested(String uri) {}
        default void onExtensionInstallRequested(String uri) {}
        default void onExternalResponse(WebResponse response) {}
        default void onContextMenu(int x, int y, GeckoSession.ContentDelegate.ContextElement element) {}
        default void onFullScreenChanged(boolean fullScreen) {}
        default void onContentProcessCrashed() {}
        default void onScrollChanged(int scrollY) {}
    }

    private static GeckoRuntime runtime;

    private final GeckoView view;
    private final GeckoSession session;
    private final Handler mainHandler;
    private final HandlerThread storageThread;
    private final Handler storageHandler;
    private Listener listener;
    private boolean closed;
    private boolean canGoBack;
    private boolean canGoForward;
    private boolean storageOperationInProgress;
    private String currentUri;
    private String loadingUri;
    private GeckoSession.SessionState sessionState;

    public GeckoEngineController(Context context, GeckoView view, Listener listener,
                                 boolean javaScriptEnabled, boolean trackingProtectionEnabled) {
        if (context == null || view == null) {
            throw new IllegalArgumentException("Context and GeckoView are required");
        }
        this.view = view;
        this.listener = listener;

        synchronized (GeckoEngineController.class) {
            if (runtime == null) {
                ContentBlocking.Settings contentBlocking =
                    new ContentBlocking.Settings.Builder()
                        .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_FIRST_PARTY_AND_ISOLATE_OTHERS)
                        .antiTracking(ContentBlocking.AntiTracking.DEFAULT)
                        .enhancedTrackingProtectionLevel(ContentBlocking.EtpLevel.DEFAULT)
                        .enhancedTrackingProtectionCategory(ContentBlocking.EtpCategory.STANDARD)
                        .safeBrowsing(ContentBlocking.SafeBrowsing.DEFAULT)
                        .cookiePurging(true)
                        .queryParameterStrippingEnabled(true)
                        .queryParameterStrippingPrivateBrowsingEnabled(true)
                        .build();

                GeckoRuntimeSettings settings = new GeckoRuntimeSettings.Builder()
                    .contentBlocking(contentBlocking)
                    .javaScriptEnabled(javaScriptEnabled)
                    .aboutConfigEnabled(false)
                    .remoteDebuggingEnabled(false)
                    .allowInsecureConnections(GeckoRuntimeSettings.HTTPS_ONLY)
                    .build();
                runtime = GeckoRuntime.create(context.getApplicationContext(), settings);
                runtime.setWebNotificationDelegate(new WebNotificationBridge(context));
            }
        }

        session = new GeckoSession();
        session.setPromptDelegate(null); // Use GeckoView's built-in secure prompt and file-picker UI.
        session.getSettings().setUseTrackingProtection(trackingProtectionEnabled);
        session.getSettings().setAllowJavascript(javaScriptEnabled);

        session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override
            public void onTitleChange(GeckoSession currentSession, String title) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onTitleChanged(title);
            }

            @Override
            public void onContextMenu(GeckoSession currentSession, int x, int y, GeckoSession.ContentDelegate.ContextElement element) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onContextMenu(x, y, element);
            }

            @Override
            public void onExternalResponse(GeckoSession currentSession, WebResponse response) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onExternalResponse(response);
                else if (response != null && response.body != null) {
                    try { response.body.close(); } catch (Exception ignored) {}
                }
            }

            @Override
            public void onFullScreen(GeckoSession currentSession, boolean fullScreen) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onFullScreenChanged(fullScreen);
            }

            @Override
            public void onCrash(GeckoSession currentSession) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onContentProcessCrashed();
            }
        });

        session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
            @Override
            public void onPageStart(GeckoSession currentSession, String uri) {
                loadingUri = uri;
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onPageStarted(uri);
            }

            @Override
            public void onPageStop(GeckoSession currentSession, boolean success) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onPageStopped(success);
            }

            @Override
            public void onProgressChange(GeckoSession currentSession, int progress) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onProgressChanged(progress);
            }

            @Override
            public void onSessionStateChange(GeckoSession currentSession, GeckoSession.SessionState state) {
                sessionState = state == null ? null : new GeckoSession.SessionState(state);
            }
        });

        session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Override
            public GeckoResult<AllowOrDeny> onLoadRequest(
                GeckoSession currentSession,
                GeckoSession.NavigationDelegate.LoadRequest request
            ) {
                String uri = request == null ? null : request.uri;
                if (uri == null) return GeckoResult.deny();
                Uri parsed = Uri.parse(uri);
                if ("https".equalsIgnoreCase(parsed.getScheme())
                    && parsed.getHost() != null && parsed.getUserInfo() == null) {
                    String path = parsed.getPath();
                    if (request.hasUserGesture && path != null
                        && path.toLowerCase(java.util.Locale.ROOT).endsWith(".xpi")) {
                        Listener callback = GeckoEngineController.this.listener;
                        if (!closed && callback != null) callback.onExtensionInstallRequested(uri);
                        return GeckoResult.deny();
                    }
                    if (request.target == GeckoSession.NavigationDelegate.TARGET_WINDOW_NEW) {
                        Listener callback = GeckoEngineController.this.listener;
                        if (request.hasUserGesture && !closed && callback != null)
                            callback.onNewTabRequested(uri);
                        return GeckoResult.deny();
                    }
                    return GeckoResult.allow();
                }
                if (uri.equals("about:blank")) return GeckoResult.allow();
                if (uri.regionMatches(true, 0, "about:reader?", 0, 13)) {
                    String readerTarget = readerTargetFromUri(uri);
                    Uri readerUri = readerTarget == null ? null : Uri.parse(readerTarget);
                    if (readerUri != null && "https".equalsIgnoreCase(readerUri.getScheme())
                        && readerUri.getHost() != null && readerUri.getUserInfo() == null) {
                        return GeckoResult.allow();
                    }
                    return GeckoResult.deny();
                }
                Listener callback = GeckoEngineController.this.listener;
                if ("http".equalsIgnoreCase(parsed.getScheme())) {
                    if (!closed && callback != null) callback.onInsecureNavigationBlocked(uri, request.triggerUri);
                } else if (!closed && callback != null) {
                    callback.onExternalNavigationRequested(uri);
                }
                return GeckoResult.deny();
            }

            @Override
            public GeckoResult<String> onLoadError(GeckoSession currentSession, String uri, WebRequestError error) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null && uri != null && uri.equals(loadingUri)) {
                    callback.onPageLoadError(uri);
                }
                // Never bypass TLS errors or load an insecure error page.
                return null;
            }

            @Override
            public void onLocationChange(
                GeckoSession currentSession,
                String uri,
                List<GeckoSession.PermissionDelegate.ContentPermission> permissions,
                Boolean hasUserGesture
            ) {
                currentUri = uri;
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onLocationChanged(uri);
            }

            @Override
            public void onCanGoBack(GeckoSession currentSession, boolean value) {
                canGoBack = value;
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onCanGoBackChanged(value);
            }

            @Override
            public void onCanGoForward(GeckoSession currentSession, boolean value) {
                canGoForward = value;
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onCanGoForwardChanged(value);
            }
        });

        session.setScrollDelegate(new GeckoSession.ScrollDelegate() {
            @Override
            public void onScrollChanged(GeckoSession currentSession, int scrollX, int scrollY) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onScrollChanged(scrollY);
            }
        });

        // The Activity installs the origin-aware PermissionDelegate before loading content.
        session.open(runtime);
        view.setSession(session);
        mainHandler = new Handler(Looper.getMainLooper());
        storageThread = new HandlerThread("SnavGeckoStorage");
        storageThread.start();
        storageHandler = new Handler(storageThread.getLooper());
    }

    public static GeckoRuntime getRuntime() {
        synchronized (GeckoEngineController.class) {
            if (runtime == null) throw new IllegalStateException("GeckoRuntime has not been initialized");
            return runtime;
        }
    }

    public GeckoSession getSession() { ensureOpen(); return session; }
    public GeckoSession.SessionState getSessionState() {
        ensureOpen();
        return sessionState == null ? null : new GeckoSession.SessionState(sessionState);
    }

    public void flushSessionState() {
        ensureOpen();
        session.flushSessionState();
    }

    public void restoreState(GeckoSession.SessionState state, String expectedUri) {
        ensureOpen();
        if (state == null) return;
        loadingUri = expectedUri;
        sessionState = new GeckoSession.SessionState(state);
        session.restoreState(sessionState);
    }

    public String getCurrentUri() { ensureOpen(); return currentUri; }
    public boolean canGoBack() { ensureOpen(); return canGoBack; }
    public boolean canGoForward() { ensureOpen(); return canGoForward; }

    public void setListener(Listener listener) {
        ensureOpen();
        this.listener = listener;
    }

    private static String readerTargetFromUri(String value) {
        if (value == null || !value.regionMatches(true, 0, "about:reader?", 0, 13)) return null;
        int queryStart = value.indexOf('?');
        if (queryStart < 0 || queryStart + 1 >= value.length()) return null;
        String[] parameters = value.substring(queryStart + 1).split("&");
        for (String parameter : parameters) {
            int separator = parameter.indexOf('=');
            if (separator < 0) continue;
            String name = Uri.decode(parameter.substring(0, separator).replace("+", "%20"));
            if ("url".equals(name)) {
                String target = Uri.decode(parameter.substring(separator + 1).replace("+", "%20"));
                return target == null || target.trim().isEmpty() ? null : target;
            }
        }
        return null;
    }

    public void loadUri(String uri, boolean doNotTrack) {
        ensureOpen();
        if (uri == null || uri.trim().isEmpty()) throw new IllegalArgumentException("URI must not be empty");
        Uri parsed = Uri.parse(uri);
        boolean https = "https".equalsIgnoreCase(parsed.getScheme())
            && parsed.getHost() != null && parsed.getUserInfo() == null;
        boolean blank = uri.equals("about:blank");
        boolean reader = false;
        if (uri.regionMatches(true, 0, "about:reader?", 0, 13)) {
            String target = readerTargetFromUri(uri);
            Uri targetUri = target == null ? null : Uri.parse(target);
            reader = targetUri != null && "https".equalsIgnoreCase(targetUri.getScheme())
                && targetUri.getHost() != null && targetUri.getUserInfo() == null;
        }
        if (!https && !blank && !reader) {
            throw new SecurityException("Only valid HTTPS pages and internal reader pages are allowed");
        }
        loadingUri = uri;
        if (doNotTrack) {
            Map<String, String> headers = new java.util.HashMap<>();
            headers.put("DNT", "1");
            session.load(new GeckoSession.Loader()
                .uri(uri)
                .additionalHeaders(headers)
                .headerFilter(GeckoSession.HEADER_FILTER_UNRESTRICTED_UNSAFE));
        } else {
            session.load(new GeckoSession.Loader().uri(uri));
        }
    }

    public void setJavaScriptEnabled(boolean enabled) {
        ensureOpen();
        session.getSettings().setAllowJavascript(enabled);
    }

    public void setTrackingProtectionEnabled(boolean enabled) {
        ensureOpen();
        session.getSettings().setUseTrackingProtection(enabled);
    }

    public GeckoResult<Boolean> processBackPressed() { ensureOpen(); return session.processBackPressed(); }
    public void reload() { ensureOpen(); session.reload(); }
    public void stop() { ensureOpen(); session.stop(); }
    public void goBack() { ensureOpen(); if (canGoBack) session.goBack(); }
    public void goForward() { ensureOpen(); if (canGoForward) session.goForward(); }
    public void setActive(boolean active) { if (!closed) session.setActive(active); }

    public GeckoResult<java.util.List<GeckoSession.PermissionDelegate.ContentPermission>> getSitePermissions(String uri) {
        ensureOpen();
        GeckoResult<java.util.List<GeckoSession.PermissionDelegate.ContentPermission>> result = new GeckoResult<>(mainHandler);
        if (!storageHandler.post(() -> {
            if (closed) { mainHandler.post(() -> result.completeExceptionally(new IllegalStateException("Gecko engine is closed"))); return; }
            try {
                runtime.getStorageController().getPermissions(uri, false).accept(
                    permissions -> mainHandler.post(() -> result.complete(permissions)),
                    error -> mainHandler.post(() -> result.completeExceptionally(error)));
            } catch (Throwable error) { mainHandler.post(() -> result.completeExceptionally(error)); }
        })) result.completeExceptionally(new IllegalStateException("Storage worker is unavailable"));
        return result;
    }

    public void setSitePermission(GeckoSession.PermissionDelegate.ContentPermission permission, int value) {
        ensureOpen();
        runtime.getStorageController().setPermission(permission, value);
    }

    public GeckoResult<Void> clearSiteData(String host) {
        ensureOpen();
        if (host == null || host.trim().isEmpty()) throw new IllegalArgumentException("Host is required");
        if (storageOperationInProgress) return GeckoResult.fromException(new IllegalStateException("A storage operation is already running"));
        return clearAndReattach(host,
            StorageController.ClearFlags.SITE_DATA | StorageController.ClearFlags.SITE_SETTINGS, false);
    }

    public GeckoResult<Void> clearBrowsingData() {
        ensureOpen();
        if (storageOperationInProgress) return GeckoResult.fromException(new IllegalStateException("A storage operation is already running"));
        session.purgeHistory();
        return clearAndReattach(null, StorageController.ClearFlags.ALL, true);
    }

    private GeckoResult<Void> clearAndReattach(final String host, final long flags, final boolean allHosts) {
        storageOperationInProgress = true;
        view.releaseSession();
        if (session.isOpen()) session.close();
        final GeckoResult<Void> result = new GeckoResult<Void>(mainHandler);
        boolean posted = storageHandler.post(new Runnable() {
            @Override public void run() {
                if (closed) {
                    mainHandler.post(new Runnable() {
                        @Override public void run() {
                            storageOperationInProgress = false;
                            result.completeExceptionally(new IllegalStateException("Gecko engine is closed"));
                        }
                    });
                    return;
                }
                final GeckoResult<Void> clearing;
                try {
                    StorageController storage = runtime.getStorageController();
                    clearing = allHosts ? storage.clearData(flags) : storage.clearDataFromHost(host, flags);
                } catch (final Throwable error) {
                    mainHandler.post(new Runnable() {
                        @Override public void run() {
                            if (!closed) reattachSession(false);
                            storageOperationInProgress = false;
                            result.completeExceptionally(error);
                        }
                    });
                    return;
                }
                clearing.accept(
                    new GeckoResult.Consumer<Void>() {
                        @Override public void accept(Void ignored) {
                            mainHandler.post(new Runnable() {
                                @Override public void run() {
                                    if (closed) {
                                        storageOperationInProgress = false;
                                        result.completeExceptionally(new IllegalStateException("Gecko engine is closed"));
                                        return;
                                    }
                                    reattachSession(true);
                                    storageOperationInProgress = false;
                                    result.complete(null);
                                }
                            });
                        }
                    },
                    new GeckoResult.Consumer<Throwable>() {
                        @Override public void accept(final Throwable error) {
                            mainHandler.post(new Runnable() {
                                @Override public void run() {
                                    if (!closed) reattachSession(false);
                                    storageOperationInProgress = false;
                                    result.completeExceptionally(error);
                                }
                            });
                        }
                    });
            }
        });
        if (!posted) {
            storageOperationInProgress = false;
            result.completeExceptionally(new IllegalStateException("Storage worker is unavailable"));
        }
        return result;
    }

    private void reattachSession(boolean dataCleared) {
        if (closed) return;
        session.open(runtime);
        view.setSession(session);
        if (dataCleared) {
            sessionState = null;
            currentUri = "about:blank";
            loadingUri = "about:blank";
            canGoBack = false;
            canGoForward = false;
            session.loadUri("about:blank");
        } else if (sessionState != null) {
            session.restoreState(sessionState);
        }
    }

    private GeckoResult<Void> clearAndReattach(GeckoResult<Void> clearing) {
        view.releaseSession();
        if (session.isOpen()) session.close();
        final GeckoResult<Void> result = new GeckoResult<>();
        clearing.accept(
            new GeckoResult.Consumer<Void>() {
                @Override public void accept(Void ignored) {
                    session.open(runtime);
                    view.setSession(session);
                    session.loadUri("about:blank");
                    currentUri = "about:blank";
                    sessionState = null;
                    canGoBack = false;
                    canGoForward = false;
                    result.complete(null);
                }
            },
            new GeckoResult.Consumer<Throwable>() {
                @Override public void accept(Throwable error) {
                    session.open(runtime);
                    view.setSession(session);
                    result.completeExceptionally(error);
                }
            });
        return result;
    }

    public void close() {
        if (closed) return;
        closed = true;
        storageOperationInProgress = false;
        listener = null;
        GeckoSession detached = view.releaseSession();
        if (detached != null && detached.isOpen()) detached.close();
        storageThread.quitSafely();
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Gecko engine session is closed");
    }
}
